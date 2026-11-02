package com.example.authentication

data class CountryDialCode(
    val isoCode: String,
    val countryName: String,
    val dialCode: String,
    val expectedLengthRange: IntRange = 7..12
)

object PhoneValidation {

    val DEFAULT_COUNTRY = CountryDialCode(
        isoCode = "IN",
        countryName = "India",
        dialCode = "+91",
        expectedLengthRange = 10..10
    )

    val SUPPORTED_COUNTRIES: List<CountryDialCode> = listOf(
        DEFAULT_COUNTRY,
        CountryDialCode("US", "United States", "+1", 10..10),
        CountryDialCode("GB", "United Kingdom", "+44", 10..10),
        CountryDialCode("CA", "Canada", "+1", 10..10),
        CountryDialCode("AU", "Australia", "+61", 9..9),
        CountryDialCode("SG", "Singapore", "+65", 8..8),
        CountryDialCode("AE", "United Arab Emirates", "+971", 9..9),
        CountryDialCode("DE", "Germany", "+49", 10..11),
        CountryDialCode("FR", "France", "+33", 9..9),
        CountryDialCode("JP", "Japan", "+81", 10..10),
        CountryDialCode("BR", "Brazil", "+55", 10..11),
        CountryDialCode("ZA", "South Africa", "+27", 9..9),
        CountryDialCode("NZ", "New Zealand", "+64", 8..10),
        CountryDialCode("MY", "Malaysia", "+60", 9..10),
        CountryDialCode("ID", "Indonesia", "+62", 9..12)
    )

    private val E164_REGEX = Regex("^\\+[1-9][0-9]{6,14}$")

    /**
     * Searches supported countries by country name, ISO code, or dial code.
     */
    fun searchCountries(query: String): List<CountryDialCode> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return SUPPORTED_COUNTRIES
        return SUPPORTED_COUNTRIES.filter { country ->
            country.countryName.contains(trimmed, ignoreCase = true) ||
                country.isoCode.contains(trimmed, ignoreCase = true) ||
                country.dialCode.contains(trimmed)
        }
    }

    /**
     * Extracts strictly the national digits from a user-entered phone input.
     * If the user pasted a full E.164 string starting with the selected [countryDialCode],
     * strips the dial code prefix and leading trunk zero before returning digits.
     */
    fun extractNationalDigits(rawInput: String, countryDialCode: String = "+91"): String {
        val trimmed = rawInput.trim()
        val dialDigits = countryDialCode.filter { it.isDigit() }
        val withoutDialPrefix = if (trimmed.startsWith(countryDialCode)) {
            trimmed.removePrefix(countryDialCode)
        } else if (trimmed.startsWith("+$dialDigits")) {
            trimmed.removePrefix("+$dialDigits")
        } else {
            trimmed
        }
        val digitsOnly = withoutDialPrefix.filter { it.isDigit() }
        return if (countryDialCode == "+91" && digitsOnly.length == 11 && digitsOnly.startsWith("0")) {
            digitsOnly.drop(1)
        } else {
            digitsOnly
        }
    }

    /**
     * Normalizes a national phone input and country dial code into strict E.164 format (e.g., `+919876543210`).
     * Returns null if invalid.
     */
    fun normalizeToE164(rawInput: String, country: CountryDialCode = DEFAULT_COUNTRY): String? {
        val trimmed = rawInput.trim()
        if (trimmed.startsWith("+") && !trimmed.startsWith(country.dialCode)) {
            // Direct E.164 input from another country
            val compact = "+" + trimmed.drop(1).filter { it.isDigit() }
            return if (isValidE164Phone(compact)) compact else null
        }

        val nationalDigits = extractNationalDigits(trimmed, country.dialCode)
        if (nationalDigits.length !in country.expectedLengthRange) {
            return null
        }
        if (country.isoCode == "IN" && nationalDigits.firstOrNull() !in listOf('6', '7', '8', '9')) {
            return null
        }
        val cleanDial = "+" + country.dialCode.filter { it.isDigit() }
        val candidate = "$cleanDial$nationalDigits"
        return if (isValidE164Phone(candidate)) candidate else null
    }

    fun isValidE164Phone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        return E164_REGEX.matches(phone.trim())
    }

    /**
     * Formats an E.164 phone number in masked display form (e.g. `+919876543210` -> `+91 98••••••10`)
     * so full phone numbers are not exposed on shared or overview screens.
     */
    fun maskPhoneForDisplay(e164Phone: String?): String {
        val trimmed = e164Phone?.trim().orEmpty()
        if (trimmed.isEmpty()) return ""
        val matchedCountry = SUPPORTED_COUNTRIES.firstOrNull { trimmed.startsWith(it.dialCode) }
        if (matchedCountry != null) {
            val national = trimmed.removePrefix(matchedCountry.dialCode)
            if (national.length >= 4) {
                val firstTwo = national.take(2)
                val lastTwo = national.takeLast(2)
                val maskedMiddle = "•".repeat((national.length - 4).coerceAtLeast(2))
                return "${matchedCountry.dialCode} $firstTwo$maskedMiddle$lastTwo"
            }
        }
        if (trimmed.length >= 6) {
            val prefix = trimmed.take(3)
            val suffix = trimmed.takeLast(2)
            val dots = "•".repeat((trimmed.length - 5).coerceAtLeast(2))
            return "$prefix $dots$suffix"
        }
        return trimmed
    }

    /**
     * Computes the lowercase SHA-256 hex hash of a normalized E.164 phone number for privacy-preserving lookup.
     */
    fun hashPhoneE164(e164Phone: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(e164Phone.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
