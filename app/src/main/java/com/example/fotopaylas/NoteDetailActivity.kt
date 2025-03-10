package com.example.fotopaylas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fotopaylas.databinding.ActivityNoteDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val noteText = intent.getStringExtra("noteText") ?: ""
        val timestamp = intent.getLongExtra("timestamp", 0L)

        val noteTextView: TextView = findViewById(R.id.noteTextView)
        val dateTextView: TextView = findViewById(R.id.dateTextView)

        noteTextView.text = noteText

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        dateTextView.text = sdf.format(date)

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}