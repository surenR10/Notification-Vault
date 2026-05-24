package com.shs.financevault

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import com.shs.financevault.alerts.AlertEngine
import com.shs.financevault.data.Repository
import com.shs.financevault.parser.BankPatterns
import com.shs.financevault.parser.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FinanceListenerService : NotificationListenerService() {

    private val job  = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val repo by lazy { Repository(applicationContext) }

    // Large transaction threshold (₹5,000 by default)
    private val LARGE_TX_THRESHOLD = 5_000.0

    // Low balance threshold
    private val LOW_BALANCE_THRESHOLD = 1_000.0

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Only process whitelisted financial apps
        if (sbn.packageName !in BankPatterns.FINANCIAL_PACKAGES) return

        val extras = sbn.notification.extras
        val title  = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val body   = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()

        if (title.isBlank() && body.isBlank()) return

        scope.launch {
            // Parse the notification into a Transaction
            val tx = NotificationParser.parse(title, body, sbn.packageName, sbn.postTime)
                ?: return@launch

            // Deduplicate (same text within 60 seconds = same notification)
            if (repo.isDuplicate(tx.rawText)) return@launch

            // Save to database
            repo.insert(tx)

            // ── Alerts ────────────────────────────────────────────────────────

            // 1. Large transaction alert
            if (tx.amount >= LARGE_TX_THRESHOLD) {
                AlertEngine.largeTransactionAlert(applicationContext, tx.amount, tx.merchant)
            }

            // 2. Low balance alert
            tx.balance?.let { bal ->
                if (bal <= LOW_BALANCE_THRESHOLD) {
                    AlertEngine.lowBalanceAlert(applicationContext, bal)
                }
            }

            // 3. Budget alert — check if this category has a budget set
            val budget = repo.getBudgetForCategory(tx.category)
            budget?.let {
                AlertEngine.checkBudgetAlert(applicationContext, repo, it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
