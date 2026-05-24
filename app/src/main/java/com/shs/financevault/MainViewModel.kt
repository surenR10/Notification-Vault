package com.shs.financevault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shs.financevault.data.Budget
import com.shs.financevault.data.Category
import com.shs.financevault.data.Repository
import com.shs.financevault.data.Transaction
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val repo = Repository(app)

    val recentTransactions  = repo.recentTransactions
    val allTransactions     = repo.allTransactions
    val thisMonthAll        = repo.getThisMonthAll()
    val totalSpent          = repo.getTotalSpent()
    val totalIncome         = repo.getTotalIncome()
    val allBudgets          = repo.allBudgets

    fun ignoreTransaction(t: Transaction) = viewModelScope.launch {
        repo.update(t.copy(isIgnored = true))
    }

    fun saveBudget(category: Category, limit: Double) = viewModelScope.launch {
        repo.upsertBudget(Budget(category = category, monthlyLimit = limit))
    }

    fun deleteBudget(b: Budget) = viewModelScope.launch {
        repo.deleteBudget(b)
    }
}
