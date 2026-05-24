package com.shs.financevault.parser

import com.shs.financevault.data.Category
import com.shs.financevault.data.Transaction
import com.shs.financevault.data.TransactionType

object NotificationParser {

    /**
     * Attempt to parse a financial transaction from a notification.
     * Returns null if this notification is not a financial transaction.
     */
    fun parse(
        title: String,
        body: String,
        packageName: String,
        timestamp: Long
    ): Transaction? {
        val text = "$title $body"

        // Must have an amount to be a transaction
        val amountMatch = BankPatterns.AMOUNT.find(text) ?: return null
        val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null

        // Skip trivial amounts (likely OTP or promo notifications)
        if (amount < 1.0) return null

        // Determine debit or credit
        val type = when {
            BankPatterns.DEBIT_KEYWORDS.containsMatchIn(text)  -> TransactionType.DEBIT
            BankPatterns.CREDIT_KEYWORDS.containsMatchIn(text) -> TransactionType.CREDIT
            else -> return null   // can't determine direction — skip
        }

        // Extract merchant name using multiple strategies
        val merchant = extractMerchant(text, type)

        // Auto-categorise
        val category = if (type == TransactionType.CREDIT) {
            if (CategoryClassifier.classify(merchant, text) == Category.OTHER)
                Category.SALARY else CategoryClassifier.classify(merchant, text)
        } else {
            CategoryClassifier.classify(merchant, text)
        }

        // Optional: available balance
        val balance = BankPatterns.BALANCE.find(text)
            ?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

        // Optional: UPI ref
        val upiRef = BankPatterns.UPI_REF.find(text)?.groupValues?.get(1)

        return Transaction(
            amount     = amount,
            type       = type,
            merchant   = merchant.trim().ifBlank { BankPatterns.sourceLabel(packageName) },
            category   = category,
            timestamp  = timestamp,
            balance    = balance,
            rawText    = text.take(500),
            sourceApp  = BankPatterns.sourceLabel(packageName),
            upiRef     = upiRef
        )
    }

    private fun extractMerchant(text: String, type: TransactionType): String {
        // Strategy 1: "Info: UPI-MERCHANTNAME-..." (HDFC style)
        BankPatterns.INFO_FIELD.find(text)?.groupValues?.get(1)
            ?.split("-")?.firstOrNull()?.trim()
            ?.takeIf { it.length >= 2 }
            ?.let { return it.capitalise() }

        // Strategy 2: VPA handle before @
        BankPatterns.VPA.find(text)?.groupValues?.get(1)
            ?.takeIf { it.length >= 2 && !it.all { c -> c.isDigit() } }
            ?.let { return it.capitalise() }

        // Strategy 3: "paid to X" / "sent to X"
        if (type == TransactionType.DEBIT) {
            BankPatterns.MERCHANT_TO.find(text)?.groupValues?.get(1)
                ?.trim()?.takeIf { it.length >= 2 }
                ?.let { return it.capitalise() }
        }

        // Strategy 4: "received from X"
        if (type == TransactionType.CREDIT) {
            BankPatterns.MERCHANT_FROM.find(text)?.groupValues?.get(1)
                ?.trim()?.takeIf { it.length >= 2 }
                ?.let { return it.capitalise() }
        }

        return ""
    }

    private fun String.capitalise() =
        split(" ").joinToString(" ") { w ->
            w.lowercase().replaceFirstChar { it.uppercaseChar() }
        }
}
