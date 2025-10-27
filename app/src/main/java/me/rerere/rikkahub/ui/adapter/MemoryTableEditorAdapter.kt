/*
package me.rerere.rikkahub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.rerere.rikkahub.databinding.ItemMemoryTableRowBinding
import me.rerere.rikkahub.data.model.MemoryTableRowItem
import me.rerere.rikkahub.ui.adapter.MemoryTableCellAdapter

class MemoryTableEditorAdapter(
    private var columnNames: List<String>,
    private val onCellEdit: (rowIndex: Int, columnIndex: Int, value: String) -> Unit,
    private val onHeaderClick: (columnIndex: Int, columnName: String) -> Unit
) : ListAdapter<MemoryTableRowItem, MemoryTableEditorAdapter.MemoryTableRowViewHolder>(MemoryTableRowDiffCallback()) {

    fun updateColumnNames(newColumnNames: List<String>) {
        columnNames = newColumnNames
        notifyDataSetChanged() // TODO: Use DiffUtil for better performance
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryTableRowViewHolder {
        val binding = ItemMemoryTableRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoryTableRowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoryTableRowViewHolder, position: Int) {
        holder.bind(getItem(position), position, onCellEdit, onHeaderClick)
    }

    class MemoryTableRowViewHolder(private val binding: ItemMemoryTableRowBinding) : RecyclerView.ViewHolder(binding.root) {
        private val cellAdapter = MemoryTableCellAdapter { columnIndex, value ->
            onCellEdit(adapterPosition, columnIndex, value)
        }

        init {
            binding.recyclerViewCells.adapter = cellAdapter
        }

        fun bind(
            row: MemoryTableRowItem,
            rowIndex: Int,
            onCellEdit: (rowIndex: Int, columnIndex: Int, value: String) -> Unit,
            onHeaderClick: (columnIndex: Int, columnName: String) -> Unit
        ) {
            binding.textViewRowName.text = row.rowName
            val cellValues = columnNames.map { columnName ->
                row.cells[columnName] ?: ""
            }
            cellAdapter.submitList(cellValues)
        }
    }

    private class MemoryTableRowDiffCallback : DiffUtil.ItemCallback<MemoryTableRowItem>() {
        override fun areItemsTheSame(oldItem: MemoryTableRowItem, newItem: MemoryTableRowItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MemoryTableRowItem, newItem: MemoryTableRowItem): Boolean {
            return oldItem == newItem
        }
    }
}
*/