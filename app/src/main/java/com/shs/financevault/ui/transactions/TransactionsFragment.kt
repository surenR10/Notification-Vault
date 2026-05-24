package com.shs.financevault.ui.transactions

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.shs.financevault.MainViewModel
import com.shs.financevault.databinding.FragmentTransactionsBinding
import com.shs.financevault.ui.adapters.TransactionAdapter

class TransactionsFragment : Fragment() {

    private var _b: FragmentTransactionsBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentTransactionsBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TransactionAdapter { vm.ignoreTransaction(it) }
        b.rvTransactions.adapter = adapter

        vm.allTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
