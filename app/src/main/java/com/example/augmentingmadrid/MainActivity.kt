package com.example.augmentingmadrid

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class MainActivity : AppCompatActivity(), LocationListener {

    private val TAG = "btaMainActivity"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private var latestLocation: Location? = null

        fun getLatestLocation(): Location? {
            return latestLocation
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val settingsButton:ImageButton = findViewById(R.id.settings_button)

        val backButton:ImageButton = findViewById(R.id.back_button)

        backButton.visibility = View.INVISIBLE

        settingsButton.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        val save_location: Button = findViewById(R.id.save_location)
        save_location.setOnClickListener {
            // Save the current location when the save_location button is pressed
            latestLocation?.let {
                Toast.makeText(this, "Saving location [${it.latitude}][${it.longitude}]", Toast.LENGTH_LONG).show()
                saveCoordinatesToFile(it.latitude, it.longitude)
            } ?: run {
                Toast.makeText(this, "No location available to save.", Toast.LENGTH_LONG).show()
            }
        }
        val userIdentifier = getUserIdentifier()

        if (userIdentifier == null) {
            askForUserIdentifier()
        }
        Log.d(TAG, "onCreate: The activity is being created.")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastKnownLocation()


        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), locationPermissionCode)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
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

    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        latestLocation = location
        val textView: TextView = findViewById(R.id.coordsTextView)
        textView.text = "Latitude: [${location.latitude}], Longitude: [${location.longitude}], UserId: [${getUserIdentifier()}]"
    }
    private fun saveCoordinatesToFile(latitude: Double, longitude: Double) {
        val fileName = "gps_coordinates.csv"
        val file = File(filesDir, fileName)
        val timestamp = System.currentTimeMillis()
        file.appendText("$timestamp;$latitude;$longitude\n")
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
                }
            }
        }
    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            latestLocation = location
        }
    }


    private fun askForUserIdentifier() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Enter User Identifier")
            .setIcon(R.mipmap.ic_launcher)
            .setView(input)
            .setPositiveButton("Save") { dialog, which ->
                val userInput = input.text.toString()
                if (userInput.isNotBlank()) {
                    saveUserIdentifier(userInput)
                    Toast.makeText(this, "User ID saved: $userInput", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "User ID cannot be blank", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUserIdentifier(userIdentifier: String) {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("userIdentifier", userIdentifier)
            apply()
        }
    }

    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }
}