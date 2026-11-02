package com.example.base

import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import java.net.HttpURLConnection
import java.net.URL
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
abstract class FirestoreEmulatorTestBase {
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var projectId: String
    lateinit var databaseId: String

    @Before
    fun setupEmulator() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        projectId = "demo-no-project"
        databaseId = context.getString(R.string.firestore_database_id)

        FirebaseApp.clearInstancesForTest()

        val options = FirebaseOptions.Builder()
            .setProjectId(projectId)
            .setApplicationId("1:1234567890:android:abcdef")
            .setApiKey("fake-api-key-for-emulator")
            .build()
        val app = FirebaseApp.initializeApp(context, options)

        auth = FirebaseAuth.getInstance(app)
        auth.useEmulator("127.0.0.1", 9099)

        db = FirebaseFirestore.getInstance(app, databaseId)
        db.useEmulator("127.0.0.1", 8085)

        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()
        db.firestoreSettings = settings

        clearEmulatorData()
    }

    @After
    fun tearDownEmulator() {
        auth.signOut()
        db.terminate()
        FirebaseApp.clearInstancesForTest()
    }

    private fun clearEmulatorData() {
        clearEndpoint("http://127.0.0.1:8085/emulator/v1/projects/$projectId/databases/$databaseId/documents")
        clearEndpoint("http://127.0.0.1:9099/emulator/v1/projects/$projectId/accounts")
    }

    private fun clearEndpoint(urlString: String) {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("Failed to clear emulator at $urlString: HTTP $code")
            }
        } finally {
            conn.disconnect()
        }
    }
}
