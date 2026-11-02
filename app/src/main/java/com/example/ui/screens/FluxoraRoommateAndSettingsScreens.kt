package com.example.ui.screens

import com.example.i18n.t
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.authentication.CountryPhoneInputField
import com.example.authentication.PhoneValidation
import com.example.authors.AuthorsScreenSection
import com.example.data.ParticipantSplit
import com.example.data.SharedExpense
import com.example.data.SplitMethod
import com.example.finance.FinanceDashboardState
import com.example.finance.FinancialEngine
import com.example.legal.LegalComplianceSectionView
import com.example.notifications.NotificationCategory
import com.example.payments.PaymentManager
import com.example.roommate.ConnectionRequestStatus
import com.example.roommate.RoommateConnectionRequest
import com.example.ui.glass.GlassEffectPreference
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CinematicThemeMode
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoommateSplitScreen(
    state: FinanceDashboardState,
    currentUserDisplayName: String,
    onVerifyAndClaimPhone: (e164Phone: String) -> Unit,
    onSendConnectionRequest: (recipientName: String, recipientPhoneE164: String) -> Unit,
    onRespondToConnectionRequest: (request: RoommateConnectionRequest, accept: Boolean) -> Unit,
    onAddHouseholdMember: (name: String, phoneE164: String, upiVpa: String?) -> Unit,
    onCreateSharedExpense: (
        title: String,
        totalInrInput: String,
        paidByName: String,
        splitMethod: SplitMethod,
        participants: List<ParticipantSplit>
    ) -> Unit,
    onConfirmParticipantSettled: (expense: SharedExpense, participantId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPhoneVerifyDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddSplitDialog by remember { mutableStateOf(false) }
    var settleTarget by remember { mutableStateOf<Pair<SharedExpense, ParticipantSplit>?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("roommate_split_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Phone Verification Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isPhoneVerified) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (state.isPhoneVerified) Icons.Default.VerifiedUser else Icons.Default.PhoneAndroid,
                            contentDescription = "Phone Verification Status",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = if (state.isPhoneVerified) {
                                    "Phone Verified (${PhoneValidation.maskPhoneForDisplay(state.verifiedPhoneE164.orEmpty())})"
                                } else {
                                    "Phone Identity Verification"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.isPhoneVerified) {
                                    "Verified and linked to your Fluxora account"
                                } else {
                                    "Verify your phone number for trusted roommate invites"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showPhoneVerifyDialog = true },
                        modifier = Modifier.testTag("open_phone_verify_dialog_button")
                    ) {
                        Text(if (state.isPhoneVerified) "Update" else "Verify Phone")
                    }
                }
            }
        }

        // Roommate Connection Requests (Incoming & Outgoing)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Roommate Connection Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showInviteDialog = true },
                    modifier = Modifier.testTag("send_roommate_invite_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Send Invite", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invite")
                }
            }
        }

        if (state.incomingRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Incoming Requests (${state.incomingRequests.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(state.incomingRequests, key = { it.id }) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${req.senderName} (${PhoneValidation.maskPhoneForDisplay(req.senderPhone)}) invited you",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Status: ${req.status}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (req.status == ConnectionRequestStatus.PENDING.name) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { onRespondToConnectionRequest(req, true) },
                                    modifier = Modifier.testTag("accept_request_${req.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Accept Request",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { onRespondToConnectionRequest(req, false) },
                                    modifier = Modifier.testTag("decline_request_${req.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Decline Request",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.outgoingRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Outgoing Invites (${state.outgoingRequests.size})",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            items(state.outgoingRequests, key = { it.id }) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "To: ${req.recipientName} (${PhoneValidation.maskPhoneForDisplay(req.recipientPhone)})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "If this number is on Fluxora, they'll see your request. (${req.status})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Household Members
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Household Members (${state.householdMembers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { showAddMemberDialog = true },
                    modifier = Modifier.testTag("add_roommate_member_button")
                ) {
                    Icon(Icons.Default.Groups, contentDescription = "Add Roommate", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Member")
                }
            }
        }

        if (state.householdMembers.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = "Add your flatmates or household members with their phone number and UPI ID (e.g., name@okaxis) to split rent, groceries, and utilities.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(state.householdMembers, key = { it.id }) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        val details = listOfNotNull(
                            member.phoneE164.takeIf { it.isNotBlank() }?.let { PhoneValidation.maskPhoneForDisplay(it) },
                            member.upiVpa?.takeIf { it.isNotBlank() }?.let { "UPI: ${PaymentManager.maskVpaForDisplay(it)}" }
                        ).joinToString(" • ")
                        if (details.isNotBlank()) {
                            Text(
                                text = details,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Shared Expenses & Split Ledger
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shared Split Ledger",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddSplitDialog = true },
                    modifier = Modifier.testTag("add_shared_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Shared Expense", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Split Expense")
                }
            }
        }

        if (state.sharedExpenses.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = "No shared bills yet. Tap 'Split Expense' to split bills equally or with custom shares.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(state.sharedExpenses, key = { it.id }) { expense ->
                val splits = expense.parsedSplits()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = expense.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${expense.splitMethod} Split • Paid by ${expense.paidByName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatOrMaskAmount(expense.totalAmountMinor),
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        splits.forEach { split ->
                            val memberUpi = state.householdMembers.find {
                                it.id == split.participantId || it.name == split.participantName
                            }?.upiVpa

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "• ${split.participantName}: ${formatOrMaskAmount(split.shareMinor)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                    Text(
                                        text = if (split.isSettled) {
                                            PaymentManager.SettlementState.CONFIRMED_BY_RECEIVER.displayLabel
                                        } else {
                                            PaymentManager.SettlementState.PENDING.displayLabel
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (split.isSettled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }

                                if (!split.isSettled) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (!memberUpi.isNullOrBlank()) {
                                            OutlinedButton(
                                                onClick = {
                                                    PaymentManager.launchUpiPaymentHandoff(
                                                        context = context,
                                                        request = PaymentManager.UpiPaymentRequest(
                                                            payeeVpa = memberUpi,
                                                            payeeName = split.participantName,
                                                            amountMinor = split.shareMinor,
                                                            transactionNote = "Fluxora Split: ${expense.title}"
                                                        )
                                                    )
                                                    settleTarget = expense to split
                                                },
                                                modifier = Modifier.testTag("upi_pay_button_${expense.id}_${split.participantId}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Payment,
                                                    contentDescription = "UPI Pay",
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("UPI")
                                            }
                                        }
                                        TextButton(
                                            onClick = { settleTarget = expense to split },
                                            modifier = Modifier.testTag("settle_split_button_${expense.id}_${split.participantId}")
                                        ) {
                                            Text("Settle")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPhoneVerifyDialog) {
        var selectedCountry by remember { mutableStateOf(PhoneValidation.DEFAULT_COUNTRY) }
        var nationalNumber by remember { mutableStateOf("") }
        val normalizedE164 = PhoneValidation.normalizeToE164(nationalNumber, selectedCountry)

        AlertDialog(
            onDismissRequest = { showPhoneVerifyDialog = false },
            title = { Text("Phone Identity Verification") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CountryPhoneInputField(
                        selectedCountry = selectedCountry,
                        onCountrySelected = { selectedCountry = it },
                        nationalPhoneInput = nationalNumber,
                        onNationalPhoneChanged = { nationalNumber = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (normalizedE164 != null) {
                            onVerifyAndClaimPhone(normalizedE164)
                            showPhoneVerifyDialog = false
                        }
                    },
                    enabled = normalizedE164 != null,
                    modifier = Modifier.testTag("verify_phone_confirm_button")
                ) {
                    Text("Verify & Bind Phone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneVerifyDialog = false }) { Text("Close") }
            }
        )
    }

    if (showInviteDialog) {
        var recipientName by remember { mutableStateOf("") }
        var selectedCountry by remember { mutableStateOf(PhoneValidation.DEFAULT_COUNTRY) }
        var nationalPhone by remember { mutableStateOf("") }
        val normalizedTarget = PhoneValidation.normalizeToE164(nationalPhone, selectedCountry)

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Send Roommate Connection Invite") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Roommate Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    CountryPhoneInputField(
                        selectedCountry = selectedCountry,
                        onCountrySelected = { selectedCountry = it },
                        nationalPhoneInput = nationalPhone,
                        onNationalPhoneChanged = { nationalPhone = it }
                    )
                    Text(
                        text = "If this number is on Fluxora, they'll see your request.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (normalizedTarget != null) {
                            onSendConnectionRequest(recipientName, normalizedTarget)
                            showInviteDialog = false
                        }
                    },
                    enabled = recipientName.isNotBlank() && normalizedTarget != null
                ) {
                    Text("Send Invite")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddMemberDialog) {
        var name by remember { mutableStateOf("") }
        var selectedCountry by remember { mutableStateOf(PhoneValidation.DEFAULT_COUNTRY) }
        var nationalPhone by remember { mutableStateOf("") }
        var upiVpa by remember { mutableStateOf("") }
        val normalizedPhone = PhoneValidation.normalizeToE164(nationalPhone, selectedCountry)

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Household Member") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.testTag("member_name_input")
                    )
                    CountryPhoneInputField(
                        selectedCountry = selectedCountry,
                        onCountrySelected = { selectedCountry = it },
                        nationalPhoneInput = nationalPhone,
                        onNationalPhoneChanged = { nationalPhone = it }
                    )
                    OutlinedTextField(
                        value = upiVpa,
                        onValueChange = { upiVpa = it },
                        label = { Text("UPI VPA (optional, e.g. rohan@okaxis)") },
                        singleLine = true,
                        modifier = Modifier.testTag("member_upi_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (normalizedPhone != null) {
                            onAddHouseholdMember(name, normalizedPhone, upiVpa.takeIf { it.isNotBlank() })
                            showAddMemberDialog = false
                        }
                    },
                    enabled = name.isNotBlank() && normalizedPhone != null,
                    modifier = Modifier.testTag("save_member_confirm_button")
                ) {
                    Text("Add Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddSplitDialog) {
        var title by remember { mutableStateOf("") }
        var totalMajor by remember { mutableStateOf("") }
        var splitMethod by remember { mutableStateOf(SplitMethod.EQUAL) }
        var showMoreSplitOptions by remember { mutableStateOf(false) }
        val memberPairs = remember(state.householdMembers, state.userId, currentUserDisplayName) {
            val selfPair = (state.userId.ifBlank { "self" }) to (currentUserDisplayName.ifBlank { "Me" })
            val others = state.householdMembers.map { it.id to it.name }
            if (others.isEmpty()) {
                listOf(selfPair, "roommate_default" to "Roommate")
            } else {
                listOf(selfPair) + others
            }
        }

        AlertDialog(
            onDismissRequest = { showAddSplitDialog = false },
            title = { Text("Create Shared Split Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Expense Title (e.g., Monthly Electricity)") },
                        singleLine = true,
                        modifier = Modifier.testTag("split_title_input")
                    )
                    OutlinedTextField(
                        value = totalMajor,
                        onValueChange = { totalMajor = it },
                        label = { Text("Total Amount in INR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.testTag("split_amount_input")
                    )
                    val visibleSplitMethods = if (showMoreSplitOptions) {
                        SplitMethod.entries
                    } else {
                        listOf(SplitMethod.EQUAL)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        visibleSplitMethods.forEach { method ->
                            FilterChip(
                                selected = splitMethod == method,
                                onClick = { splitMethod = method },
                                label = { Text(method.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                        if (!showMoreSplitOptions) {
                            TextButton(
                                onClick = { showMoreSplitOptions = true },
                                modifier = Modifier.testTag("more_split_options_button")
                            ) {
                                Text("More split options", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Text(
                        text = "Splitting across: ${memberPairs.joinToString(", ") { it.second }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalMinor = FinancialEngine.parseInrMajorToMinor(totalMajor) ?: 0L
                        if (totalMinor > 0L) {
                            val shares = FinancialEngine.calculateEqualSplits(totalMinor, memberPairs.size)
                            val participants = memberPairs.mapIndexed { idx, (id, name) ->
                                ParticipantSplit(
                                    participantId = id,
                                    participantName = name,
                                    shareMinor = shares[idx],
                                    isSettled = idx == 0
                                )
                            }
                            onCreateSharedExpense(
                                title,
                                totalMajor,
                                currentUserDisplayName.ifBlank { "Me" },
                                splitMethod,
                                participants
                            )
                            showAddSplitDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && totalMajor.isNotBlank(),
                    modifier = Modifier.testTag("save_split_confirm_button")
                ) {
                    Text("Create Split")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSplitDialog = false }) { Text("Cancel") }
            }
        )
    }

    settleTarget?.let { (expense, split) ->
        AlertDialog(
            onDismissRequest = { settleTarget = null },
            title = { Text("Two-Party Settlement Confirmation") },
            text = {
                Text(
                    "Note: Android UPI intent handoff cannot verify bank clearing automatically. " +
                        "Confirm only after both payer and receiver verify that ${formatOrMaskAmount(split.shareMinor)} for '${expense.title}' (${split.participantName}) has been received."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmParticipantSettled(expense, split.participantId)
                        settleTarget = null
                    },
                    modifier = Modifier.testTag("confirm_verified_settlement_button")
                ) {
                    Text("Confirm Settled (Both Parties)")
                }
            },
            dismissButton = {
                TextButton(onClick = { settleTarget = null }) {
                    Text("Not Yet Paid")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsAndGovernanceScreen(
    currentUserDisplayName: String,
    currentUserEmail: String,
    selectedTheme: AppTheme,
    isDarkMode: Boolean,
    hideSensitiveAmounts: Boolean,
    biometricLockEnabled: Boolean,
    inactivityTimeoutMinutes: Int,
    masterNotificationsEnabled: Boolean,
    categoryToggleSnapshot: Map<NotificationCategory, Boolean>,
    onSelectTheme: (AppTheme) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleHideSensitiveAmounts: () -> Unit,
    onUpdateBiometricSettings: (Boolean, Int) -> Unit,
    onToggleMasterNotifications: (Boolean) -> Unit,
    onToggleCategoryNotification: (NotificationCategory, Boolean) -> Unit,
    onSignOut: () -> Unit,
    isAdvancedMode: Boolean = false,
    isAiAdvisorEnabled: Boolean = true,
    selectedLanguageCode: String = "en",
    onToggleAdvancedMode: (Boolean) -> Unit = {},
    onToggleAiAdvisorEnabled: (Boolean) -> Unit = {},
    onSelectLanguage: (String) -> Unit = {},
    onDeleteAllAccountData: () -> Unit = {},
    themeMode: CinematicThemeMode = CinematicThemeMode.DARK,
    glassPreference: GlassEffectPreference = GlassEffectPreference.AUTO,
    onSelectThemeMode: (CinematicThemeMode) -> Unit = {},
    onSelectGlassPreference: (GlassEffectPreference) -> Unit = {},
    onOpenCinematicDesignSystem: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeoutOptionsMinutes = listOf(1, 5, 15, 30)
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showAccentPickerDialog by remember { mutableStateOf(false) }
    var isAccentPickerSubScreenActive by remember { mutableStateOf(false) }
    var isAdvancedAppearanceExpanded by remember { mutableStateOf(false) }
    var isAdvancedSettingsExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAuthManager = remember(context) { BiometricAuthManager(context) }
    var biometricTestStatus by remember { mutableStateOf<String?>(null) }

    if (isAccentPickerSubScreenActive) {
        BackHandler { isAccentPickerSubScreenActive = false }
        FluxoraAccentPickerSubScreen(
            selectedTheme = selectedTheme,
            onSelectTheme = { theme ->
                onSelectTheme(theme)
                isAccentPickerSubScreenActive = false
            },
            onBack = { isAccentPickerSubScreenActive = false },
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("settings_governance_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --------------------------------------------------------------------
        // 1. ACCOUNT GROUP (Compact Profile Card)
        // --------------------------------------------------------------------
        item {
            com.example.authentication.GoogleSignInAuthComponent(
                isAuthenticated = true,
                userDisplayName = currentUserDisplayName,
                userEmail = currentUserEmail,
                onSignOutClick = onSignOut
            )
        }

        // --------------------------------------------------------------------
        // 2. APPEARANCE GROUP (Theme Mode, Accent Picker, Advanced Appearance)
        // --------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = t("appearance"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = t("appearance"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = t("theme_mode"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CinematicThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onSelectThemeMode(mode) },
                                label = { Text(mode.displayName) },
                                modifier = Modifier.testTag("theme_mode_chip_${mode.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t("dark_mode"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode,
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compact Selected Accent Card + Change Accent Action
                    Text(
                        text = t("accent_color"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(selectedTheme.previewPrimary)
                                )
                                Text(
                                    text = selectedTheme.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            OutlinedButton(
                                onClick = { isAccentPickerSubScreenActive = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("change_accent_button")
                            ) {
                                Text(
                                    text = t("change_accent"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Advanced Appearance Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isAdvancedAppearanceExpanded = !isAdvancedAppearanceExpanded }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isAdvancedAppearanceExpanded) "Hide Advanced Appearance" else "Advanced Appearance Options",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (isAdvancedAppearanceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isAdvancedAppearanceExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "Liquid Glass Surface Quality",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GlassEffectPreference.entries.forEach { pref ->
                                    FilterChip(
                                        selected = glassPreference == pref,
                                        onClick = { onSelectGlassPreference(pref) },
                                        label = { Text(pref.displayName) },
                                        modifier = Modifier.testTag("glass_pref_chip_${pref.name}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onOpenCinematicDesignSystem,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_cinematic_design_system_button")
                            ) {
                                Text("Design Tokens & Accessibility Audit")
                            }
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // 3. PREFERENCES GROUP (Language, Notifications, Security & Advanced)
        // --------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = t("preferences"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = t("preferences"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language Picker (EN, HI, ES, FR)
                    Text(
                        text = "App Language / भाषा / Idioma / Langue",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.i18n.SupportedLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = selectedLanguageCode.lowercase() == lang.code,
                                onClick = { onSelectLanguage(lang.code) },
                                label = { Text(lang.nativeName) },
                                modifier = Modifier.testTag("language_chip_${lang.code}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Master Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notifications", fontWeight = FontWeight.Medium)
                            Text(
                                text = "Receive bill reminders and transaction alerts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = masterNotificationsEnabled,
                            onCheckedChange = onToggleMasterNotifications,
                            modifier = Modifier.testTag("master_notifications_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mask Sensitive Currency Amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mask Sensitive Amounts", fontWeight = FontWeight.Medium)
                            Text(
                                text = "Replaces INR balances with INR •••••• across screens",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hideSensitiveAmounts,
                            onCheckedChange = { onToggleHideSensitiveAmounts() },
                            modifier = Modifier.testTag("hide_sensitive_amounts_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Biometric Lock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric App Lock", fontWeight = FontWeight.Medium)
                            Text(
                                text = "Requires fingerprint, face, or device PIN after inactivity",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = biometricLockEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateBiometricSettings(enabled, inactivityTimeoutMinutes)
                            },
                            modifier = Modifier.testTag("biometric_lock_switch")
                        )
                    }

                    if (biometricLockEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inactivity Lock Timeout",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            timeoutOptionsMinutes.forEach { mins ->
                                FilterChip(
                                    selected = inactivityTimeoutMinutes == mins,
                                    onClick = { onUpdateBiometricSettings(true, mins) },
                                    label = { Text("${mins}m") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                if (activity != null) {
                                    biometricAuthManager.promptBiometricAuth(
                                        activity = activity,
                                        title = "Test Biometric Prompt",
                                        subtitle = "Fingerprint and Face Scan verification",
                                        description = "Biometrics are correctly configured on your device",
                                        onSuccess = { biometricTestStatus = "Biometric authentication verified successfully!" },
                                        onError = { biometricTestStatus = "Biometric prompt: $it" }
                                    )
                                } else {
                                    biometricTestStatus = "Biometric prompt verified (preview mode)"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_biometric_prompt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Biometric Prompt (Fingerprint / Face)")
                        }

                        if (biometricTestStatus != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = biometricTestStatus!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("biometric_test_status_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Expandable Advanced Settings Section (Collapsed by Default)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isAdvancedSettingsExpanded = !isAdvancedSettingsExpanded }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isAdvancedSettingsExpanded) "Hide Advanced Settings" else "Advanced Settings",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (isAdvancedSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isAdvancedSettingsExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            // Advanced Mode Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Advanced Mode", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Unlocks granular features and technical telemetry",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isAdvancedMode,
                                    onCheckedChange = onToggleAdvancedMode,
                                    modifier = Modifier.testTag("simple_advanced_mode_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // AI Financial Advisor Consent
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("AI Advisor Category Aggregates", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Shares aggregate totals for smart financial insights",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isAiAdvisorEnabled,
                                    onCheckedChange = onToggleAiAdvisorEnabled,
                                    modifier = Modifier.testTag("ai_consent_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Notification Category Granular Controls
                            Text(
                                text = "Notification Category Controls",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            NotificationCategory.entries.forEach { cat ->
                                val isChecked = categoryToggleSnapshot[cat] ?: true
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { onToggleCategoryNotification(cat, it) },
                                        modifier = Modifier.testTag("notif_switch_${cat.name}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Delete Account Data Button
                            OutlinedButton(
                                onClick = { showDeleteAccountConfirmDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("delete_account_data_button")
                            ) {
                                Text(
                                    text = "Delete Account & Financial Data",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Legal & Privacy Governance (8 Sections + Contact Email)
                            LegalComplianceSectionView()

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mandatory Authors Section
                            AuthorsScreenSection()
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // ACCENT PICKER DIALOG (Preserves all 8 AppTheme options & test tags)
    // ------------------------------------------------------------------------
    if (showAccentPickerDialog) {
        AlertDialog(
            onDismissRequest = { showAccentPickerDialog = false },
            title = {
                Text(
                    text = "Select Accent Color",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose your preferred primary accent theme for Fluxora.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            val selected = selectedTheme == theme
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                modifier = Modifier
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        onSelectTheme(theme)
                                        showAccentPickerDialog = false
                                    }
                                    .testTag("theme_option_${theme.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(theme.previewPrimary)
                                    )
                                    Text(
                                        text = theme.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccentPickerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
            title = { Text("Delete Account & Financial Data") },
            text = {
                Text(
                    "This permanently deletes your transactions, budgets, savings goals, recurring bills, " +
                        "shared splits, and releases your verified phone number in compliance with the DPDP Act."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllAccountData()
                        showDeleteAccountConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_delete_account_data_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
}

/**
 * Dedicated Sub-Screen for Accent Color Selection.
 * Completely declutters the main Settings view by presenting theme selections in an independent view.
 */
@Composable
fun FluxoraAccentPickerSubScreen(
    selectedTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("accent_picker_subscreen")
    ) {
        // Sub-screen Header with Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("accent_picker_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column {
                Text(
                    text = "Accent Color Themes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select your preferred primary accent theme for Fluxora",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(AppTheme.entries) { theme ->
                val selected = selectedTheme == theme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectTheme(theme) }
                        .testTag("theme_option_${theme.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(theme.previewPrimary)
                            )
                            Column {
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                )
                                Text(
                                    text = if (selected) "Active Theme" else "Tap to apply",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
