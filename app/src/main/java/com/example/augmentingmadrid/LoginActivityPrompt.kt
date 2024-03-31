package com.example.augmentingmadrid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class LoginActivityPrompt : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN: Int = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_prompt)

        auth = FirebaseAuth.getInstance()

        // Views
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.visibility = View.INVISIBLE

        val settingsButton: ImageButton = findViewById(R.id.settings_button)
        settingsButton.visibility = View.INVISIBLE

        val signInWithEmailButton: Button = findViewById(R.id.btnSignInWithEmail)
        val signInWithGoogleButton: Button = findViewById(R.id.btnSignInWithGoogle)
        val signUpButton: Button = findViewById(R.id.btnSignUp)

        signInWithEmailButton.setOnClickListener {
            val signInIntent = Intent(this, LoginActivity::class.java)
            startActivity(signInIntent)
        }

        signInWithGoogleButton.setOnClickListener {
            startSignInWithGoogle()
        }

        signUpButton.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
    }

    private fun signInWithEmail() {
        setContentView(R.layout.activity_login)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)

        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    Toast.makeText(this, "Welcome back ${currentUser?.email}", Toast.LENGTH_SHORT).show()
                    val mainIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun startSignInWithGoogle() {
        // Configure Google Sign-in
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

        // Start sign-in Intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, "Signed in as ${currentUser?.displayName}", Toast.LENGTH_SHORT).show()
                val mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            } else {
                Toast.makeText(this, "Sign-in failed: ${response?.error?.errorCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }
    }
}