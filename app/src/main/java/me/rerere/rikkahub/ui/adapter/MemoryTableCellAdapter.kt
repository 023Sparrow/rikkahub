/*
package me.rerere.rikkahub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.rerere.rikkahub.databinding.ItemMemoryTableCellBinding

class MemoryTableCellAdapter(
    private val onCellEdit: (position: Int, value: String) -> Unit
) : ListAdapter<String, MemoryTableCellAdapter.MemoryTableCellViewHolder>(MemoryTableCellDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryTableCellViewHolder {
        val binding = ItemMemoryTableCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoryTableCellViewHolder(binding, onCellEdit)
    }

    override fun onBindViewHolder(holder: MemoryTableCellViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class MemoryTableCellViewHolder(
        private val binding: ItemMemoryTableCellBinding,
        private val onCellEdit: (position: Int, value: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.editTextCell.setOnEditorActionListener { v, actionId, event ->
                onCellEdit(adapterPosition, v.text.toString())
                false
            }
        }

        fun bind(cellValue: String, position: Int) {
            binding.editTextCell.setText(cellValue)
        }
    }

    private class MemoryTableCellDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
*/