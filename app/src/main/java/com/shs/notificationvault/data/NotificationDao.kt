package com.shs.notificationvault.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotificationDao {

    // ── Read ──────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarred(): LiveData<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE appPackage = :pkg ORDER BY timestamp DESC")
    fun getByApp(pkg: String): LiveData<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications 
        WHERE title LIKE '%' || :q || '%' 
           OR text  LIKE '%' || :q || '%'
           OR appName LIKE '%' || :q || '%'
        ORDER BY timestamp DESC
    """)
    fun search(q: String): LiveData<List<NotificationEntity>>

    @Query("SELECT DISTINCT appPackage, appName FROM notifications ORDER BY appName ASC")
    fun getDistinctApps(): LiveData<List<AppSummary>>

    @Query("SELECT COUNT(*) FROM notifications")
    fun getTotalCount(): LiveData<Int>

    // ── Write ─────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(n: NotificationEntity)

    @Delete
    suspend fun delete(n: NotificationEntity)

    @Query("DELETE FROM notifications WHERE isStarred = 0")
    suspend fun deleteUnstarred()

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Update
    suspend fun update(n: NotificationEntity)
}

/** Lightweight projection for the app-filter spinner */
data class AppSummary(val appPackage: String, val appName: String)
