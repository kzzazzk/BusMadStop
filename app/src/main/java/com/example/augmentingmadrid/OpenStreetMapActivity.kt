package com.example.augmentingmadrid


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.io.InputStream


class OpenStreetMapActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OpenStreetMapActivity"
        private const val MINIMUM_ZOOM_LEVEL_FOR_MARKERS = 15
        private const val GEOJSON_FILE_NAME = "EMT_stops.geojson"
        private const val MAP_EVENT_DELAY = 100L
        private const val MIN_TIME_BW_UPDATES: Long = 1000
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 1.0f
        private const val PERMISSIONS_REQUEST_LOCATION = 101
    }

    private lateinit var map: MapView
    private var geoJsonMarkers: MutableList<Marker> = mutableListOf()
    private var locationMarker: Marker? = null
    private lateinit var locationManager: LocationManager
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.i(TAG, "Location changed: Lat ${location.latitude}, Lon ${location.longitude}")
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            updateLocationMarker(geoPoint)
            map.controller.animateTo(geoPoint) // Optional: move the map to the new location
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }

        override fun onProviderEnabled(provider: String) { }

        override fun onProviderDisabled(provider: String) { }
    }


    private fun checkPermissionsAndStartLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION)
        } else {
            manageLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    manageLocation()
                }
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_street_map)
        initializeMap()
        manageLocation()
        setupToolbar()
        setupSettingsButton()
        handleIntentLocation()
        setupMapEventOverlay()
        setupBottomNavigationView()
        checkPermissionsAndStartLocationUpdates()
    }

    private fun manageLocation(){
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
            )
        } catch (e: SecurityException) {
            // Handle exception if permissions are not granted
            e.printStackTrace()
        }

    }

    private fun updateLocationMarker(geoPoint: GeoPoint) {
        if (locationMarker == null) {
            locationMarker = Marker(map).apply {
                position = geoPoint
                icon = ContextCompat.getDrawable(this@OpenStreetMapActivity, R.drawable.current_location_icon)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                map.overlays.add(this)
            }
        } else {
            locationMarker?.position = geoPoint
        }
        map.invalidate() // Refresh the map
    }

    private fun initializeMap() {
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))
        map = findViewById<MapView>(R.id.map).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(MINIMUM_ZOOM_LEVEL_FOR_MARKERS)
            intent.getBundleExtra("locationBundle")?.getParcelable<Location>("location")?.also { location ->
                controller.setCenter(GeoPoint(location.latitude, location.longitude))
            }
            addMapListener(DelayedMapListener(createMapListener(), MAP_EVENT_DELAY))
        }
        map.setMultiTouchControls(true)
    }


    private fun setupToolbar() {
        findViewById<Toolbar>(R.id.toolbar).also {
            setSupportActionBar(it)
            findViewById<ImageButton>(R.id.back_button).visibility = View.INVISIBLE
        }
    }


    private fun setupSettingsButton() {
        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }


    private fun createMapListener() = object : MapListener {
        override fun onScroll(event: ScrollEvent): Boolean {
            updateMarkersVisibility()
            return false
        }


        override fun onZoom(event: ZoomEvent): Boolean {
            updateMarkersVisibility()
            return false
        }
    }


    private fun handleIntentLocation() {
        intent.getBundleExtra("locationBundle")?.getParcelable<Location>("location")?.also { location ->
            Log.i(TAG, "Location: [${location.altitude}][${location.latitude}][${location.longitude}]")
            val gymkhanaCoords: List<GeoPoint> = listOf()
            val gymkhanaNames: List<String> = listOf()
            addMarkersAndRoute(map, gymkhanaCoords, gymkhanaNames)
            loadGeoJsonMarkers()
        } ?: Log.w(TAG, "No location found in intent extras.")
    }


    private fun setupMapEventOverlay() {
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean = false
            override fun longPressHelper(p: GeoPoint): Boolean = false
        })
        map.overlays.add(mapEventsOverlay)
    }


    private fun setupBottomNavigationView() {
        findViewById<BottomNavigationView>(R.id.nav_view).setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navigateTo(MainActivity::class.java)
                    true  // Explicit return true for handled cases
                }
                R.id.navigation_map -> {
                    handleMapNavigation()
                    true
                }
                R.id.navigation_list -> {
                    navigateTo(SecondActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }


    private fun navigateTo(activityClass: Class<out AppCompatActivity>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }


    private fun handleMapNavigation(): Boolean {
        MainActivity.getLatestLocation()?.let { location ->
            val intent = Intent(this, OpenStreetMapActivity::class.java).apply {
                putExtra("locationBundle", Bundle().apply { putParcelable("location", location) })
            }
            startActivity(intent)
        } ?: Log.e(TAG, "Location not set yet.")
        return true
    }

    private fun loadGeoJson() = try {
        assets.open(GEOJSON_FILE_NAME).bufferedReader().use { it.readText() }
    } catch (ex: IOException) {
        Log.e(TAG, "IOException reading GeoJSON file", ex)
        ""
    }


    private fun loadGeoJsonMarkers() {
        val jsonString: String = loadGeoJson()
        JSONObject(jsonString).getJSONArray("features").let { features ->
            for (i in 0 until features.length()) {
                features.getJSONObject(i).also { feature ->
                    if (feature.getJSONObject("geometry").getString("type") == "Point") {
                        parseGeoPoint(feature)?.let { geoPoint ->
                            geoJsonMarkers.add(createMarker(geoPoint))
                        }
                    }
                }
            }
        }
    }


    private fun parseGeoPoint(feature: JSONObject): GeoPoint? {
        feature.getJSONObject("geometry").getJSONArray("coordinates").let { coordinates ->
            return GeoPoint(coordinates.getDouble(1), coordinates.getDouble(0))
        }
    }


    private fun createMarker(geoPoint: GeoPoint) = Marker(map).apply {
        position = geoPoint
        icon = ContextCompat.getDrawable(this@OpenStreetMapActivity, R.drawable.bus_station_icon)
    }


    fun addMarkersAndRoute(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {
        // Verificar que las listas tengan el mismo tamaño
        if (locationsCoords.size != locationsNames.size) {
            Log.e(TAG, "Locations and names lists must have the same number of items.")
            return
        }

        // Un mapa iconMap asociando el nombre de cada lugar con su icono
        val iconMap = mapOf(
            "Plaza de España" to R.drawable.icon_plaza,
            "Malasaña" to R.drawable.icon_location,
            "Puerta de Alcala" to R.drawable.icon_puerta_alcala,
            "Museo del Prado" to R.drawable.icon_museum,
            "El Retiro" to R.drawable.icon_park,
            "Restaurante Asturias" to R.drawable.icon_restaurant,
            "Bar Las Carabelas" to R.drawable.icon_restaurant,
            "Taperia La Pequena Grana" to R.drawable.icon_restaurant,
            "Callao" to R.drawable.icon_plaza,
            "Casa de Campo" to R.drawable.icon_park,
            "Manzanares el Real" to R.drawable.icon_park
        )

        // Crear y configurar la Polyline
        val route = Polyline().apply {
            color = ContextCompat.getColor(this@OpenStreetMapActivity, R.color.teal_700)
            setPoints(locationsCoords)
        }

        mapView.overlays.add(route)

        locationsNames.forEachIndexed { index, name ->
            val location = locationsCoords[index]
            val markerIconId = iconMap[name] ?:  ContextCompat.getDrawable(this, com.google.android.material.R.drawable.ic_m3_chip_checked_circle) // Use a default icon if no match found
            val marker = Marker(mapView).apply {
                position = location
                title = "Marker at $name ${location.latitude}, ${location.longitude}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(this@OpenStreetMapActivity, markerIconId as Int)
            }
            mapView.overlays.add(marker)
        }


        // Refrescar el mapa para que muestre la ruta y los marcadores
        mapView.invalidate()
    }




    private fun updateMarkersVisibility() {
        // Check current zoom level
        val currentZoomLevel = map.zoomLevelDouble


        // Only show markers if the current zoom level is >= MINIMUM_ZOOM_LEVEL_FOR_MARKERS
        if (currentZoomLevel >= MINIMUM_ZOOM_LEVEL_FOR_MARKERS) {
            val boundingBox = map.boundingBox
            geoJsonMarkers.forEach { marker ->
                if (boundingBox.contains(marker.position)) {
                    if (!map.overlays.contains(marker)) {
                        map.overlays.add(marker)
                    }
                } else {
                    map.overlays.remove(marker)
                }
            }
        } else {
            // If zoomed out, remove all bus stop markers
            geoJsonMarkers.forEach {
                map.overlays.remove(it)
            }
        }


        map.invalidate() // Refresh the map
    }



    override fun onResume() {
        super.onResume()
        map.onResume()
    }


    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(locationListener)
        map.onPause()
    }
}


