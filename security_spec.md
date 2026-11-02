# Fluxora Security Specification (`security_spec.md`)

## 1. Data Invariants
1. **Strict Authenticated Multi-Tenancy**: Every financial record (`transactions`, `categories`, `budgets`, `savings_goals`, `recurring_bills`, `household_members`, `shared_expenses`, `notifications`) resides under `/users/{userId}/...` and is strictly accessible only when `request.auth != null && request.auth.uid == userId` and `incoming().userId == request.auth.uid`.
2. **Minor-Unit Monetary Bounds**: Every financial amount (`amountMinor`, `limitMinor`, `targetMinor`, `totalAmountMinor`) must be an integer within `1..100000000000` (`MAX_AMOUNT_MINOR`, representing INR 1,000,000,000.00 in paise).
3. **Exclusive Phone Ownership Registry**: `/phone_registry/{e164Phone}` maps an E.164 phone number (`^\+[1-9][0-9]{6,14}$`) to a single `ownerUid`. Once claimed by `ownerUid`, no other user can overwrite or hijack it.
4. **Bidirectional Roommate Connection Request Security**:
   - Stored in `/connection_requests/{requestId}`.
   - Self-requests (`senderUid == recipientUid` or `senderPhone == recipientPhone`) are strictly rejected on creation.
   - Creation requires `senderUid == request.auth.uid` and `status == 'PENDING'`.
   - Updates to `status` (`'ACCEPTED'` or `'DECLINED'`) are strictly restricted to the authorized recipient (`existing().recipientUid == request.auth.uid` and `existing().status == 'PENDING'`). Senders and unrelated third parties are blocked from accepting or declining.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Scrape**: Unauthenticated read on `/users/alice/transactions` -> Rejected.
2. **Cross-User Transaction Read**: `bob` reading `/users/alice/transactions/tx_1` -> Rejected.
3. **Cross-User Transaction Write**: `bob` creating `/users/alice/transactions/tx_2` -> Rejected.
4. **Identity Spoofing Payload**: `alice` creating `/users/alice/transactions/tx_3` with `userId: "bob"` -> Rejected.
5. **Zero or Negative Amount Minor**: `alice` creating a transaction with `amountMinor: 0` or `-500` -> Rejected.
6. **Overflow Amount Minor**: `alice` creating a transaction with `amountMinor: 999999999999999` -> Rejected.
7. **Shadow Ghost Field Injection**: `alice` updating a budget with `{ "isAdmin": true }` -> Rejected by `hasOnly()`.
8. **Immutable `createdAt` Tampering**: `alice` updating `createdAt` on an existing transaction -> Rejected.
9. **Phone Hijack Attempt**: `bob` attempting to overwrite `/phone_registry/+919876543210` owned by `alice` -> Rejected.
10. **Self Roommate Connection Request**: `alice` creating a request where `senderUid == recipientUid` or `senderPhone == recipientPhone` -> Rejected.
11. **Sender Self-Accepting Roommate Request**: `alice` (sender) updating `status` from `PENDING` to `ACCEPTED` -> Rejected.
12. **Terminal State Re-Mutation**: Updating a `connection_requests` document after `status` is already `ACCEPTED` or `DECLINED` -> Rejected.
