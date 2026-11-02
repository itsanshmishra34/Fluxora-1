package com.example

import com.example.authentication.PhoneValidation
import com.example.authors.AuthorsRegistry
import com.example.data.FinancialTransaction
import com.example.finance.CsvDataPortability
import com.example.finance.DateValidator
import com.example.finance.FinancialEngine
import com.example.legal.LegalComplianceRegistry
import com.example.payments.PaymentManager
import com.example.roommate.ConnectionRequestStatus
import com.example.roommate.RoommateConnectionRequest
import com.example.roommate.RoommateRequestSecurityValidator
import com.example.ui.theme.AppTheme
import com.example.ui.theme.toColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    // --- 1. Indian Currency Formatting Tests (12 tests) ---

    @Test
    fun `formatIndianCurrency formats zero paise`() {
        assertEquals("INR 0.00", FinancialEngine.formatIndianCurrency(0L))
    }

    @Test
    fun `formatIndianCurrency formats sub-rupee paise`() {
        assertEquals("INR 0.05", FinancialEngine.formatIndianCurrency(5L))
        assertEquals("INR 0.99", FinancialEngine.formatIndianCurrency(99L))
    }

    @Test
    fun `formatIndianCurrency formats hundreds without comma`() {
        assertEquals("INR 999.00", FinancialEngine.formatIndianCurrency(99_900L))
    }

    @Test
    fun `formatIndianCurrency formats thousands with first comma at 3 digits`() {
        assertEquals("INR 1,000.00", FinancialEngine.formatIndianCurrency(100_000L))
        assertEquals("INR 99,999.50", FinancialEngine.formatIndianCurrency(9_999_950L))
    }

    @Test
    fun `formatIndianCurrency formats 1 Lakh with Indian grouping`() {
        assertEquals("INR 1,00,000.00", FinancialEngine.formatIndianCurrency(10_000_000L))
    }

    @Test
    fun `formatIndianCurrency formats 12 Lakh 34 Thousand 567 Rupees 89 Paise`() {
        assertEquals("INR 12,34,567.89", FinancialEngine.formatIndianCurrency(123_456_789L))
    }

    @Test
    fun `formatIndianCurrency formats 1 Crore with Indian grouping`() {
        assertEquals("INR 1,00,00,000.00", FinancialEngine.formatIndianCurrency(1_000_000_000L))
    }

    @Test
    fun `formatIndianCurrency formats multi-Crore amounts`() {
        assertEquals("INR 98,76,54,321.10", FinancialEngine.formatIndianCurrency(98_765_432_110L))
    }

    @Test
    fun `formatIndianCurrency formats negative amounts with minus prefix`() {
        assertEquals("-INR 1,50,000.00", FinancialEngine.formatIndianCurrency(-15_000_000L))
    }

    @Test
    fun `formatIndianCurrency appends plus prefix when showSign is true`() {
        assertEquals("+INR 2,500.00", FinancialEngine.formatIndianCurrency(250_000L, showSign = true))
    }

    @Test
    fun `parseInrMajorToMinor parses valid rupee strings to paise`() {
        assertEquals(125_050L, FinancialEngine.parseInrMajorToMinor("1250.50"))
        assertEquals(10_000_000L, FinancialEngine.parseInrMajorToMinor("INR 1,00,000"))
    }

    @Test
    fun `parseInrMajorToMinor rejects negative or non-numeric strings`() {
        assertNull(FinancialEngine.parseInrMajorToMinor(""))
        assertNull(FinancialEngine.parseInrMajorToMinor("-500"))
        assertNull(FinancialEngine.parseInrMajorToMinor("abc"))
    }

    // --- 2. Split Calculation Engine Tests (10 tests) ---

    @Test
    fun `calculateEqualSplits divides evenly when divisible`() {
        val splits = FinancialEngine.calculateEqualSplits(90_000L, 3)
        assertEquals(listOf(30_000L, 30_000L, 30_000L), splits)
        assertEquals(90_000L, splits.sum())
    }

    @Test
    fun `calculateEqualSplits distributes paise remainder deterministically without loss`() {
        val splits = FinancialEngine.calculateEqualSplits(10_000L, 3)
        assertEquals(listOf(3_334L, 3_333L, 3_333L), splits)
        assertEquals(10_000L, splits.sum())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculateEqualSplits throws for zero amount`() {
        FinancialEngine.calculateEqualSplits(0L, 2)
    }

    @Test
    fun `validateExactSplits succeeds when exact shares sum to total`() {
        assertTrue(FinancialEngine.validateExactSplits(100_000L, listOf(60_000L, 40_000L)))
    }

    @Test
    fun `validateExactSplits fails when sum does not match total`() {
        assertFalse(FinancialEngine.validateExactSplits(100_000L, listOf(50_000L, 40_000L)))
    }

    @Test
    fun `validateExactSplits fails on negative member share`() {
        assertFalse(FinancialEngine.validateExactSplits(100_000L, listOf(120_000L, -20_000L)))
    }

    @Test
    fun `calculatePercentageSplits allocates exact paise when percentages sum to 100`() {
        val splits = FinancialEngine.calculatePercentageSplits(100_000L, listOf(33.33, 33.33, 33.34))
        assertEquals(100_000L, splits.sum())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculatePercentageSplits fails when percentages do not sum to 100`() {
        FinancialEngine.calculatePercentageSplits(100_000L, listOf(50.0, 30.0))
    }

    @Test
    fun `calculateShareWeightSplits allocates proportionally by integer weights`() {
        val splits = FinancialEngine.calculateShareWeightSplits(100_000L, listOf(2, 1, 1))
        assertEquals(listOf(50_000L, 25_000L, 25_000L), splits)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `calculateShareWeightSplits fails when weight is zero or negative`() {
        FinancialEngine.calculateShareWeightSplits(100_000L, listOf(0, 1))
    }

    // --- 3. DateValidator Tests (5 tests) ---

    @Test
    fun `DateValidator accepts current timestamp`() {
        val now = 1_730_000_000_000L
        assertTrue(DateValidator.validateTransactionDate(now, now).isValid)
    }

    @Test
    fun `DateValidator accepts past timestamp`() {
        val now = 1_730_000_000_000L
        val yesterday = now - 24L * 3600L * 1000L
        assertTrue(DateValidator.validateTransactionDate(yesterday, now).isValid)
    }

    @Test
    fun `DateValidator rejects future date beyond end of today`() {
        val now = 1_730_000_000_000L
        val nextWeek = now + 7L * 24L * 3600L * 1000L
        assertFalse(DateValidator.validateTransactionDate(nextWeek, now).isValid)
    }

    @Test
    fun `DateValidator rejects far-future date beyond 1 year`() {
        val now = 1_730_000_000_000L
        val twoYearsAhead = now + 730L * 24L * 3600L * 1000L
        val res = DateValidator.validateTransactionDate(twoYearsAhead, now)
        assertFalse(res.isValid)
        assertNotNull(res.errorMessage)
    }

    @Test
    fun `DateValidator rejects pre-1970 non-positive timestamp`() {
        val res = DateValidator.validateTransactionDate(-1000L, 1_730_000_000_000L)
        assertFalse(res.isValid)
    }

    // --- 4. All 8 Signature Themes Individual Palette Verification (8 tests) ---

    @Test
    fun `Midnight Emerald theme has valid dark and light color schemes`() {
        assertEquals("Midnight Emerald", AppTheme.MIDNIGHT_EMERALD.displayName)
        assertNotNull(AppTheme.MIDNIGHT_EMERALD.toColorScheme(true).primary)
        assertNotNull(AppTheme.MIDNIGHT_EMERALD.toColorScheme(false).primary)
    }

    @Test
    fun `Cyber Violet theme has valid dark and light color schemes`() {
        assertEquals("Cyber Violet", AppTheme.CYBER_VIOLET.displayName)
        assertNotNull(AppTheme.CYBER_VIOLET.toColorScheme(true).primary)
        assertNotNull(AppTheme.CYBER_VIOLET.toColorScheme(false).primary)
    }

    @Test
    fun `Solar Gold theme has valid dark and light color schemes`() {
        assertEquals("Solar Gold", AppTheme.SOLAR_GOLD.displayName)
        assertNotNull(AppTheme.SOLAR_GOLD.toColorScheme(true).primary)
        assertNotNull(AppTheme.SOLAR_GOLD.toColorScheme(false).primary)
    }

    @Test
    fun `Arctic Cyan theme has valid dark and light color schemes`() {
        assertEquals("Arctic Cyan", AppTheme.ARCTIC_CYAN.displayName)
        assertNotNull(AppTheme.ARCTIC_CYAN.toColorScheme(true).primary)
        assertNotNull(AppTheme.ARCTIC_CYAN.toColorScheme(false).primary)
    }

    @Test
    fun `Crimson Pulse theme has valid dark and light color schemes`() {
        assertEquals("Crimson Pulse", AppTheme.CRIMSON_PULSE.displayName)
        assertNotNull(AppTheme.CRIMSON_PULSE.toColorScheme(true).primary)
        assertNotNull(AppTheme.CRIMSON_PULSE.toColorScheme(false).primary)
    }

    @Test
    fun `Obsidian Rose theme has valid dark and light color schemes`() {
        assertEquals("Obsidian Rose", AppTheme.OBSIDIAN_ROSE.displayName)
        assertNotNull(AppTheme.OBSIDIAN_ROSE.toColorScheme(true).primary)
        assertNotNull(AppTheme.OBSIDIAN_ROSE.toColorScheme(false).primary)
    }

    @Test
    fun `Aurora Borealis theme has valid dark and light color schemes`() {
        assertEquals("Aurora Borealis", AppTheme.AURORA_BOREALIS.displayName)
        assertNotNull(AppTheme.AURORA_BOREALIS.toColorScheme(true).primary)
        assertNotNull(AppTheme.AURORA_BOREALIS.toColorScheme(false).primary)
    }

    @Test
    fun `Monochrome Slate theme has valid dark and light color schemes`() {
        assertEquals("Monochrome Slate", AppTheme.MONOCHROME_SLATE.displayName)
        assertNotNull(AppTheme.MONOCHROME_SLATE.toColorScheme(true).primary)
        assertNotNull(AppTheme.MONOCHROME_SLATE.toColorScheme(false).primary)
    }

    // --- 5. Additional Security, CSV, Legal & Authors Unit Invariants (12 tests) ---

    @Test
    fun `csv sanitizeAndEscapeCsvCell escapes internal double quotes`() {
        val sanitized = CsvDataPortability.sanitizeAndEscapeCsvCell("Lunch at \"Taj\"")
        assertEquals("\"Lunch at \"\"Taj\"\"\"", sanitized)
    }

    @Test
    fun `csv round trip preserves clean normal titles`() {
        val tx = FinancialTransaction(
            id = "tx_rt_1",
            userId = "u1",
            title = "Electricity Bill",
            amountMinor = 145_000L,
            type = "EXPENSE",
            categoryId = "bills_utilities",
            categoryName = "Bills & Utilities",
            timestampMillis = 1730000000000L,
            notes = "October cycle"
        )
        val exported = CsvDataPortability.exportToCsv(listOf(tx))
        val imported = CsvDataPortability.importFromCsv(exported, userId = "u1", nowMillis = 1730010000000L)
        assertEquals(1, imported.importedTransactions.size)
        assertEquals("Electricity Bill", imported.importedTransactions.first().title)
        assertEquals(145_000L, imported.importedTransactions.first().amountMinor)
    }

    @Test
    fun `PhoneValidation extractNationalDigits strips non-digits and country dial prefix`() {
        val digits = PhoneValidation.extractNationalDigits("+91 (987) 654-3210", countryDialCode = "+91")
        assertEquals("9876543210", digits)
    }

    @Test
    fun `PhoneValidation supports US +1 10-digit normalization`() {
        val us = PhoneValidation.SUPPORTED_COUNTRIES.first { it.isoCode == "US" }
        assertEquals("+14155552671", PhoneValidation.normalizeToE164("415-555-2671", us))
    }

    @Test
    fun `RoommateRequestSecurityValidator rejects third-party actor transitions`() {
        val req = RoommateConnectionRequest(
            id = "req_tp",
            senderUid = "uid_alice",
            senderPhone = "+919876543210",
            recipientUid = "uid_bob",
            recipientPhone = "+919876543211",
            status = ConnectionRequestStatus.PENDING.name
        )
        val res = RoommateRequestSecurityValidator.validateStatusTransition(
            request = req,
            actorUid = "uid_mallory",
            actorVerifiedPhoneE164 = "+919876543299",
            isActorPhoneVerified = true,
            targetStatus = ConnectionRequestStatus.ACCEPTED
        )
        assertTrue(res.isFailure)
    }

    @Test
    fun `RoommateRequestSecurityValidator rejects unverified sender on creation`() {
        val res = RoommateRequestSecurityValidator.validateNewRequest(
            senderUid = "uid_alice",
            senderName = "Alice",
            senderVerifiedPhoneE164 = null,
            isSenderPhoneVerified = false,
            recipientUid = "uid_bob",
            recipientName = "Bob",
            recipientPhoneE164 = "+919876543211",
            existingRequests = emptyList()
        )
        assertTrue(res.isFailure)
    }

    @Test
    fun `PaymentManager rejects amount exceeding 1 Lakh UPI per-transaction limit`() {
        val overLimit = PaymentManager.validatePaymentRequest(
            payeeVpa = "merchant@okaxis",
            amountMinor = 15_000_000L // INR 1,50,000.00 > INR 1,00,000.00
        )
        assertFalse(overLimit.isValid)
    }

    @Test
    fun `Legal section 1 covers Scope and Controller Identity`() {
        val s1 = LegalComplianceRegistry.PRIVACY_POLICY_SECTIONS.first()
        assertEquals(1, s1.number)
        assertTrue(s1.title.contains("Scope"))
    }

    @Test
    fun `Legal section 8 covers Official Privacy and Legal Contact Routing`() {
        val s8 = LegalComplianceRegistry.PRIVACY_POLICY_SECTIONS.last()
        assertEquals(8, s8.number)
        assertTrue(s8.content.contains("Ayush Mishra"))
        assertTrue(s8.content.contains("Sunaina Bansal"))
    }

    @Test
    fun `Author Ayush Mishra profile has verified LinkedIn URL`() {
        val author = AuthorsRegistry.AUTHORS[0]
        assertEquals("Ayush Mishra", author.fullName)
        assertEquals("https://www.linkedin.com/in/ayush-mishra-12b36a37a/", author.linkedInUrl)
    }

    @Test
    fun `Author Sunaina Bansal profile has verified LinkedIn URL`() {
        val author = AuthorsRegistry.AUTHORS[1]
        assertEquals("Sunaina Bansal", author.fullName)
        assertEquals("https://www.linkedin.com/in/sunaina-bansal-b5107a374/", author.linkedInUrl)
    }

    @Test
    fun `Author Rohan Kumar profile has verified LinkedIn URL`() {
        val author = AuthorsRegistry.AUTHORS[2]
        assertEquals("Rohan Kumar", author.fullName)
        assertEquals("https://www.linkedin.com/in/rohan-16kumar", author.linkedInUrl)
    }

    // --- 6. Fluxora v2 Money Value Class & CurrencyStyle Tests ---

    @Test
    fun `Money value class performs safe addition subtraction and multiplication`() {
        val m1 = com.example.finance.Money(3_905_000L)
        val m2 = com.example.finance.Money(100_000L)
        assertEquals(4_005_000L, (m1 + m2).amountMinor)
        assertEquals(3_805_000L, (m1 - m2).amountMinor)
        assertEquals(7_810_000L, (m1 * 2).amountMinor)
    }

    @Test(expected = ArithmeticException::class)
    fun `Money value class detects Long addition overflow`() {
        val maxMoney = com.example.finance.Money(Long.MAX_VALUE)
        maxMoney + com.example.finance.Money(1L)
    }

    @Test
    fun `formatIndianCurrency supports SYMBOL style and Money format helper`() {
        val money = com.example.finance.Money(3_905_000L)
        assertEquals(
            "INR 39,050.00",
            FinancialEngine.formatIndianCurrency(money, style = com.example.finance.CurrencyStyle.ISO_PREFIX)
        )
        assertEquals(
            "₹39,050.00",
            FinancialEngine.formatIndianCurrency(money, style = com.example.finance.CurrencyStyle.SYMBOL)
        )
        assertEquals(
            "-₹1,50,000.00",
            FinancialEngine.formatIndianCurrency(-15_000_000L, style = com.example.finance.CurrencyStyle.SYMBOL)
        )
    }

    // --- 7. CINEMATIC v2.0, Liquid Glass, Safe-to-Spend Today & Smart Quick-Add Tests ---

    @Test
    fun `CINEMATIC v2 ColorContrastAuditor verifies all Dark and Light pairings pass WCAG AA`() {
        val results = com.example.ui.theme.ColorContrastAuditor.runFullContrastAudit()
        assertEquals(22, results.size)
        val failures = results.filter { !it.passed }
        assertTrue(
            "Expected all 22 CINEMATIC v2.0 contrast pairings to pass WCAG AA, failed: $failures",
            failures.isEmpty()
        )
    }

    @Test
    fun `CINEMATIC v2 FluidScale computes responsive typography spacing and grid across 12 viewports`() {
        val viewports = listOf(320, 360, 375, 390, 430, 640, 768, 1024, 1280, 1440, 1920, 2560)
        val scales = viewports.map { com.example.ui.theme.CinematicFluidScale.forViewportWidthDp(it.toFloat()) }
        assertEquals(1, scales.first().gridColumns)
        assertEquals(6, scales.last().gridColumns)
        assertTrue(scales.last().textDisplay.value > scales.first().textDisplay.value)
        assertTrue(scales.last().pageGutter.value > scales.first().pageGutter.value)
    }

    @Test
    fun `LiquidGlass TierResolver degrades gracefully across API level low RAM and battery saver`() {
        assertEquals(
            com.example.ui.glass.GlassQualityTier.FULL,
            com.example.ui.glass.GlassTierResolver.resolveTier(
                sdkInt = 34,
                isLowRamDevice = false,
                isPowerSaveMode = false,
                preference = com.example.ui.glass.GlassEffectPreference.AUTO
            )
        )
        assertEquals(
            com.example.ui.glass.GlassQualityTier.SOFT,
            com.example.ui.glass.GlassTierResolver.resolveTier(
                sdkInt = 31,
                isLowRamDevice = false,
                isPowerSaveMode = false,
                preference = com.example.ui.glass.GlassEffectPreference.AUTO
            )
        )
        assertEquals(
            com.example.ui.glass.GlassQualityTier.FROST,
            com.example.ui.glass.GlassTierResolver.resolveTier(
                sdkInt = 34,
                isLowRamDevice = true,
                isPowerSaveMode = false,
                preference = com.example.ui.glass.GlassEffectPreference.AUTO
            )
        )
    }

    @Test
    fun `Safe-to-Spend Today matches worked example on Page 48 of Master Engineering Prompt v2`() {
        // Food budget ₹6,000 (spent ₹3,500 -> remaining ₹2,500)
        // Transport budget ₹3,000 (spent ₹1,000 -> remaining ₹2,000)
        // Upcoming unpaid bill ₹1,500, 10 days left -> (4,500 - 1,500) / 10 = ₹300 (30,000 paise)
        val safeToday = FinancialEngine.calculateSafeToSpendToday(
            categoryBudgetsAndSpent = listOf(
                600_000L to 350_000L,
                300_000L to 100_000L
            ),
            upcomingBillsMinor = 150_000L,
            daysLeftInMonth = 10
        )
        assertEquals(30_000L, safeToday?.amountMinor)
        assertNull(FinancialEngine.calculateSafeToSpendToday(emptyList(), 150_000L, 10))
    }

    @Test
    fun `SmartQuickAddParser parses English Hindi and Hinglish quick add phrases`() {
        val now = 1_730_000_000_000L
        val chai = com.example.finance.SmartQuickAddParser.parse("chai 40", now)
        assertNotNull(chai)
        assertEquals(4_000L, chai!!.amountMinor)
        assertEquals("Food & Dining", chai.categoryName)
        assertEquals(com.example.data.TransactionType.EXPENSE, chai.type)

        val kirana = com.example.finance.SmartQuickAddParser.parse("kirana 850 yesterday", now)
        assertNotNull(kirana)
        assertEquals(85_000L, kirana!!.amountMinor)
        assertEquals("Groceries", kirana.categoryName)
        assertTrue(kirana.timestampMillis < now)
    }
}


