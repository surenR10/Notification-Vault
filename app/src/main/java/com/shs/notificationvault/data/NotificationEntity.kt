package com.shs.notificationvault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents one captured notification stored in the local database.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** e.g. "com.whatsapp" */
    val appPackage: String,

    /** Human-readable app name, e.g. "WhatsApp" */
    val appName: String,

    /** Notification title */
    val title: String,

    /** Notification body text (big text preferred if available) */
    val text: String,

    /** System time when the notification was posted (epoch ms) */
    val timestamp: Long,

    /** User can star important ones to preserve them */
    val isStarred: Boolean = false
)
