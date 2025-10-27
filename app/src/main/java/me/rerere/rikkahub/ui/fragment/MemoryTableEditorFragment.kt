/*
package me.rerere.rikkahub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import me.rerere.rikkahub.R
import me.rerere.rikkahub.databinding.FragmentMemoryTableEditorBinding
import me.rerere.rikkahub.ui.adapter.MemoryTableEditorAdapter
import me.rerere.rikkahub.ui.viewmodel.MemoryTableEditorViewModel
import me.rerere.rikkahub.utils.showInputDialog

@AndroidEntryPoint
class MemoryTableEditorFragment : Fragment() {

    private var _binding: FragmentMemoryTableEditorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryTableEditorViewModel by viewModels()
    private val args: MemoryTableEditorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoryTableEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tableId = args.tableId
        viewModel.setTableId(tableId)

        setupToolbar()
        setupButtons()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun setupButtons() {
        binding.btnAddRow.setOnClickListener {
            viewModel.addRow()
        }

        binding.btnAddColumn.setOnClickListener {
            showAddColumnDialog()
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveMemoryTable()
        }
    }

    private fun showAddColumnDialog() {
        requireContext().showInputDialog(
            title = getString(R.string.memory_table_add_column),
            hint = getString(R.string.memory_table_column_name_hint),
            onConfirm = { columnName ->
                if (columnName.isNotBlank()) {
                    viewModel.addColumn(columnName)
                }
            }
        )
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = MemoryTableEditorAdapter(
                columnNames = emptyList(),
                onCellEdit = { rowIndex, columnIndex, value ->
                    viewModel.updateCell(rowIndex, columnIndex, value)
                },
                onHeaderClick = { columnIndex, columnName ->
                }
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.columnNames.collectLatest { columnNames ->
                (binding.recyclerView.adapter as? MemoryTableEditorAdapter)?.updateColumnNames(columnNames)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tableRows.collectLatest { tableRows ->
                (binding.recyclerView.adapter as? MemoryTableEditorAdapter)?.submitList(tableRows)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is MemoryTableEditorViewModel.UIEvent.NavigateBack -> {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
*/