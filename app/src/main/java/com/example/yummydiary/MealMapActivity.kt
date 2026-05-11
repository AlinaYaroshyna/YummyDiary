package com.example.yummydiary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MealMapActivity : BaseActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var hasCentered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_meal_map)
        setToolbarTitle("Mapa dań")

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(15.0)

        // Setup User Location Overlay
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                if (!hasCentered) {
                    val myLocation = locationOverlay.myLocation
                    if (myLocation != null) {
                        mapController.setCenter(myLocation)
                        hasCentered = true
                        fetchNearbyPlaces(myLocation)
                    }
                }
            }
        }
        map.overlays.add(locationOverlay)

        // Default center if location not available yet
        val wroclaw = GeoPoint(51.1099, 17.0318)
        mapController.setCenter(wroclaw)

        findViewById<Button>(R.id.btnAddMeal).setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun fetchNearbyPlaces(center: GeoPoint) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Overpass API Query: find restaurants, cafes, fast_food, pubs, bars within 2000m (2 km)
                val query = """
                    [out:json];
                    (
                      node["amenity"~"restaurant|cafe|fast_food|pub|bar"](around:2000,${center.latitude},${center.longitude});
                      way["amenity"~"restaurant|cafe|fast_food|pub|bar"](around:2000,${center.latitude},${center.longitude});
                    );
                    out center;
                """.trimIndent()

                val url = URL("https://overpass-api.de/api/interpreter?data=${URLEncoder.encode(query, "UTF-8")}")
                val connection = url.openConnection() as HttpURLConnection
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val elements = json.getJSONArray("elements")

                val places = mutableListOf<Place>()
                for (i in 0 until elements.length()) {
                    val obj = elements.getJSONObject(i)
                    val id = obj.getLong("id")
                    val lat = if (obj.has("lat")) obj.getDouble("lat") else obj.getJSONObject("center").getDouble("lat")
                    val lon = if (obj.has("lon")) obj.getDouble("lon") else obj.getJSONObject("center").getDouble("lon")
                    
                    val tags = if (obj.has("tags")) obj.getJSONObject("tags") else null
                    val name = if (tags != null && tags.has("name")) tags.getString("name") else "Gastronomia"
                    val amenity = if (tags != null && tags.has("amenity")) tags.getString("amenity") else "punkt"
                    
                    // Simple address extraction from OSM tags
                    val street = tags?.optString("addr:street", "") ?: ""
                    val houseNumber = tags?.optString("addr:housenumber", "") ?: ""
                    val city = tags?.optString("addr:city", "") ?: ""
                    val address = listOf(street, houseNumber, city).filter { it.isNotEmpty() }.joinToString(" ")
                    
                    places.add(Place(id, GeoPoint(lat, lon), name, amenity, address))
                }

                withContext(Dispatchers.Main) {
                    addMarkersToMap(places)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addMarkersToMap(places: List<Place>) {
        for (place in places) {
            val marker = Marker(map)
            marker.position = place.geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            
            // Create a custom InfoWindow
            val infoWindow = object : MarkerInfoWindow(R.layout.map_info_window, map) {
                override fun onOpen(item: Any?) {
                    val m = item as Marker
                    val title = mView.findViewById<TextView>(R.id.bubble_title)
                    val description = mView.findViewById<TextView>(R.id.bubble_description)
                    val btnAdd = mView.findViewById<Button>(R.id.bubble_button)
                    
                    title.text = place.name
                    description.text = "Kategoria: ${place.category}\n${place.address}"
                    
                    btnAdd.setOnClickListener {
                        val intent = Intent(this@MealMapActivity, AddMealActivity::class.java).apply {
                            putExtra("RESTAURANT_ID", place.id)
                            putExtra("RESTAURANT_NAME", place.name)
                            putExtra("RESTAURANT_ADDRESS", place.address)
                            putExtra("RESTAURANT_CATEGORY", place.category)
                        }
                        startActivity(intent)
                        m.closeInfoWindow()
                    }
                }
            }
            
            marker.infoWindow = infoWindow
            marker.title = place.name
            
            // Create a small red dot as the icon
            val dot = GradientDrawable()
            dot.shape = GradientDrawable.OVAL
            dot.setColor(Color.RED)
            dot.setSize(30, 30)
            marker.icon = dot
            
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    data class Place(val id: Long, val geoPoint: GeoPoint, val name: String, val category: String, val address: String)

    override fun onResume() {
        super.onResume()
        map.onResume()
        locationOverlay.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        locationOverlay.disableMyLocation()
    }
}
