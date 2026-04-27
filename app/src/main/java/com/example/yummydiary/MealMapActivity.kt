package com.example.yummydiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.content.Intent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MealMapActivity : BaseActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Wymagana zgoda na lokalizację, aby pokazać Cię na mapie", Toast.LENGTH_SHORT).show()
        }
    }

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

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        map.overlays.add(locationOverlay)

        checkLocationPermissions()

        val wroclaw = GeoPoint(51.1099, 17.0318)
        mapController.setCenter(wroclaw)

        loadMealMarkers()

        findViewById<Button>(R.id.btnAddMeal).setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
        }
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    private fun enableMyLocation() {
        locationOverlay.enableMyLocation()
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                map.controller.animateTo(locationOverlay.myLocation)
            }
        }
    }

    private fun loadMealMarkers() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@MealMapActivity)
            val meals = database.mealDao().getAllMeals()
            
            if (meals.isEmpty()) {
                // Toast.makeText(this@MealMapActivity, "Brak dań z lokalizacją w bazie", Toast.LENGTH_SHORT).show()
            }

            meals.forEach { meal ->
                if (meal.latitude != null && meal.longitude != null) {
                    val marker = Marker(map)
                    marker.position = GeoPoint(meal.latitude, meal.longitude)
                    marker.title = meal.restaurantName
                    marker.snippet = "${meal.mealName} - Ocena: ${meal.rating}"
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    // Opcjonalnie: ikona markera
                    // marker.icon = ContextCompat.getDrawable(this@MealMapActivity, R.drawable.ic_marker)
                    
                    map.overlays.add(marker)
                }
            }
            map.invalidate()
        }
    }

    override fun onResume() { 
        super.onResume()
        map.onResume() 
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationOverlay.enableMyLocation()
        }
    }
    override fun onPause()  { 
        super.onPause()
        map.onPause()
        locationOverlay.disableMyLocation()
    }
}