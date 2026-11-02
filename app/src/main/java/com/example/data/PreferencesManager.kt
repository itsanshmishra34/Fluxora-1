package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.authentication.PhoneValidation
import com.example.notifications.NotificationCategory
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.fluxoraDataStore: DataStore<Preferences> by preferencesDataStore(name = "fluxora_preferences")

/**
 * Manages user-scoped phone verification & ownership flows, theme preferences,
 * privacy masking, biometric lock timeout, and granular notification toggles.
 */
class PreferencesManager(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.fluxoraDataStore)

    companion object {
        private val KEY_THEME = stringPreferencesKey("selected_theme")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
        private val KEY_THEME_MODE = stringPreferencesKey("cinematic_theme_mode")
        private val KEY_GLASS_EFFECT_PREF = stringPreferencesKey("glass_effect_preference")
        private val KEY_HIDE_SENSITIVE = booleanPreferencesKey("hide_sensitive_amounts")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        private val KEY_INACTIVITY_TIMEOUT_MIN = intPreferencesKey("inactivity_timeout_minutes")
        private val KEY_MASTER_NOTIFICATIONS = booleanPreferencesKey("master_notifications_enabled")
        private val KEY_ADVANCED_MODE = booleanPreferencesKey("advanced_settings_mode")
        private val KEY_AI_ADVISOR_ENABLED = booleanPreferencesKey("ai_advisor_consent_enabled")
        private val KEY_LANGUAGE_CODE = stringPreferencesKey("selected_language_code")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        private fun keyPhoneVerifiedForUid(uid: String) = booleanPreferencesKey("phone_verified_uid_$uid")
        private fun keyVerifiedPhoneForUid(uid: String) = stringPreferencesKey("verified_phone_uid_$uid")
        private fun keyOwnerUidForPhone(e164Phone: String) = stringPreferencesKey("phone_owner_${e164Phone.trim()}")
        private fun keyCategoryNotification(category: NotificationCategory) =
            booleanPreferencesKey("notif_category_${category.name}")
    }

    // --- Per-User Phone Verification & Ownership Scoping ---

    fun isPhoneVerifiedFlow(uid: String): Flow<Boolean> {
        if (uid.isBlank()) return dataStore.data.map { false }
        return dataStore.data.map { prefs ->
            val isVerified = prefs[keyPhoneVerifiedForUid(uid)] ?: false
            val phone = prefs[keyVerifiedPhoneForUid(uid)]
            val owner = if (!phone.isNullOrBlank()) prefs[keyOwnerUidForPhone(phone)] else null
            isVerified && !phone.isNullOrBlank() && owner == uid
        }
    }

    fun getVerifiedPhoneFlow(uid: String): Flow<String?> {
        if (uid.isBlank()) return dataStore.data.map { null }
        return dataStore.data.map { prefs ->
            val phone = prefs[keyVerifiedPhoneForUid(uid)]
            if (!phone.isNullOrBlank() && prefs[keyOwnerUidForPhone(phone)] == uid) {
                phone
            } else {
                null
            }
        }
    }

    fun getPhoneOwnerFlow(e164Phone: String): Flow<String?> {
        val normalized = e164Phone.trim()
        if (!PhoneValidation.isValidE164Phone(normalized)) return dataStore.data.map { null }
        return dataStore.data.map { prefs ->
            prefs[keyOwnerUidForPhone(normalized)]
        }
    }

    /**
     * Scopes a verified E.164 phone number to [uid] and prevents duplicate phone number
     * claiming across different accounts.
     */
    suspend fun recordVerifiedPhone(uid: String, e164Phone: String): Result<String> {
        require(uid.isNotBlank()) { "User UID must not be blank" }
        val normalized = e164Phone.trim()
        if (!PhoneValidation.isValidE164Phone(normalized)) {
            return Result.failure(IllegalArgumentException("Invalid E.164 phone number: $e164Phone"))
        }

        var claimError: String? = null
        dataStore.edit { prefs ->
            val existingOwner = prefs[keyOwnerUidForPhone(normalized)]
            if (existingOwner != null && existingOwner != uid) {
                claimError = "This phone number ($normalized) is already verified and owned by another account."
            } else {
                // Release previous phone if this user changed numbers
                val previousPhone = prefs[keyVerifiedPhoneForUid(uid)]
                if (!previousPhone.isNullOrBlank() && previousPhone != normalized) {
                    if (prefs[keyOwnerUidForPhone(previousPhone)] == uid) {
                        prefs.remove(keyOwnerUidForPhone(previousPhone))
                    }
                }
                prefs[keyPhoneVerifiedForUid(uid)] = true
                prefs[keyVerifiedPhoneForUid(uid)] = normalized
                prefs[keyOwnerUidForPhone(normalized)] = uid
            }
        }

        return if (claimError != null) {
            Result.failure(IllegalStateException(claimError))
        } else {
            Result.success(normalized)
        }
    }

    /**
     * Releases a user's verified phone number claim upon account/data deletion.
     */
    suspend fun releaseVerifiedPhone(uid: String) {
        if (uid.isBlank()) return
        dataStore.edit { prefs ->
            val currentPhone = prefs[keyVerifiedPhoneForUid(uid)]
            if (!currentPhone.isNullOrBlank() && prefs[keyOwnerUidForPhone(currentPhone)] == uid) {
                prefs.remove(keyOwnerUidForPhone(currentPhone))
            }
            prefs.remove(keyPhoneVerifiedForUid(uid))
            prefs.remove(keyVerifiedPhoneForUid(uid))
        }
    }

    // --- Theme & Visual Appearance ---

    val selectedThemeFlow: Flow<AppTheme> = dataStore.data.map { prefs ->
        AppTheme.fromId(prefs[KEY_THEME])
    }

    val isDarkModeFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: true
    }

    val themeModeFlow: Flow<com.example.ui.theme.CinematicThemeMode> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME_MODE]
        if (raw != null) {
            com.example.ui.theme.CinematicThemeMode.fromId(raw)
        } else {
            val dark = prefs[KEY_DARK_MODE] ?: true
            if (dark) com.example.ui.theme.CinematicThemeMode.DARK else com.example.ui.theme.CinematicThemeMode.LIGHT
        }
    }

    val glassEffectPreferenceFlow: Flow<com.example.ui.glass.GlassEffectPreference> = dataStore.data.map { prefs ->
        com.example.ui.glass.GlassEffectPreference.fromId(prefs[KEY_GLASS_EFFECT_PREF])
    }

    suspend fun setSelectedTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.id
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
            prefs[KEY_THEME_MODE] = if (enabled) {
                com.example.ui.theme.CinematicThemeMode.DARK.id
            } else {
                com.example.ui.theme.CinematicThemeMode.LIGHT.id
            }
        }
    }

    suspend fun setThemeMode(mode: com.example.ui.theme.CinematicThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.id
            if (mode == com.example.ui.theme.CinematicThemeMode.DARK) {
                prefs[KEY_DARK_MODE] = true
            } else if (mode == com.example.ui.theme.CinematicThemeMode.LIGHT) {
                prefs[KEY_DARK_MODE] = false
            }
        }
    }

    suspend fun setGlassEffectPreference(pref: com.example.ui.glass.GlassEffectPreference) {
        dataStore.edit { prefs ->
            prefs[KEY_GLASS_EFFECT_PREF] = pref.id
        }
    }

    // --- Sensitive Amount Privacy Masking ---

    val hideSensitiveAmountsFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_HIDE_SENSITIVE] ?: false
    }

    suspend fun setHideSensitiveAmounts(hide: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HIDE_SENSITIVE] = hide
        }
    }

    // --- Progressive Disclosure: Simple / Advanced Settings Mode ---

    val isAdvancedModeFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ADVANCED_MODE] ?: false
    }

    suspend fun setAdvancedMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ADVANCED_MODE] = enabled
        }
    }

    // --- AI Financial Advisor Opt-In Consent & Language ---

    val isAiAdvisorEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AI_ADVISOR_ENABLED] ?: true
    }

    suspend fun setAiAdvisorEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AI_ADVISOR_ENABLED] = enabled
        }
    }

    val selectedLanguageCodeFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE_CODE] ?: "en"
    }

    suspend fun setSelectedLanguageCode(languageCode: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE_CODE] = languageCode.trim().ifEmpty { "en" }
        }
    }

    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    // --- Biometric Lock & Inactivity Timeout ---

    val isBiometricLockEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val inactivityTimeoutMinutesFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_INACTIVITY_TIMEOUT_MIN] ?: 5
    }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setInactivityTimeoutMinutes(minutes: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_INACTIVITY_TIMEOUT_MIN] = minutes.coerceIn(1, 60)
        }
    }

    // --- Granular Notification Engine Preferences ---
    // Master switch preserves individual category toggles when disabled and re-enabled.

    val masterNotificationsEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_MASTER_NOTIFICATIONS] ?: true
    }

    fun isCategoryToggleCheckedFlow(category: NotificationCategory): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[keyCategoryNotification(category)] ?: true
        }
    }

    fun isCategoryEffectivelyEnabledFlow(category: NotificationCategory): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            val master = prefs[KEY_MASTER_NOTIFICATIONS] ?: true
            val individual = prefs[keyCategoryNotification(category)] ?: true
            master && individual
        }
    }

    suspend fun setMasterNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            // Only update the master key; preserve all individual category keys untouched!
            prefs[KEY_MASTER_NOTIFICATIONS] = enabled
        }
    }

    suspend fun setCategoryNotificationEnabled(category: NotificationCategory, enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[keyCategoryNotification(category)] = enabled
        }
    }
}
