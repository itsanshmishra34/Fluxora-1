package com.example.roommate

import com.example.authentication.PhoneValidation
import com.example.data.HouseholdMember
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ConnectionRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

/**
 * Bidirectional Roommate Connection Request with strict sender/recipient query separation,
 * self-request blocking, unverified phone blocking, recipient-only transition authorization,
 * and remote sync tampering protection.
 */
@IgnoreExtraProperties
data class RoommateConnectionRequest(
    @DocumentId val id: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val senderUpiVpa: String? = null,
    val recipientUid: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val status: String = ConnectionRequestStatus.PENDING.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    /**
     * Strict query separation: true ONLY when this request is addressed TO [userUid] or [verifiedUserPhoneE164]
     * and was NOT sent by [userUid].
     */
    fun isIncomingFor(userUid: String, verifiedUserPhoneE164: String? = null): Boolean {
        if (userUid.isBlank()) return false
        if (senderUid == userUid) return false
        val matchesUid = recipientUid.isNotBlank() && recipientUid == userUid
        val matchesPhone = !verifiedUserPhoneE164.isNullOrBlank() &&
            PhoneValidation.isValidE164Phone(verifiedUserPhoneE164) &&
            recipientPhone == verifiedUserPhoneE164.trim()
        return matchesUid || matchesPhone
    }

    /**
     * Strict query separation: true ONLY when this request was originated BY [userUid]
     * and is NOT addressed to [userUid].
     */
    fun isOutgoingFor(userUid: String): Boolean {
        if (userUid.isBlank()) return false
        return senderUid == userUid && recipientUid != userUid
    }

    fun canBeAcceptedOrDeclinedBy(actorUid: String, verifiedActorPhoneE164: String?): Boolean {
        if (status != ConnectionRequestStatus.PENDING.name) return false
        if (verifiedActorPhoneE164.isNullOrBlank() || !PhoneValidation.isValidE164Phone(verifiedActorPhoneE164)) {
            return false
        }
        return isIncomingFor(actorUid, verifiedActorPhoneE164)
    }
}

object RoommateRequestSecurityValidator {

    /**
     * Validates creating a new outgoing roommate connection request:
     * - Requires sender's phone to be verified E.164.
     * - Requires recipient's phone to be valid E.164.
     * - Blocks self-requests by UID or phone number.
     * - Blocks duplicate pending requests between the same pair of users/phones.
     */
    fun validateNewRequest(
        senderUid: String,
        senderName: String,
        senderVerifiedPhoneE164: String?,
        isSenderPhoneVerified: Boolean,
        recipientUid: String,
        recipientName: String,
        recipientPhoneE164: String,
        existingRequests: List<RoommateConnectionRequest>
    ): Result<RoommateConnectionRequest> {
        if (senderUid.isBlank()) {
            return Result.failure(IllegalArgumentException("Sender must be authenticated."))
        }
        if (!isSenderPhoneVerified || senderVerifiedPhoneE164.isNullOrBlank() || !PhoneValidation.isValidE164Phone(senderVerifiedPhoneE164)) {
            return Result.failure(IllegalStateException("You must verify your phone number before sending roommate connection requests."))
        }
        val normalizedSenderPhone = senderVerifiedPhoneE164.trim()
        val normalizedRecipientPhone = recipientPhoneE164.trim()
        if (!PhoneValidation.isValidE164Phone(normalizedRecipientPhone)) {
            return Result.failure(IllegalArgumentException("Recipient phone number must be a valid E.164 number."))
        }
        if (senderUid == recipientUid || normalizedSenderPhone == normalizedRecipientPhone) {
            return Result.failure(IllegalArgumentException("Self-requests by UID or phone number are not permitted."))
        }

        val hasDuplicatePending = existingRequests.any { existing ->
            existing.status == ConnectionRequestStatus.PENDING.name && (
                (existing.senderPhone == normalizedSenderPhone && existing.recipientPhone == normalizedRecipientPhone) ||
                    (existing.senderPhone == normalizedRecipientPhone && existing.recipientPhone == normalizedSenderPhone) ||
                    (existing.senderUid == senderUid && existing.recipientUid == recipientUid && recipientUid.isNotBlank())
                )
        }
        if (hasDuplicatePending) {
            return Result.failure(IllegalStateException("A pending roommate connection request already exists for this roommate."))
        }

        return Result.success(
            RoommateConnectionRequest(
                id = "req_${senderUid.take(8)}_${System.currentTimeMillis()}",
                senderUid = senderUid,
                senderName = senderName.trim().ifEmpty { "Fluxora User" },
                senderPhone = normalizedSenderPhone,
                recipientUid = recipientUid.trim(),
                recipientName = recipientName.trim().ifEmpty { "Roommate" },
                recipientPhone = normalizedRecipientPhone,
                status = ConnectionRequestStatus.PENDING.name
            )
        )
    }

    /**
     * Validates an accept or decline transition on an existing request.
     * Enforces that ONLY the authorized recipient with a verified phone number can accept or decline,
     * blocking senders and unrelated third parties.
     */
    fun validateStatusTransition(
        request: RoommateConnectionRequest,
        actorUid: String,
        actorVerifiedPhoneE164: String?,
        isActorPhoneVerified: Boolean,
        targetStatus: ConnectionRequestStatus
    ): Result<RoommateConnectionRequest> {
        if (targetStatus == ConnectionRequestStatus.PENDING) {
            return Result.failure(IllegalArgumentException("Cannot transition back to PENDING."))
        }
        if (request.status != ConnectionRequestStatus.PENDING.name) {
            return Result.failure(IllegalStateException("Request is already finalized (${request.status})."))
        }
        if (!isActorPhoneVerified || actorVerifiedPhoneE164.isNullOrBlank()) {
            return Result.failure(SecurityException("Unverified phone numbers cannot accept or decline roommate requests."))
        }
        if (request.senderUid == actorUid || request.senderPhone == actorVerifiedPhoneE164.trim()) {
            return Result.failure(SecurityException("Senders cannot accept or decline their own outgoing connection request."))
        }
        if (!request.isIncomingFor(actorUid, actorVerifiedPhoneE164)) {
            return Result.failure(SecurityException("Only the authorized recipient may accept or decline this request."))
        }

        return Result.success(request.copy(status = targetStatus.name))
    }

    /**
     * Protects against remote sync tampering (`syncRemoteConnectionRequest`):
     * Rejects remote updates that mutate immutable identity fields (`senderUid`, `senderPhone`, `recipientPhone`),
     * forge self-requests, or transition status unlawfully.
     * When a valid remote or local transition reaches `ACCEPTED`, automatically builds the `HouseholdMember` entry.
     */
    data class RemoteSyncOutcome(
        val acceptedRequest: RoommateConnectionRequest,
        val autoCreatedHouseholdMember: HouseholdMember?
    )

    fun syncRemoteConnectionRequest(
        localExisting: RoommateConnectionRequest?,
        remoteIncoming: RoommateConnectionRequest,
        currentUserUid: String,
        currentUserVerifiedPhone: String?
    ): Result<RemoteSyncOutcome> {
        if (remoteIncoming.senderUid.isBlank() ||
            !PhoneValidation.isValidE164Phone(remoteIncoming.senderPhone) ||
            !PhoneValidation.isValidE164Phone(remoteIncoming.recipientPhone)
        ) {
            return Result.failure(SecurityException("Tampered remote request: invalid sender or recipient phone."))
        }
        if (remoteIncoming.senderUid == remoteIncoming.recipientUid ||
            remoteIncoming.senderPhone == remoteIncoming.recipientPhone
        ) {
            return Result.failure(SecurityException("Tampered remote request: self-request detected."))
        }
        if (!remoteIncoming.isIncomingFor(currentUserUid, currentUserVerifiedPhone) &&
            !remoteIncoming.isOutgoingFor(currentUserUid)
        ) {
            return Result.failure(SecurityException("Unauthorized remote request: does not belong to current user."))
        }

        if (localExisting != null) {
            // Immutable fields must never change during remote sync
            if (localExisting.senderUid != remoteIncoming.senderUid ||
                localExisting.senderPhone != remoteIncoming.senderPhone ||
                localExisting.recipientPhone != remoteIncoming.recipientPhone
            ) {
                return Result.failure(SecurityException("Remote sync tampering detected: immutable identity fields altered."))
            }
            // Once terminal, status cannot revert or change
            if (localExisting.status != ConnectionRequestStatus.PENDING.name &&
                localExisting.status != remoteIncoming.status
            ) {
                return Result.failure(SecurityException("Remote sync tampering detected: terminal status mutated."))
            }
        }

        val householdMember = if (remoteIncoming.status == ConnectionRequestStatus.ACCEPTED.name) {
            val isCurrentUserSender = remoteIncoming.senderUid == currentUserUid
            val roommateUid = if (isCurrentUserSender) remoteIncoming.recipientUid else remoteIncoming.senderUid
            val roommateName = if (isCurrentUserSender) remoteIncoming.recipientName else remoteIncoming.senderName
            val roommatePhone = if (isCurrentUserSender) remoteIncoming.recipientPhone else remoteIncoming.senderPhone
            val roommateUpi = if (isCurrentUserSender) null else remoteIncoming.senderUpiVpa
            HouseholdMember(
                id = "member_${roommatePhone.filter { it.isDigit() }}",
                userId = currentUserUid,
                memberUid = roommateUid.ifEmpty { "uid_${roommatePhone.filter { it.isDigit() }}" },
                name = roommateName,
                phoneE164 = roommatePhone,
                upiVpa = roommateUpi
            )
        } else {
            null
        }

        return Result.success(
            RemoteSyncOutcome(
                acceptedRequest = remoteIncoming,
                autoCreatedHouseholdMember = householdMember
            )
        )
    }
}
