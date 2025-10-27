/*
package me.rerere.rikkahub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.composables.icons.lucide.Plus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.model.MemoryTableItem
import me.rerere.rikkahub.databinding.FragmentMemoryTableBinding
import me.rerere.rikkahub.ui.adapter.MemoryTableAdapter
import me.rerere.rikkahub.ui.viewmodel.MemoryTableViewModel
import me.rerere.rikkahub.utils.showInputDialog

@AndroidEntryPoint
class MemoryTableFragment : Fragment() {
    private var _binding: FragmentMemoryTableBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryTableViewModel by viewModels()
    private lateinit var adapter: MemoryTableAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoryTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MemoryTableAdapter { tableItem ->
            navigateToTableEditor(tableItem.id)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MemoryTableFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            showNewTableDialog()
        }
    }

    private fun showNewTableDialog() {
        requireContext().showInputDialog(
            title = getString(R.string.memory_table_new_title),
            hint = getString(R.string.memory_table_name_hint),
            onConfirm = { name ->
                if (name.isNotBlank()) {
                    viewModel.createTable(name)
                }
            }
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tables.collectLatest { tables ->
                adapter.submitList(tables)
                binding.emptyView.isVisible = tables.isEmpty()
            }
        }
    }

    private fun navigateToTableEditor(tableId: String) {
        findNavController().navigate(
            MemoryTableFragmentDirections.actionMemoryTableFragmentToMemoryTableEditorFragment(tableId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
*/