package com.shs.financevault.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE isIgnored = 0 ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE isIgnored = 0 
          AND timestamp >= :from AND timestamp <= :to 
        ORDER BY timestamp DESC
    """)
    fun getInRange(from: Long, to: Long): LiveData<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE isIgnored = 0 
          AND type = 'DEBIT'
          AND timestamp >= :from AND timestamp <= :to 
        ORDER BY timestamp DESC
    """)
    fun getDebitsInRange(from: Long, to: Long): LiveData<List<Transaction>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'DEBIT' AND isIgnored = 0 
          AND category = :category
          AND timestamp >= :from AND timestamp <= :to
    """)
    fun getSpentForCategory(category: Category, from: Long, to: Long): LiveData<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'DEBIT' AND isIgnored = 0
          AND timestamp >= :from AND timestamp <= :to
    """)
    fun getTotalSpent(from: Long, to: Long): LiveData<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'CREDIT' AND isIgnored = 0
          AND timestamp >= :from AND timestamp <= :to
    """)
    fun getTotalIncome(from: Long, to: Long): LiveData<Double?>

    @Query("SELECT * FROM transactions WHERE isIgnored = 0 ORDER BY timestamp DESC LIMIT 5")
    fun getRecent(): LiveData<List<Transaction>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'DEBIT' AND isIgnored = 0
          AND timestamp >= :from AND timestamp <= :to
          AND category = :category
    """)
    suspend fun getSpentSync(category: Category, from: Long, to: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: Transaction): Long

    @Update
    suspend fun update(t: Transaction)

    @Delete
    suspend fun delete(t: Transaction)

    @Query("SELECT COUNT(*) FROM transactions WHERE rawText = :raw AND timestamp >= :since")
    suspend fun countDuplicates(raw: String, since: Long): Int
}
