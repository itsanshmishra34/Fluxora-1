package com.example.payments

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.finance.FinancialEngine

/**
 * UPI Payment Handoff & Validation Engine.
 * - Validates UPI VPAs (e.g., `alex@okhdfcbank`) and amount bounds (`1..MAX_AMOUNT_MINOR`).
 * - Builds standard Android `upi://pay` deep-link intents.
 * - Handles missing UPI apps, cancellation, and failure.
 * - Never fabricates payment success merely from launching an external UPI app.
 */
object PaymentManager {

    private val UPI_VPA_REGEX = Regex("^[a-zA-Z0-9._\\-]{2,64}@[a-zA-Z0-9]{2,64}$")

    // Maximum standard NPCI per-transaction UPI peer limit in minor units (INR 1,00,000.00 = 10,000,000 paise)
    const val MAX_UPI_TRANSACTION_MINOR: Long = 10_000_000L

    enum class SettlementHandoffState {
        IDLE,
        HANDOFF_INITIATED_PENDING_VERIFICATION,
        VERIFIED_SUCCESS,
        CANCELLED_BY_USER,
        FAILED,
        NO_UPI_APP_INSTALLED
    }

    data class UpiValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    data class UpiPaymentRequest(
        val payeeVpa: String,
        val payeeName: String,
        val amountMinor: Long,
        val transactionNote: String = "Fluxora Roommate Settlement",
        val transactionRef: String = "FLX-${System.currentTimeMillis()}"
    )

    fun isValidUpiVpa(vpa: String?): Boolean {
        if (vpa.isNullOrBlank()) return false
        return UPI_VPA_REGEX.matches(vpa.trim())
    }

    fun validatePaymentRequest(
        payeeVpa: String,
        amountMinor: Long
    ): UpiValidationResult {
        if (!isValidUpiVpa(payeeVpa)) {
            return UpiValidationResult(
                isValid = false,
                errorMessage = "Enter a valid UPI ID (for example, name@okhdfcbank)."
            )
        }
        if (amountMinor < FinancialEngine.MIN_AMOUNT_MINOR) {
            return UpiValidationResult(
                isValid = false,
                errorMessage = "Settlement amount must be at least INR 0.01."
            )
        }
        if (amountMinor > MAX_UPI_TRANSACTION_MINOR) {
            return UpiValidationResult(
                isValid = false,
                errorMessage = "Single UPI payment cannot exceed INR 1,00,000.00."
            )
        }
        return UpiValidationResult(isValid = true)
    }

    fun buildUpiPayUri(request: UpiPaymentRequest): Uri {
        val validation = validatePaymentRequest(request.payeeVpa, request.amountMinor)
        require(validation.isValid) { validation.errorMessage ?: "Invalid UPI payment request" }

        val amountDecimal = FinancialEngine.formatMinorToPlainDecimal(request.amountMinor)
        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", request.payeeVpa.trim())
            .appendQueryParameter("pn", request.payeeName.trim().ifEmpty { "Roommate" })
            .appendQueryParameter("am", amountDecimal)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", request.transactionNote.trim())
            .appendQueryParameter("tr", request.transactionRef.trim())
            .build()
    }

    fun buildUpiPayIntent(request: UpiPaymentRequest): Intent {
        val uri = buildUpiPayUri(request)
        return Intent(Intent.ACTION_VIEW, uri)
    }

    /**
     * Launches external UPI application handoff.
     * Note: Launching the intent returns [SettlementHandoffState.HANDOFF_INITIATED_PENDING_VERIFICATION],
     * NOT [SettlementHandoffState.VERIFIED_SUCCESS], because launching an intent alone does not prove payment succeeded.
     */
    fun launchUpiPaymentHandoff(
        context: Context,
        request: UpiPaymentRequest
    ): SettlementHandoffState {
        val validation = validatePaymentRequest(request.payeeVpa, request.amountMinor)
        if (!validation.isValid) {
            return SettlementHandoffState.FAILED
        }
        return try {
            val intent = buildUpiPayIntent(request).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pm = context.packageManager
            if (pm != null && intent.resolveActivity(pm) == null) {
                SettlementHandoffState.NO_UPI_APP_INSTALLED
            } else {
                context.startActivity(intent)
                SettlementHandoffState.HANDOFF_INITIATED_PENDING_VERIFICATION
            }
        } catch (e: Exception) {
            SettlementHandoffState.NO_UPI_APP_INSTALLED
        }
    }

    /**
     * Evaluates an Android ActivityResult response string returned by a UPI app (`response` extra).
     */
    fun evaluateUpiActivityResponse(resultCode: Int, responseString: String?): SettlementHandoffState {
        if (responseString.isNullOrBlank()) {
            return if (resultCode == android.app.Activity.RESULT_CANCELED) {
                SettlementHandoffState.CANCELLED_BY_USER
            } else {
                SettlementHandoffState.FAILED
            }
        }
        val params = responseString.split("&").associate { pair ->
            val parts = pair.split("=", limit = 2)
            val key = parts.getOrNull(0)?.trim()?.lowercase() ?: ""
            val value = parts.getOrNull(1)?.trim() ?: ""
            key to value
        }
        val status = params["status"]?.uppercase() ?: ""
        val txnId = params["txnid"] ?: params["approvalrefno"] ?: ""
        return when {
            status == "SUCCESS" && txnId.isNotBlank() -> SettlementHandoffState.VERIFIED_SUCCESS
            status == "SUBMITTED" || status == "PENDING" -> SettlementHandoffState.HANDOFF_INITIATED_PENDING_VERIFICATION
            status.contains("CANCEL") -> SettlementHandoffState.CANCELLED_BY_USER
            else -> SettlementHandoffState.FAILED
        }
    }

    /**
     * Two-Party Settlement State Machine (Fluxora v2).
     * Launching a UPI app is never treated as proof of payment;
     * payer marks CONFIRMATION_REQUIRED and receiver confirms CONFIRMED_BY_RECEIVER ("Settled (confirmed by both)").
     */
    enum class SettlementState(val displayLabel: String) {
        PENDING("Balance owed"),
        INITIATED("UPI app opened"),
        USER_CANCELLED("Cancelled"),
        FAILED("Payment failed"),
        CONFIRMATION_REQUIRED("Awaiting receiver confirmation"),
        CONFIRMED_BY_RECEIVER("Settled (confirmed by both)"),
        DISPUTED("Not received — disputed"),
        VERIFIED_SUCCESS("Settled (provider verified)")
    }

    /**
     * Validates two-party settlement state transitions:
     * - Launching UPI intent transitions to INITIATED (never directly to settled).
     * - Payer marks paid -> CONFIRMATION_REQUIRED.
     * - Receiver confirms receipt -> CONFIRMED_BY_RECEIVER ("Settled (confirmed by both)").
     * - Receiver disputes -> DISPUTED.
     */
    fun validateSettlementTransition(
        current: SettlementState,
        target: SettlementState,
        isPayer: Boolean,
        isReceiver: Boolean
    ): Boolean {
        return when (target) {
            SettlementState.INITIATED,
            SettlementState.USER_CANCELLED,
            SettlementState.FAILED ->
                isPayer && current in setOf(
                    SettlementState.PENDING,
                    SettlementState.INITIATED,
                    SettlementState.USER_CANCELLED,
                    SettlementState.FAILED,
                    SettlementState.DISPUTED
                )
            SettlementState.CONFIRMATION_REQUIRED ->
                isPayer && current in setOf(
                    SettlementState.PENDING,
                    SettlementState.INITIATED,
                    SettlementState.USER_CANCELLED,
                    SettlementState.FAILED,
                    SettlementState.DISPUTED
                )
            SettlementState.CONFIRMED_BY_RECEIVER,
            SettlementState.DISPUTED ->
                isReceiver && current == SettlementState.CONFIRMATION_REQUIRED
            SettlementState.VERIFIED_SUCCESS -> false // Reserved for signed webhook provider only
            SettlementState.PENDING -> false
        }
    }

    /**
     * Masks a saved UPI VPA for display (e.g. `rohan@okaxis` -> `ro•••@okaxis`).
     */
    fun maskVpaForDisplay(vpa: String?): String {
        val trimmed = vpa?.trim().orEmpty()
        if (!trimmed.contains("@")) return trimmed
        val parts = trimmed.split("@", limit = 2)
        val handle = parts[0]
        val bank = parts[1]
        if (handle.length <= 2) return "${handle.firstOrNull() ?: '•'}••@$bank"
        return "${handle.take(2)}•••@$bank"
    }

    /**
     * Extracts `(payeeVpa, payeeName)` from a scanned `upi://pay?pa=...&pn=...` QR string
     * so users can save payees from QR codes without manual VPA typing.
     */
    fun parseUpiQrCode(qrPayload: String): Pair<String, String>? {
        val trimmed = qrPayload.trim()
        if (!trimmed.lowercase().startsWith("upi://pay")) return null
        val queryStart = trimmed.indexOf('?')
        if (queryStart < 0 || queryStart == trimmed.length - 1) return null
        val query = trimmed.substring(queryStart + 1)
        val params = query.split("&").associate { pair ->
            val kv = pair.split("=", limit = 2)
            val k = kv.getOrNull(0)?.trim()?.lowercase().orEmpty()
            val v = Uri.decode(kv.getOrNull(1)?.trim().orEmpty())
            k to v
        }
        val pa = params["pa"].orEmpty().trim()
        val pn = params["pn"].orEmpty().trim().ifEmpty { pa.substringBefore("@") }
        return if (isValidUpiVpa(pa)) pa to pn else null
    }
}
