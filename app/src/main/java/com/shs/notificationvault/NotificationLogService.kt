package com.shs.notificationvault

import android.app.Notification
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.shs.notificationvault.data.NotificationEntity
import com.shs.notificationvault.data.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Runs as a bound system service once the user grants Notification Access.
 * Every notification that appears in the status bar triggers onNotificationPosted().
 * We extract relevant fields and persist them to the local Room database.
 */
class NotificationLogService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val repo by lazy { NotificationRepository(applicationContext) }

    // Packages to ignore (our own app + known noisy system services)
    private val ignoreList = setOf(
        "com.shs.notificationvault",
        "android",
        "com.android.systemui"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName in ignoreList) return
        if (sbn.isOngoing) return // skip persistent/foreground-service notifications

        val extras = sbn.notification.extras

        val title   = extras.getString(Notification.EXTRA_TITLE).orEmpty().trim()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
        val text    = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()

        // Use bigText when available — it has the full message body
        val body = if (!bigText.isNullOrBlank()) bigText else text

        // Skip completely empty notifications (e.g. silent ticker updates)
        if (title.isBlank() && body.isBlank()) return

        val entity = NotificationEntity(
            appPackage = sbn.packageName,
            appName    = resolveAppName(sbn.packageName),
            title      = title,
            text       = body,
            timestamp  = sbn.postTime
        )

        scope.launch { repo.insert(entity) }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun resolveAppName(pkg: String): String {
        return try {
            val info: ApplicationInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg // fallback to package name
        }
    }
}
