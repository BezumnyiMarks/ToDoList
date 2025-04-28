package com.example.todolist.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.Note
import com.example.todolist.databinding.NoteViewBinding
import java.util.*

class NotesAdapter(
    private val onNoteClick:(Note) -> Unit,
    private val onCheckboxClick:(Note) -> Unit
) : ListAdapter<Note, NotesViewHolder>(NotesAdapterDiffUtilCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            NoteViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val note = getItem(position)
        with(holder.binding){
            tvHeader.text = note.header
            tvDescription.text = note.description
            cbDone.isChecked = note.done

            root.setOnClickListener {
                onNoteClick(note)
            }

            cbDone.setOnClickListener {
                onCheckboxClick(note)
            }
        }
    }
}

class NotesAdapterDiffUtilCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem.noteID == newItem.noteID

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        oldItem == newItem
}

class NotesViewHolder (val binding: NoteViewBinding) : RecyclerView.ViewHolder(binding.root)
