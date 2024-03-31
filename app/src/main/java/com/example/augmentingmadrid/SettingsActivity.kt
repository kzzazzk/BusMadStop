package com.example.augmentingmadrid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text


class SettingsActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    private lateinit var auth: FirebaseAuth
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val button:ImageButton = findViewById(R.id.settings_button)
        button.visibility = View.INVISIBLE

        val backButton:ImageButton = findViewById(R.id.back_button)
        backButton.visibility = View.VISIBLE
        val userIdentifier: TextView = findViewById(R.id.user_identifier)
        userIdentifier.text = "user_identifier: \n"+ auth.currentUser?.uid
        val log_out_button:Button = findViewById(R.id.logout)
        log_out_button.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivityPrompt::class.java)
            startActivity(intent)
        }
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUser(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user", null)
    }

}