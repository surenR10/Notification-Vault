package com.shs.financevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: Category,
    val monthlyLimit: Double,
    val alertAt: Int = 80   // alert when X% of budget is consumed
)
