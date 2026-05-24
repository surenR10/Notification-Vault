package com.shs.financevault.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.shs.financevault.MainViewModel
import com.shs.financevault.data.TransactionType
import com.shs.financevault.databinding.FragmentDashboardBinding
import com.shs.financevault.ui.adapters.TransactionAdapter
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentDashboardBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TransactionAdapter { vm.ignoreTransaction(it) }
        b.rvRecent.adapter = adapter

        // Month label
        b.tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Date())

        // Total spent this month
        vm.totalSpent.observe(viewLifecycleOwner) { spent ->
            b.tvTotalSpent.text = "₹${(spent ?: 0.0).fmt()}"
        }

        // Total income this month
        vm.totalIncome.observe(viewLifecycleOwner) { income ->
            b.tvTotalIncome.text = "₹${(income ?: 0.0).fmt()}"
        }

        // Savings = income - spent
        vm.totalSpent.observe(viewLifecycleOwner) { spent ->
            vm.totalIncome.observe(viewLifecycleOwner) { income ->
                val savings = (income ?: 0.0) - (spent ?: 0.0)
                b.tvSavings.text = "₹${savings.fmt()}"
                b.tvSavings.setTextColor(
                    if (savings >= 0) 0xFF2E7D32.toInt() else 0xFFC62828.toInt()
                )
            }
        }

        // Budget progress bars — top 3 categories with budgets
        vm.allBudgets.observe(viewLifecycleOwner) { budgets ->
            b.budgetSummaryContainer.removeAllViews()
            budgets.take(3).forEach { budget ->
                val row = layoutInflater.inflate(
                    com.shs.financevault.R.layout.item_budget_mini,
                    b.budgetSummaryContainer, false
                )
                val label   = row.findViewById<android.widget.TextView>(com.shs.financevault.R.id.tvBudgetLabel)
                val progress= row.findViewById<android.widget.ProgressBar>(com.shs.financevault.R.id.progressBudget)
                val amount  = row.findViewById<android.widget.TextView>(com.shs.financevault.R.id.tvBudgetAmount)

                label.text = "${budget.category.emoji} ${budget.category.label}"

                vm.thisMonthAll.observe(viewLifecycleOwner) { txList ->
                    val spent = txList
                        .filter { it.category == budget.category && it.type == TransactionType.DEBIT && !it.isIgnored }
                        .sumOf { it.amount }
                    val pct = ((spent / budget.monthlyLimit) * 100).toInt().coerceAtMost(100)
                    progress.progress = pct
                    progress.progressTintList = android.content.res.ColorStateList.valueOf(
                        when {
                            pct >= 100 -> 0xFFC62828.toInt()
                            pct >= 80  -> 0xFFE65100.toInt()
                            else       -> 0xFF2E7D32.toInt()
                        }
                    )
                    amount.text = "₹${spent.fmt()} / ₹${budget.monthlyLimit.fmt()}"
                }
                b.budgetSummaryContainer.addView(row)
            }
        }

        // Recent transactions
        vm.recentTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.tvNoTransactions.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    private fun Double.fmt() = if (this >= 1_00_000) "%.1fL".format(this / 1_00_000)
        else "%.0f".format(this)
}
