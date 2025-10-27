/*
package me.rerere.rikkahub.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.databinding.FragmentWorldBookBinding
import me.rerere.rikkahub.databinding.DialogEditWorldBookEntryBinding
import me.rerere.rikkahub.ui.adapter.WorldBookAdapter
import me.rerere.rikkahub.ui.viewmodel.WorldBookViewModel

class WorldBookFragment : Fragment() {
    private lateinit var binding: FragmentWorldBookBinding
    private val viewModel: WorldBookViewModel by viewModels()
    private lateinit var adapter: WorldBookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorldBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        adapter = WorldBookAdapter(
            onItemClick = { entry -> showEditDialog(entry) },
            onDeleteClick = { entry -> deleteEntry(entry) }
        )
        binding.entriesRecyclerView.adapter = adapter

        binding.addEntryButton.setOnClickListener {
            showEditDialog()
        }
        
        binding.searchView.setOnQueryTextListener { text ->
            viewModel.updateSearchQuery(text)
            true
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collectLatest { entries ->
                adapter.submitList(entries)
            }
        }
    }

    private fun showEditDialog(entry: WorldBookEntry? = null) {
        val dialogBinding = DialogEditWorldBookEntryBinding.inflate(layoutInflater)
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (entry == null) R.string.add_new_entry else R.string.edit_entry)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.titleEditText.text.toString()
                val keywords = dialogBinding.keywordsEditText.text.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                val content = dialogBinding.contentEditText.text.toString()
                val comment = dialogBinding.commentEditText.text.toString()
                val isConstant = dialogBinding.constantCheckBox.isChecked
                val useRegex = dialogBinding.regexCheckBox.isChecked
                val priority = dialogBinding.priorityEditText.text.toString().toIntOrNull() ?: 0

                if (entry == null) {
                    viewModel.addEntry(
                        title = title,
                        keywords = keywords,
                        content = content,
                        comment = comment,
                        isConstant = isConstant,
                        useRegex = useRegex,
                        priority = priority
                    )
                } else {
                    val updatedEntry = entry.copy(
                        title = title,
                        keywords = keywords,
                        content = content,
                        comment = comment,
                        isConstant = isConstant,
                        useRegex = useRegex,
                        priority = priority
                    )
                    viewModel.updateEntry(updatedEntry)
                }
            }
            .setNegativeButton(R.string.cancel, null)

        if (entry != null) {
            dialogBinding.titleEditText.setText(entry.title)
            dialogBinding.keywordsEditText.setText(entry.keywords.joinToString(", "))
            dialogBinding.contentEditText.setText(entry.content)
            dialogBinding.commentEditText.setText(entry.comment)
            dialogBinding.constantCheckBox.isChecked = entry.isConstant
            dialogBinding.regexCheckBox.isChecked = entry.useRegex
            dialogBinding.priorityEditText.setText(entry.priority.toString())
        }

        builder.show()
    }

    private fun deleteEntry(entry: WorldBookEntry) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_entry)
            .setMessage(getString(R.string.delete_entry_confirmation, entry.title))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteEntry(entry.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
*/