package com.example.authentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * International Phone Input Field with Country Code Search & Selection (defaulting to India +91),
 * national digit extraction, and live E.164 normalization preview.
 */
@Composable
fun CountryPhoneInputField(
    selectedCountry: CountryDialCode,
    onCountrySelected: (CountryDialCode) -> Unit,
    nationalPhoneInput: String,
    onNationalPhoneChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Mobile Number"
) {
    var showCountryPickerDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val normalizedPreview = remember(selectedCountry, nationalPhoneInput) {
        PhoneValidation.normalizeToE164(nationalPhoneInput, selectedCountry)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .height(56.dp)
                    .clickable { showCountryPickerDialog = true }
                    .testTag("country_code_selector"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedCountry.isoCode} ${selectedCountry.dialCode}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select country code"
                    )
                }
            }

            OutlinedTextField(
                value = nationalPhoneInput,
                onValueChange = { raw ->
                    val extracted = PhoneValidation.extractNationalDigits(raw, selectedCountry.dialCode)
                    onNationalPhoneChanged(extracted)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("phone_national_input"),
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (nationalPhoneInput.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (normalizedPreview != null) {
                    "Verified Number: $normalizedPreview"
                } else {
                    "Enter ${selectedCountry.expectedLengthRange.first} valid national digits for ${selectedCountry.countryName} (${selectedCountry.dialCode})"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (normalizedPreview != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }

    if (showCountryPickerDialog) {
        val filteredCountries = remember(searchQuery) {
            PhoneValidation.searchCountries(searchQuery)
        }
        AlertDialog(
            onDismissRequest = { showCountryPickerDialog = false },
            title = { Text("Select Country Dial Code") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("country_search_input"),
                        placeholder = { Text("Search India, +91, US...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        items(filteredCountries, key = { it.isoCode }) { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCountrySelected(country)
                                        showCountryPickerDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${country.countryName} (${country.isoCode})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = country.dialCode,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryPickerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
