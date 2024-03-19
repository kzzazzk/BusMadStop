package com.example.augmentingmadrid


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class ThirdActivity : ComponentActivity() {
    private val TAG = "btaThirdActivity"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: The activity is being created.");
        setContentView(R.layout.activity_third)
        val latitude = intent.getStringExtra("latitude")
        val longitude = intent.getStringExtra("longitude")
        Log.d(TAG, "Latitude: $latitude, Longitude: $longitude")

        val coordText: TextView = findViewById(R.id.list_coords_item)
        coordText.text = "Latitude: [${latitude}], Longitude: [${longitude}]]"

        val backToMain: Button = findViewById(R.id.toMainActivity)
        val backToSecond: Button = findViewById(R.id.toSecondActivity)
        backToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        backToSecond.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_map -> {
                    val latestLocation = MainActivity.getLatestLocation()
                    if (latestLocation != null) {
                        val intent = Intent(this, OpenStreetMapActivity::class.java)
                        val bundle = Bundle()
                        bundle.putParcelable("location", latestLocation)
                        intent.putExtra("locationBundle", bundle)
                        startActivity(intent)
                    }else{
                        Log.e(TAG, "Location not set yet.")
                    }
                    true
                }
                R.id.navigation_list -> {
                    val intent = Intent(this, SecondActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


    }
}
