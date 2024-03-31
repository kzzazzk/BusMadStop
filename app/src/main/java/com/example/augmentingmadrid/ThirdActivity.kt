package com.example.augmentingmadrid


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ThirdActivity : AppCompatActivity() {
    private val TAG = "ThirdActivity"
    private lateinit var retrofitService: EmtMadridService
    private lateinit var listView: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.visibility = View.INVISIBLE

        val settingsButton: ImageButton = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_map -> {
                    MainActivity.getLatestLocation()?.let {
                        val intent = Intent(this, OpenStreetMapActivity::class.java).apply {
                            val bundle = Bundle()
                            bundle.putParcelable("location", it)
                            putExtra("locationBundle", bundle)
                        }
                        startActivity(intent)
                    }
                    true
                }
                R.id.navigation_list -> {
                    startActivity(Intent(this, SecondActivity::class.java))
                    true
                }
                else -> false
            }
        }


        listView = findViewById(R.id.list_view_bus_stops)
        val radius = 100
        retrofitService = RetrofitClient.instance


        val latitudeValue = intent.getStringExtra("latitude")
        val longitudeValue = intent.getStringExtra("longitude")
        makeApiCall(longitudeValue, latitudeValue, radius)
    }


    private fun makeApiCall(longitude: String?, latitude: String?, radius: Int) {
        if (longitude.isNullOrEmpty() || latitude.isNullOrEmpty()) {
            Log.e(TAG, "Longitude or latitude values are null or empty.")
            return
        }


        val longitudeValue = longitude.toDouble()
        val latitudeValue = latitude.toDouble()

        retrofitService.getStopsAround("0303d17d-f6ab-48a1-b74a-d4dcb7cafdb9", longitudeValue, latitudeValue, radius)
            .enqueue(object : Callback<BusStopsResponse> {
                override fun onResponse(call: Call<BusStopsResponse>, response: Response<BusStopsResponse>) {
                    if (response.isSuccessful) {
                        val busStops = response.body()?.data
                        busStops?.let {
                            updateListView(it)
                        }
                    } else {
                        Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                    }
                }


                override fun onFailure(call: Call<BusStopsResponse>, t: Throwable) {
                    Log.e(TAG, "Failure: ${t.message}")
                }
            })
    }


    private fun updateListView(busStops: List<BusStop>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            busStops.map { "${it.stopName}, ${it.address}, ${it.metersToPoint} meters" }
        )
        listView.adapter = adapter
    }
}
