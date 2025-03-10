package com.example.fotopaylas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Lütfen e-posta ve şifre girin", Toast.LENGTH_SHORT).show()
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        val hashedPassword = hashPassword(password)

        db.collection("users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val storedPassword = document.getString("password")

                    if (storedPassword == hashedPassword) {
                        Toast.makeText(this, "Giriş başarılı!", Toast.LENGTH_SHORT).show()

                        saveUserSession(email)

                        val intent = Intent(this, NotesActivity::class.java)
                        intent.putExtra("userId", email)
                        intent.putExtra("userName", document.getString("name"))
                        startActivity(intent)
                        Thread.sleep(500)
                        finish()
                    } else {
                        Toast.makeText(this, "Şifre yanlış!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Böyle bir kullanıcı yok!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Giriş sırasında hata oluştu!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun saveUserSession(email: String) {
        try {
            val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
            sharedPreferences.edit().putString("user_email", email).apply()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Kullanıcı oturumu kaydedilemedi", Toast.LENGTH_SHORT).show()
        }
    }
}
