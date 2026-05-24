package com.shs.notificationvault.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shs.notificationvault.data.NotificationEntity
import com.shs.notificationvault.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onStar:   (NotificationEntity) -> Unit,
    private val onDelete: (NotificationEntity) -> Unit
) : ListAdapter<NotificationEntity, NotificationAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemNotificationBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(item: NotificationEntity) {
            b.tvAppName.text = item.appName
            b.tvTitle.text   = item.title.ifBlank { "(no title)" }
            b.tvBody.text    = item.text.ifBlank  { "(no content)" }
            b.tvTime.text    = formatTime(item.timestamp)

            // Star icon
            val starRes = if (item.isStarred)
                android.R.drawable.btn_star_big_on
            else
                android.R.drawable.btn_star_big_off
            b.btnStar.setImageResource(starRes)

            b.btnStar.setOnClickListener   { onStar(item)   }
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatTime(ts: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - ts

        return when {
            diff < 60_000      -> "Just now"
            diff < 3_600_000   -> "${diff / 60_000}m ago"
            diff < 86_400_000  -> "${diff / 3_600_000}h ago"
            else -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ts))
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<NotificationEntity>() {
            override fun areItemsTheSame(a: NotificationEntity, b: NotificationEntity) =
                a.id == b.id
            override fun areContentsTheSame(a: NotificationEntity, b: NotificationEntity) =
                a == b
        }
    }
}
