package com.example.augmentingmadrid

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class OpenStreetMapActivity : AppCompatActivity() {
    private val TAG = "btaOpenStreetMapActivity"
    private lateinit var map: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_street_map)
        Log.d(TAG, "onCreate: The activity is being created.");
        val bundle = intent.getBundleExtra("locationBundle")
        val location: Location? = bundle?.getParcelable("location")
        if (location != null) {
            Log.i(TAG, "onCreate: Location["+location.altitude+"]["+location.latitude+"]["+location.longitude+"][")
            Configuration.getInstance().load(applicationContext, getSharedPreferences("osm", MODE_PRIVATE))
            map = findViewById(R.id.map)
            map.setTileSource(TileSourceFactory.MAPNIK)
            map.controller.setZoom(15.0)
            val madridGeoPoint = GeoPoint(40.416775,  -3.703790) // Madrid, Spain
            val gymkhanaCoords = listOf(
                GeoPoint(40.423391666667, -3.7122333333333), // Plaza de España
                GeoPoint(40.4252777778, -3.70833333333), // Malasaña
                GeoPoint(40.420024, -3.688727), // Puerta de Alcalá
                GeoPoint(40.413891, -3.691890), // Museo del prado
                GeoPoint(40.414509, -3.682737), // El Retiro
                GeoPoint(40.406740, -3.692668), // Restaurante Asturias
                GeoPoint(40.402998, -3.696018), // Bar Las Carabelas
                GeoPoint(40.399409, -3.698589), // Tapería La Pequeña Graná
                GeoPoint(40.42, -3.7061), // Callao
                GeoPoint(	40.42291, -3.75595), // Casa de Campo
                GeoPoint(40.727222222222, -3.8611111111111), // Manzanares el Real
            )
            val gymkhanaNames = listOf(
                "Plaza de España",
                "Malasaña",
                "Puerta de Alcala",
                "Museo del Prado",
                "El Retiro",
                "Restaurante Asturias",
                "Bar Las Carabelas",
                "Taperia La Pequena Grana",
                "Callao",
                "Casa de Campo",
                "Manzanares el Real",
            )
            map.controller.setCenter(madridGeoPoint)
            addMarkersAndRoute(map, gymkhanaCoords, gymkhanaNames)
        }

    }

    fun addMarkersAndRoute(mapView: MapView, locationsCoords: List<GeoPoint>, locationsNames: List<String>) {
        if (locationsCoords.size != locationsNames.size) {
            Log.e("addMarkersAndRoute", "Locations and names lists must have the same number of items.")
            return
        }

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

        val route = Polyline().also {
            it.color = ContextCompat.getColor(this, R.color.teal_700)
            it.setPoints(locationsCoords)
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
        mapView.invalidate()


    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }
    override fun onPause() {
        super.onPause()
        map.onPause()
    }

}
