package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onNoteClicked: (Note) -> Unit,
    private val onNoteDeleteClicked: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.note_item_title)
        private val contentView: TextView = itemView.findViewById(R.id.note_item_content)
        private val categoryView: TextView = itemView.findViewById(R.id.note_item_category)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.note_item_delete)

        fun bind(note: Note) {
            if (!note.title.isNullOrEmpty()) {
                titleView.text = note.title
                titleView.visibility = View.VISIBLE
            } else {
                titleView.visibility = View.GONE
            }

            if (!note.content.isNullOrEmpty()) {
                contentView.text = note.content
                contentView.visibility = View.VISIBLE
            } else {
                contentView.visibility = View.GONE
            }

            if (!note.category.isNullOrEmpty()) {
                categoryView.text = note.category
                categoryView.visibility = View.VISIBLE
            } else {
                categoryView.visibility = View.GONE
            }

            itemView.setOnClickListener { onNoteClicked(note) }
            deleteButton.setOnClickListener { onNoteDeleteClicked(note) }
        }
    }
}