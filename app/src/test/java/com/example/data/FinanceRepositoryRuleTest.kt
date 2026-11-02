package com.example.data

import android.os.Looper
import com.example.base.FirestoreEmulatorTestBase
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.Shadows.shadowOf

class FinanceRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private suspend fun <T> awaitWithMainLooper(task: Task<T>): T {
        while (!task.isComplete) {
            shadowOf(Looper.getMainLooper()).idle()
            delay(10)
        }
        if (task.isSuccessful) {
            return task.result
        } else {
            throw task.exception ?: RuntimeException("Task failed with no exception")
        }
    }

    private suspend fun <T> runWithMainLooper(block: suspend () -> T): T = kotlinx.coroutines.coroutineScope {
        val deferred = async { block() }
        while (!deferred.isCompleted) {
            shadowOf(Looper.getMainLooper()).idle()
            delay(10)
        }
        deferred.await()
    }

    @Test
    fun `owner can save and delete transaction via FinanceRepository`() = runBlocking {
        val authResult = awaitWithMainLooper(
            auth.createUserWithEmailAndPassword("owner@fluxora.app", "password123")
        )
        val uid = authResult.user!!.uid
        val repository = FinanceRepository(db)

        val saveResult = runWithMainLooper {
            repository.addTransaction(
                title = "Metro Card Recharge",
                amountMinor = 50_000L,
                type = TransactionType.EXPENSE,
                categoryId = "transportation",
                categoryName = "Transportation",
                timestampMillis = 1730000000000L,
                notes = "Monthly pass",
                nowMillis = 1730005000000L
            )
        }
        assertTrue("Save transaction failed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)
        val createdTx = saveResult.getOrThrow()

        val snap = awaitWithMainLooper(
            db.collection("users").document(uid).collection("transactions").document(createdTx.id).get()
        )
        assertTrue(snap.exists())
        assertEquals(50_000L, snap.getLong("amountMinor"))

        val deleteResult = runWithMainLooper { repository.deleteTransaction(createdTx.id) }
        assertTrue(deleteResult.isSuccess)
        val deletedSnap = awaitWithMainLooper(
            db.collection("users").document(uid).collection("transactions").document(createdTx.id).get()
        )
        assertFalse(deletedSnap.exists())
    }

    @Test
    fun `cross-user direct write to another user subcollection is denied by security rules`() = runBlocking {
        val authResult = awaitWithMainLooper(
            auth.createUserWithEmailAndPassword("attacker@fluxora.app", "password123")
        )
        val attackerUid = authResult.user!!.uid

        val forgedDocRef = db.collection("users")
            .document("victim_uid_999")
            .collection("transactions")
            .document("tx_forged")

        val task = forgedDocRef.set(
            mapOf(
                "userId" to attackerUid,
                "title" to "Forged",
                "amountMinor" to 10_000L,
                "type" to "EXPENSE",
                "categoryId" to "other",
                "categoryName" to "Other",
                "timestampMillis" to 1730000000000L
            )
        )

        var permissionDenied = false
        try {
            awaitWithMainLooper(task)
        } catch (e: Exception) {
            permissionDenied = true
        }
        assertTrue("Expected cross-user write to be denied by Firestore rules", permissionDenied)
    }

    @Test
    fun `markBillAsPaid executes atomic transaction and enforces 10-second idempotency guard`() = runBlocking {
        val authResult = awaitWithMainLooper(
            auth.createUserWithEmailAndPassword("biller@fluxora.app", "password123")
        )
        val uid = authResult.user!!.uid
        val repository = FinanceRepository(db)

        val createBillRes = runWithMainLooper {
            repository.createRecurringBill(
                title = "Gigabit Fiber",
                amountMinor = 119_900L,
                categoryId = "bills_utilities",
                categoryName = "Bills & Utilities",
                frequency = BillFrequency.MONTHLY,
                nextDueDateMillis = 1730000000000L
            )
        }
        assertTrue(createBillRes.isSuccess)
        val bill = createBillRes.getOrThrow()

        val now = 1730005000000L
        val firstPayResult = runWithMainLooper { repository.markBillAsPaid(bill, nowMillis = now) }
        assertTrue("First bill payment should succeed: ${firstPayResult.exceptionOrNull()}", firstPayResult.isSuccess)
        val (updatedBill, _) = firstPayResult.getOrThrow()
        assertEquals(now, updatedBill.lastPaidAtMillis)
        assertTrue(updatedBill.nextDueDateMillis > bill.nextDueDateMillis)

        // Rapid second tap within 10 seconds must be rejected by the idempotency guard
        val rapidSecondTap = runWithMainLooper {
            repository.markBillAsPaid(updatedBill, nowMillis = now + 3_000L)
        }
        assertTrue(rapidSecondTap.isFailure)
    }
}
