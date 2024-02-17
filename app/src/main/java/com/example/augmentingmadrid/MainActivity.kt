package com.example.augmentingmadrid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
class MainActivity : ComponentActivity() {

    private val TAG = "btaMainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: The activity is being created.")

        setContentView(R.layout.activity_main)
        val buttonNext: Button = findViewById(R.id.toSecondActivity)

        buttonNext.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

}
