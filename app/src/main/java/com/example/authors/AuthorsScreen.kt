package com.example.authors

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FluxoraAuthor(
    val id: String,
    val fullName: String,
    val linkedInUrl: String
)

object AuthorsRegistry {

    /**
     * All three mandatory Fluxora authors with their exact verified LinkedIn URLs.
     * Zero fabricated titles or backgrounds.
     */
    val AUTHORS: List<FluxoraAuthor> = listOf(
        FluxoraAuthor(
            id = "ayush_mishra",
            fullName = "Ayush Mishra",
            linkedInUrl = "https://www.linkedin.com/in/ayush-mishra-12b36a37a/"
        ),
        FluxoraAuthor(
            id = "sunaina_bansal",
            fullName = "Sunaina Bansal",
            linkedInUrl = "https://www.linkedin.com/in/sunaina-bansal-b5107a374/"
        ),
        FluxoraAuthor(
            id = "rohan_kumar",
            fullName = "Rohan Kumar",
            linkedInUrl = "https://www.linkedin.com/in/rohan-16kumar"
        )
    )

    fun buildLinkedInIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()))
    }

    fun openLinkedInProfile(context: Context, url: String): Boolean {
        return try {
            val intent = buildLinkedInIntent(url).apply {
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
fun AuthorsScreenSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("authors_screen_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Authors & Credits",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        AuthorsRegistry.AUTHORS.forEach { author ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("author_card_${author.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = author.fullName,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = author.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Verified LinkedIn Profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = { AuthorsRegistry.openLinkedInProfile(context, author.linkedInUrl) },
                        modifier = Modifier.testTag("author_linkedin_btn_${author.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LinkedIn")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open LinkedIn profile for ${author.fullName}",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
