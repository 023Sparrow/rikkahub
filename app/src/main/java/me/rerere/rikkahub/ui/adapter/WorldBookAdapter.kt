/*
package me.rerere.rikkahub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.databinding.ItemWorldBookEntryBinding

class WorldBookAdapter(
    private val onItemClick: (WorldBookEntry) -> Unit,
    private val onDeleteClick: (WorldBookEntry) -> Unit
) : ListAdapter<WorldBookEntry, WorldBookAdapter.WorldBookViewHolder>(WorldBookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorldBookViewHolder {
        val binding = ItemWorldBookEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorldBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorldBookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorldBookViewHolder(
        private val binding: ItemWorldBookEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: WorldBookEntry) {
            binding.titleTextView.text = entry.title
            binding.keywordsTextView.text = entry.keywords.joinToString(", ")
            binding.contentTextView.text = entry.content.take(100) + if (entry.content.length > 100) "..." else ""

            binding.root.setOnClickListener { onItemClick(entry) }
            binding.deleteButton.setOnClickListener { onDeleteClick(entry) }
        }
    }

    class WorldBookDiffCallback : DiffUtil.ItemCallback<WorldBookEntry>() {
        override fun areItemsTheSame(oldItem: WorldBookEntry, newItem: WorldBookEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorldBookEntry, newItem: WorldBookEntry): Boolean {
            return oldItem == newItem
        }
    }
}
*/