package com.example.yummydiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.content.Intent
import android.widget.Button
import android.widget.ImageButton

class MealMapActivity : BaseActivity() {

    private lateinit var map: MapView

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

        val wroclaw = GeoPoint(51.1099, 17.0318)
        mapController.setCenter(wroclaw)

        val marker = Marker(map)
        marker.position = wroclaw
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Wrocław — Rynek"
        map.overlays.add(marker)
        map.invalidate()
        findViewById<Button>(R.id.btnAddMeal).setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
        }
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause()  { super.onPause();  map.onPause()  }
}