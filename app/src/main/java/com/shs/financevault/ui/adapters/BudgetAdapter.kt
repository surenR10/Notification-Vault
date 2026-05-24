package com.shs.financevault.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shs.financevault.data.Budget
import com.shs.financevault.databinding.ItemBudgetBinding

class BudgetAdapter(
    private val onEdit:   (Budget) -> Unit,
    private val onDelete: (Budget) -> Unit
) : ListAdapter<Budget, BudgetAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemBudgetBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(budget: Budget) {
            b.tvEmoji.text    = budget.category.emoji
            b.tvCategory.text = budget.category.label
            b.tvLimit.text    = "₹${budget.monthlyLimit.fmt()} / month"
            b.tvAlert.text    = "Alert at ${budget.alertAt}%"
            b.btnEdit.setOnClickListener   { onEdit(budget)   }
            b.btnDelete.setOnClickListener { onDelete(budget) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    private fun Double.fmt() = "%.0f".format(this)

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Budget>() {
            override fun areItemsTheSame(a: Budget, b: Budget) = a.category == b.category
            override fun areContentsTheSame(a: Budget, b: Budget) = a == b
        }
    }
}
