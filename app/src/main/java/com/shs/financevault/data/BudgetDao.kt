package com.shs.financevault.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAll(): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE category = :cat")
    suspend fun getForCategory(cat: Category): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(b: Budget)

    @Delete
    suspend fun delete(b: Budget)
}
