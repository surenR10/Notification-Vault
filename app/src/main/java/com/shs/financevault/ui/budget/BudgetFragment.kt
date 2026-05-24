package com.shs.financevault.ui.budget

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.shs.financevault.MainViewModel
import com.shs.financevault.data.Budget
import com.shs.financevault.data.Category
import com.shs.financevault.databinding.FragmentBudgetBinding
import com.shs.financevault.ui.adapters.BudgetAdapter

class BudgetFragment : Fragment() {

    private var _b: FragmentBudgetBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentBudgetBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BudgetAdapter(
            onEdit   = { showEditDialog(it) },
            onDelete = { vm.deleteBudget(it) }
        )
        b.rvBudgets.adapter = adapter

        vm.allBudgets.observe(viewLifecycleOwner) { adapter.submitList(it) }

        b.fabAddBudget.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val categories = Category.values()
            .filter { it != Category.SALARY && it != Category.TRANSFER }
        val labels = categories.map { "${it.emoji} ${it.label}" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Choose category")
            .setItems(labels) { _, idx -> showAmountDialog(categories[idx], null) }
            .show()
    }

    private fun showEditDialog(budget: Budget) = showAmountDialog(budget.category, budget)

    private fun showAmountDialog(category: Category, existing: Budget?) {
        val input = EditText(requireContext()).apply {
            hint = "Monthly limit (₹)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            existing?.let { setText(it.monthlyLimit.toInt().toString()) }
            setPadding(48, 32, 48, 8)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("${category.emoji} ${category.label}")
            .setMessage("Set your monthly budget")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    vm.saveBudget(category, amount)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
