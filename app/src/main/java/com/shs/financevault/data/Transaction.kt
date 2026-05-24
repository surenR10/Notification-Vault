package com.shs.financevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { DEBIT, CREDIT }

enum class Category(val label: String, val emoji: String) {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    BILLS("Bills & Utilities", "⚡"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health", "💊"),
    GROCERIES("Groceries", "🛒"),
    TRANSFER("Transfer", "↔️"),
    SALARY("Salary / Income", "💰"),
    INVESTMENT("Investment", "📈"),
    OTHER("Other", "📦")
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val category: Category,
    val timestamp: Long,
    val balance: Double? = null,        // available balance if parsed
    val rawText: String,                // original notification text
    val sourceApp: String,              // e.g. "GPay", "HDFC Bank"
    val upiRef: String? = null,         // UPI reference number if found
    val isIgnored: Boolean = false      // user can dismiss false positives
)
