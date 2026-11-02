package com.example.authentication

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthManager"

/**
 * Multi-Modal Firebase Authentication & Phone OTP Verification Manager.
 * Uses Jetpack Credential Manager (`GetSignInWithGoogleOption` and `GetGoogleIdOption`)
 * with `R.string.default_web_client_id` and Firebase Phone Auth OTP verification.
 */
object AuthManager {

    fun observeAuthState(auth: FirebaseAuth = Firebase.auth): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Silent Auto-Sign-In on startup using a single GetGoogleIdOption in GetCredentialRequest.
     */
    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (Firebase.auth.currentUser != null) {
            onAuthSuccess()
            return
        }
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        // CRITICAL: Exactly one credential option per request for Android 16 (API 36) responsiveness
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Silent Google Sign-In cancelled or dismissed: ${e.message}", e)
                onUnauthenticated()
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    /**
     * Interactive Google Sign-In using a single GetSignInWithGoogleOption in GetCredentialRequest.
     */
    fun onGoogleSignInClicked(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onAuthError("Sign-in is temporarily unavailable. Please try again later.")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onAuthError("Unable to complete Google Sign-In. Please try again.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError("Unable to sign in right now. Please check your connection and try again.")
            }
        }
    }

    /**
     * Initiates Firebase Phone Auth OTP verification for an E.164 phone number.
     */
    fun startPhoneOtpVerification(
        activity: Activity,
        e164PhoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onInstantVerified: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (String) -> Unit
    ) {
        if (!PhoneValidation.isValidE164Phone(e164PhoneNumber)) {
            onVerificationFailed("Please enter a valid phone number with country code.")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onInstantVerified(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "Phone OTP verification failed: ${e.message}", e)
                onVerificationFailed("Unable to verify your phone number right now. Please try again.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(e164PhoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun buildPhoneCredential(verificationId: String, otpCode: String): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId, otpCode.trim())
    }

    /**
     * Signs in or registers a user via Firebase Email & Password authentication.
     */
    fun signInOrRegisterWithEmail(
        email: String,
        password: String,
        isRegisterMode: Boolean,
        scope: CoroutineScope,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            onAuthError("Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            onAuthError("Password must be at least 6 characters.")
            return
        }

        scope.launch {
            try {
                if (isRegisterMode) {
                    Firebase.auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
                } else {
                    Firebase.auth.signInWithEmailAndPassword(trimmedEmail, password).await()
                }
                onAuthSuccess()
            } catch (e: Exception) {
                Log.w(TAG, "Email auth failed: ${e.message}", e)
                val friendlyMsg = if (isRegisterMode) {
                    "Unable to create account with this email. Try signing in or using Google Sign-In."
                } else {
                    "Unable to sign in with those credentials. Please check your email and password or continue with Google."
                }
                onAuthError(friendlyMsg)
            }
        }
    }

    /**
     * Sends a password reset email via Firebase Auth.
     */
    fun sendPasswordReset(
        email: String,
        scope: CoroutineScope,
        onResult: (String) -> Unit
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            onResult("Enter your email address above to receive a password reset link.")
            return
        }
        scope.launch {
            try {
                Firebase.auth.sendPasswordResetEmail(trimmedEmail).await()
                onResult("If an account exists for $trimmedEmail, a reset link has been sent.")
            } catch (e: Exception) {
                Log.w(TAG, "Password reset failed: ${e.message}", e)
                onResult("Unable to send reset link right now. Please try again later.")
            }
        }
    }

    /**
     * Signs out of Firebase Auth and clears Jetpack Credential Manager state.
     */
    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onSignOutComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        Firebase.auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear Credential Manager state", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
