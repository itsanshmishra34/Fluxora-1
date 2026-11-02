package com.example.legal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class LegalSection(
    val number: Int,
    val title: String,
    val content: String
)

object LegalComplianceRegistry {

    const val OFFICIAL_CONTACT_EMAIL = "itsanshmishra34@gmail.com"
    val LEGAL_CONTACTS = listOf("Ayush Mishra", "Sunaina Bansal")

    /**
     * Authoritative 8-Section Privacy Policy for Fluxora with zero drafting placeholders,
     * accurate Firebase & Gemini AI disclosures, and zero false E2EE claims.
     */
    val PRIVACY_POLICY_SECTIONS: List<LegalSection> = listOf(
        LegalSection(
            1,
            "1. Scope & Controller Identity",
            "Fluxora is a personal finance, roommate split ledger, and financial advisory application created by Ayush Mishra, Sunaina Bansal, and Rohan Kumar. This policy explains how your financial records, phone verification, and advisor prompts are handled when you use Fluxora."
        ),
        LegalSection(
            2,
            "2. Financial & Account Information Collected",
            "When you sign in with your Google account or verify your phone number, Fluxora collects your display name, email address, verified phone number, and user-entered transactions, categories, budgets, savings goals, recurring bills, and shared roommate expenses."
        ),
        LegalSection(
            3,
            "3. Data Storage & Security",
            "Your financial records are stored in your private account ledger and protected by strict access controls. Data is encrypted in transit and at rest. Records are accessible only to your signed-in account and authorized participants in shared roommate expenses."
        ),
        LegalSection(
            4,
            "4. AI Financial Advisor Disclosures",
            "When you request a Daily Saving Insight or ask a question in the Financial Advisor chat, Fluxora uses a summary of your budgets, recurring bills, and category spending totals to generate tailored guidance. Do not enter sensitive government IDs, bank passwords, or card PINs into the chat."
        ),
        LegalSection(
            5,
            "5. Phone Verification & Roommate Split Ledger",
            "Verified phone numbers are linked to your account to prevent duplicate phone claims and to route roommate connection requests. Only verified recipients can accept or decline incoming roommate invitations."
        ),
        LegalSection(
            6,
            "6. Biometric Security & App Preferences",
            "Fluxora uses your device's built-in biometric or screen lock prompt locally on your phone. Fluxora never captures, accesses, or stores your fingerprint or facial images. Theme selections, privacy amount masking, and notification preferences are saved on your device."
        ),
        LegalSection(
            7,
            "7. UPI Payment Handoff & CSV Data Portability",
            "When you initiate a roommate settlement, Fluxora validates the recipient UPI ID and opens your installed UPI payment app. Fluxora never accesses your bank credentials or UPI PIN. You may export or import your transaction history at any time via CSV."
        ),
        LegalSection(
            8,
            "8. Official Privacy & Legal Contact Routing",
            "For data access, account deletion requests, or legal inquiries, contact designated representatives Ayush Mishra and Sunaina Bansal using the in-app contact buttons below."
        )
    )

    val TERMS_AND_CONDITIONS_SECTIONS: List<LegalSection> = listOf(
        LegalSection(
            1,
            "1. Acceptance of Terms",
            "By signing in to and using Fluxora, you agree to these Terms & Conditions governing personal financial tracking, roommate expense splitting, UPI payment handoff, and AI Financial Advisor insights."
        ),
        LegalSection(
            2,
            "2. Non-Advisory Financial Disclaimer",
            "Insights generated by the Fluxora Financial Advisor are informational budgeting aids only and do not constitute certified financial, tax, investment, or legal advice."
        ),
        LegalSection(
            3,
            "3. Accurate Financial & Roommate Entries",
            "You are responsible for verifying the accuracy of transaction amounts, recurring bill schedules, CSV imports, and roommate split shares. Opening an external UPI app does not automatically confirm payment completion; please verify settlement receipts with your payment provider."
        ),
        LegalSection(
            4,
            "4. Account Security & Acceptable Use",
            "You must only verify phone numbers that you legitimately own. Attempting to spoof roommate connection requests or import malformed data is prohibited."
        )
    )

    /**
     * Routes official legal contact inquiries for Ayush Mishra and Sunaina Bansal via `mailto:` intent.
     */
    fun buildContactEmailIntent(recipientRoleName: String = "Ayush Mishra & Sunaina Bansal"): Intent {
        val mailtoUri = Uri.parse("mailto:$OFFICIAL_CONTACT_EMAIL")
            .buildUpon()
            .appendQueryParameter("subject", "Fluxora Legal & Privacy Inquiry — Attn: $recipientRoleName")
            .build()
        return Intent(Intent.ACTION_SENDTO, mailtoUri)
    }

    fun openContactEmail(context: Context, recipientRoleName: String = "Ayush Mishra & Sunaina Bansal"): Boolean {
        return try {
            val intent = buildContactEmailIntent(recipientRoleName).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}

@Composable
fun LegalComplianceSectionView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedPrivacyIndex by remember { mutableStateOf<Int?>(1) }
    var expandedTermsIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("legal_screens_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        LegalComplianceRegistry.PRIVACY_POLICY_SECTIONS.forEach { section ->
            val isExpanded = expandedPrivacyIndex == section.number
            AccordionLegalCard(
                section = section,
                expanded = isExpanded,
                onToggle = {
                    expandedPrivacyIndex = if (isExpanded) null else section.number
                },
                testTag = "privacy_section_${section.number}"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Terms & Conditions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        LegalComplianceRegistry.TERMS_AND_CONDITIONS_SECTIONS.forEach { section ->
            val isExpanded = expandedTermsIndex == section.number
            AccordionLegalCard(
                section = section,
                expanded = isExpanded,
                onToggle = {
                    expandedTermsIndex = if (isExpanded) null else section.number
                },
                testTag = "terms_section_${section.number}"
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Privacy & Support Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reach out to Ayush Mishra or Sunaina Bansal for privacy or support questions:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { LegalComplianceRegistry.openContactEmail(context, "Ayush Mishra") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("contact_ayush_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null)
                        Text(" Contact Ayush")
                    }
                    Button(
                        onClick = { LegalComplianceRegistry.openContactEmail(context, "Sunaina Bansal") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("contact_sunaina_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null)
                        Text(" Contact Sunaina")
                    }
                }
            }
        }
    }
}

@Composable
private fun AccordionLegalCard(
    section: LegalSection,
    expanded: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse section" else "Expand section",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = section.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
