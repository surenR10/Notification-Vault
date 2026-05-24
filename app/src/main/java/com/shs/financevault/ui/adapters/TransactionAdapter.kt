package com.shs.financevault.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shs.financevault.data.Transaction
import com.shs.financevault.data.TransactionType
import com.shs.financevault.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onIgnore: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction) {
            b.tvEmoji.text    = tx.category.emoji
            b.tvMerchant.text = tx.merchant.ifBlank { tx.sourceApp }
            b.tvCategory.text = tx.category.label
            b.tvTime.text     = formatTime(tx.timestamp)
            b.tvSource.text   = tx.sourceApp

            val sign = if (tx.type == TransactionType.DEBIT) "-" else "+"
            b.tvAmount.text = "$sign₹${tx.amount.fmt()}"
            b.tvAmount.setTextColor(
                if (tx.type == TransactionType.DEBIT) 0xFFC62828.toInt()
                else 0xFF2E7D32.toInt()
            )

            b.btnIgnore.setOnClickListener { onIgnore(tx) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    private fun formatTime(ts: Long): String {
        val diff = System.currentTimeMillis() - ts
        return when {
            diff < 3_600_000  -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ts))
        }
    }

    private fun Double.fmt() = if (this >= 1_00_000) "%.1fL".format(this / 1_00_000)
        else "%.0f".format(this)

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
            override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
        }
    }
}
