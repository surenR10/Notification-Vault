package com.shs.financevault.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.shs.financevault.MainActivity
import com.shs.financevault.R
import com.shs.financevault.data.Budget
import com.shs.financevault.data.Repository

object AlertEngine {

    private const val CHANNEL_ID   = "finance_vault_alerts"
    private const val CHANNEL_NAME = "Finance Alerts"

    fun setupChannel(ctx: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Budget and spending alerts" }
        ctx.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /**
     * Check if a budget alert should fire after a new transaction is recorded.
     */
    suspend fun checkBudgetAlert(ctx: Context, repo: Repository, budget: Budget) {
        val spent = repo.getSpentForCategory(budget.category)
        val pct   = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit * 100).toInt() else 0

        val (title, body) = when {
            pct >= 100 -> Pair(
                "🚨 Budget exceeded: ${budget.category.label}",
                "You've spent ₹${spent.fmt()} of your ₹${budget.monthlyLimit.fmt()} budget"
            )
            pct >= budget.alertAt -> Pair(
                "⚠️ Budget alert: ${budget.category.label}",
                "${pct}% used — ₹${spent.fmt()} of ₹${budget.monthlyLimit.fmt()}"
            )
            else -> return   // no alert needed
        }

        sendNotification(ctx, title, body, pct * 100)   // unique ID per category+pct
    }

    /**
     * Alert for a large single transaction.
     */
    fun largeTransactionAlert(ctx: Context, amount: Double, merchant: String) {
        sendNotification(
            ctx,
            "💳 Large transaction detected",
            "₹${amount.fmt()} paid to $merchant",
            id = 9001
        )
    }

    /**
     * Low balance alert (when bank notification includes balance).
     */
    fun lowBalanceAlert(ctx: Context, balance: Double) {
        sendNotification(
            ctx,
            "🔴 Low balance warning",
            "Available balance: ₹${balance.fmt()}",
            id = 9002
        )
    }

    private fun sendNotification(ctx: Context, title: String, body: String, id: Int = 1) {
        val intent = Intent(ctx, MainActivity::class.java)
        val pi = PendingIntent.getActivity(ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vault)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        ctx.getSystemService(NotificationManager::class.java)
            .notify(id, notif)
    }

    private fun Double.fmt(): String {
        return if (this >= 1_00_000) "%.1fL".format(this / 1_00_000)
        else if (this >= 1_000) "%.0f".format(this)
        else "%.0f".format(this)
    }
}
