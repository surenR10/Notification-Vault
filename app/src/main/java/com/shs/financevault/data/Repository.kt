package com.shs.financevault.data

import android.content.Context
import java.util.Calendar

class Repository(ctx: Context) {
    private val db = AppDatabase.get(ctx)
    private val txDao = db.transactionDao()
    private val budgetDao = db.budgetDao()

    // ── Transactions ──────────────────────────────────────────────────────────
    val allTransactions = txDao.getAll()
    val recentTransactions = txDao.getRecent()

    fun getThisMonthDebits(): LiveDataPair {
        val (from, to) = thisMonthRange()
        return txDao.getDebitsInRange(from, to)
    }

    fun getThisMonthAll() = txDao.getInRange(*thisMonthRange().toList().toLongArray())
    fun getTotalSpent()   = txDao.getTotalSpent(*thisMonthRange().toList().toLongArray())
    fun getTotalIncome()  = txDao.getTotalIncome(*thisMonthRange().toList().toLongArray())

    suspend fun insert(t: Transaction) = txDao.insert(t)
    suspend fun update(t: Transaction) = txDao.update(t)

    suspend fun isDuplicate(rawText: String): Boolean {
        val since = System.currentTimeMillis() - 60_000   // 60-second window
        return txDao.countDuplicates(rawText.take(100), since) > 0
    }

    suspend fun getSpentForCategory(cat: Category): Double {
        val (from, to) = thisMonthRange()
        return txDao.getSpentSync(cat, from, to) ?: 0.0
    }

    // ── Budgets ───────────────────────────────────────────────────────────────
    val allBudgets = budgetDao.getAll()
    suspend fun upsertBudget(b: Budget) = budgetDao.upsert(b)
    suspend fun getBudgetForCategory(cat: Category) = budgetDao.getForCategory(cat)
    suspend fun deleteBudget(b: Budget) = budgetDao.delete(b)

    // ── Helpers ───────────────────────────────────────────────────────────────
    fun thisMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val from = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        return from to cal.timeInMillis
    }
}

private fun Pair<Long, Long>.toList() = listOf(first, second)
private fun List<Long>.toLongArray() = LongArray(size) { get(it) }
typealias LiveDataPair = androidx.lifecycle.LiveData<List<Transaction>>
