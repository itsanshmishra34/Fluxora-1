package com.example.finance

import com.example.data.FinancialTransaction
import com.example.data.TransactionType

/**
 * CSV Data Portability & Spreadsheet Formula Injection Protection Engine.
 */
object CsvDataPortability {

    const val CSV_HEADER = "Title,AmountINR,Type,Category,TimestampMillis,Notes"
    private val DANGEROUS_FORMULA_PREFIXES = charArrayOf('=', '+', '-', '@')

    data class CsvImportSummary(
        val importedTransactions: List<FinancialTransaction>,
        val skippedDuplicatesCount: Int,
        val malformedRowErrors: List<String>
    )

    /**
     * Neutralizes spreadsheet formula injection by prefixing leading `=`, `+`, `-`, or `@`
     * with a single quote `'`, and wraps fields containing commas, quotes, or newlines in RFC-4180 quotes.
     */
    fun sanitizeAndEscapeCsvCell(rawValue: String): String {
        val trimmedCheck = rawValue.trimStart()
        val formulaSafe = if (trimmedCheck.isNotEmpty() && trimmedCheck[0] in DANGEROUS_FORMULA_PREFIXES) {
            "'$rawValue"
        } else {
            rawValue
        }

        val needsQuotes = formulaSafe.contains(',') ||
            formulaSafe.contains('"') ||
            formulaSafe.contains('\n') ||
            formulaSafe.contains('\r')

        return if (needsQuotes) {
            "\"${formulaSafe.replace("\"", "\"\"")}\""
        } else {
            formulaSafe
        }
    }

    /**
     * Exports transactions to CSV while defending against spreadsheet formula injection.
     */
    fun exportToCsv(transactions: List<FinancialTransaction>): String {
        val sb = StringBuilder()
        sb.appendLine(CSV_HEADER)
        for (tx in transactions) {
            val titleCell = sanitizeAndEscapeCsvCell(tx.title)
            val amountCell = FinancialEngine.formatMinorToPlainDecimal(tx.amountMinor)
            val typeCell = sanitizeAndEscapeCsvCell(tx.type)
            val categoryCell = sanitizeAndEscapeCsvCell(tx.categoryName)
            val timestampCell = tx.timestampMillis.toString()
            val notesCell = sanitizeAndEscapeCsvCell(tx.notes ?: "")
            sb.appendLine("$titleCell,$amountCell,$typeCell,$categoryCell,$timestampCell,$notesCell")
        }
        return sb.toString().trimEnd()
    }

    /**
     * Imports transactions from CSV:
     * - Validates required columns (`Title,AmountINR,Type,Category,TimestampMillis,Notes`).
     * - Validates minor-unit amounts and transaction dates.
     * - Reports malformed rows with descriptive line numbers.
     * - Silently skips duplicate rows within the same import batch.
     */
    fun importFromCsv(
        csvContent: String,
        userId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): CsvImportSummary {
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return CsvImportSummary(
                importedTransactions = emptyList(),
                skippedDuplicatesCount = 0,
                malformedRowErrors = listOf("CSV content is empty.")
            )
        }

        val headerColumns = parseCsvLine(lines.first()).map { it.trim().lowercase() }
        val requiredCols = listOf("title", "amountinr", "type", "category", "timestampmillis")
        val missingCols = requiredCols.filter { it !in headerColumns }
        if (missingCols.isNotEmpty()) {
            return CsvImportSummary(
                importedTransactions = emptyList(),
                skippedDuplicatesCount = 0,
                malformedRowErrors = listOf("Missing required CSV columns: ${missingCols.joinToString(", ")}")
            )
        }

        val titleIdx = headerColumns.indexOf("title")
        val amountIdx = headerColumns.indexOf("amountinr")
        val typeIdx = headerColumns.indexOf("type")
        val categoryIdx = headerColumns.indexOf("category")
        val timestampIdx = headerColumns.indexOf("timestampmillis")
        val notesIdx = headerColumns.indexOf("notes")

        val imported = mutableListOf<FinancialTransaction>()
        val errors = mutableListOf<String>()
        val seenBatchSignatures = HashSet<String>()
        var duplicateCount = 0

        for (lineIndex in 1 until lines.size) {
            val rowNumber = lineIndex + 1
            val cells = parseCsvLine(lines[lineIndex])
            if (cells.size < 5) {
                errors.add("Row $rowNumber: Insufficient columns (expected at least 5, found ${cells.size}).")
                continue
            }

            val rawTitle = cells.getOrNull(titleIdx)?.trim()?.removePrefix("'")?.trim().orEmpty()
            val rawAmount = cells.getOrNull(amountIdx)?.trim()?.removePrefix("'")?.trim().orEmpty()
            val rawType = cells.getOrNull(typeIdx)?.trim()?.uppercase().orEmpty()
            val rawCategory = cells.getOrNull(categoryIdx)?.trim()?.removePrefix("'")?.trim().orEmpty()
            val rawTimestamp = cells.getOrNull(timestampIdx)?.trim().orEmpty()
            val rawNotes = if (notesIdx >= 0) cells.getOrNull(notesIdx)?.trim()?.removePrefix("'")?.trim() else null

            if (rawTitle.isEmpty()) {
                errors.add("Row $rowNumber: Title cannot be empty.")
                continue
            }

            val amountMinor = FinancialEngine.parseInrMajorToMinor(rawAmount)
            if (amountMinor == null) {
                errors.add("Row $rowNumber: Invalid amount '$rawAmount'.")
                continue
            }

            if (rawType != TransactionType.INCOME.name && rawType != TransactionType.EXPENSE.name) {
                errors.add("Row $rowNumber: Invalid transaction type '$rawType' (must be INCOME or EXPENSE).")
                continue
            }

            if (rawCategory.isEmpty()) {
                errors.add("Row $rowNumber: Category cannot be empty.")
                continue
            }

            val timestampMillis = rawTimestamp.toLongOrNull()
            if (timestampMillis == null) {
                errors.add("Row $rowNumber: Invalid timestamp '$rawTimestamp'.")
                continue
            }

            val dateValidation = DateValidator.validateTransactionDate(timestampMillis, nowMillis)
            if (!dateValidation.isValid) {
                errors.add("Row $rowNumber: ${dateValidation.errorMessage}")
                continue
            }

            val batchSignature = "${rawTitle.lowercase()}|$amountMinor|$rawType|${rawCategory.lowercase()}|$timestampMillis|${rawNotes.orEmpty().lowercase()}"
            if (!seenBatchSignatures.add(batchSignature)) {
                duplicateCount++
                continue
            }

            val categoryId = rawCategory.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifEmpty { "other" }
            imported.add(
                FinancialTransaction(
                    id = "csv_${timestampMillis}_${imported.size}",
                    userId = userId,
                    title = rawTitle,
                    amountMinor = amountMinor,
                    type = rawType,
                    categoryId = categoryId,
                    categoryName = rawCategory,
                    timestampMillis = timestampMillis,
                    notes = rawNotes?.takeIf { it.isNotEmpty() }
                )
            )
        }

        return CsvImportSummary(
            importedTransactions = imported,
            skippedDuplicatesCount = duplicateCount,
            malformedRowErrors = errors
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
