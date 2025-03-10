package com.example.fotopaylas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.fotopaylas.databinding.ActivityNotesBinding

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var notesAdapter: NotesAdapter
    private val notesList = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId") ?: ""
        userName = intent.getStringExtra("userName") ?: ""

        binding.welcomeTextView.text = "Hoş geldin, $userName!"

        db = FirebaseFirestore.getInstance()

        notesAdapter = NotesAdapter(notesList) { note ->
            val intent = Intent(this, NoteDetailActivity::class.java)
            intent.putExtra("noteText", note.text)
            intent.putExtra("timestamp", note.timestamp)
            startActivity(intent)
        }
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.addNoteButton.setOnClickListener {
            val noteText = binding.noteEditText.text.toString()
            if (noteText.isNotEmpty()) {
                addNote(noteText)
                binding.noteEditText.text.clear()
            } else {
                Toast.makeText(this, "Lütfen bir not girin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.logoutButton.setOnClickListener {
            clearUserData()
            navigateToLogin()
        }

        loadNotes()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val noteId = notesList[position].id
                deleteNoteFromFirestore(noteId)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.notesRecyclerView)
    }

    private fun addNote(noteText: String) {
        if (userId.isNotEmpty()) {
            val note = hashMapOf(
                "userId" to userId,
                "text" to noteText,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("notlar")
                .add(note)
                .addOnSuccessListener {
                    Toast.makeText(this, "Not başarıyla eklendi", Toast.LENGTH_SHORT).show()
                    loadNotes()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Not eklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadNotes() {
        if (userId.isNotEmpty()) {
            db.collection("notlar")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    notesList.clear()
                    for (document in documents) {
                        val text = document.getString("text") ?: ""
                        val timestamp = document.getLong("timestamp") ?: 0
                        notesList.add(Note(document.id, text, timestamp))
                    }
                    notesAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Notlar yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.w("LoadNotes", "User ID is empty, skipping note load.")
        }
    }

    private fun deleteNoteFromFirestore(noteId: String) {
        db.collection("notlar").document(noteId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Not başarıyla silindi", Toast.LENGTH_SHORT).show()
                loadNotes()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Not silinirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearUserData() {
        binding.welcomeTextView.text = ""

        notesList.clear()
        notesAdapter.notifyDataSetChanged()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}