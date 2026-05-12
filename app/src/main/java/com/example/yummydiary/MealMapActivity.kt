package com.example.yummydiary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.yummydiary.ui.theme.PrimaryDark
import com.example.yummydiary.ui.theme.YummyDiaryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MealMapActivity : ComponentActivity() {

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocationPermissions()

        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            YummyDiaryTheme {
                MapScreen(
                    onBack = { finish() },
                    onAddMeal = {
                        startActivity(Intent(this, AddMealActivity::class.java))
                    },
                    onPlaceSelected = { place ->
                        val intent = Intent(this, AddMealActivity::class.java).apply {
                            putExtra("RESTAURANT_ID", place.id)
                            putExtra("RESTAURANT_NAME", place.name)
                            putExtra("RESTAURANT_ADDRESS", place.address)
                            putExtra("RESTAURANT_CATEGORY", place.category)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit, onAddMeal: () -> Unit, onPlaceSelected: (Place) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val mapView = remember { MapView(context) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }

    // Obsługa cyklu życia mapy
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MAPA DAŃ", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = {
                        isFetching = true
                        val center = GeoPoint(mapView.mapCenter.latitude, mapView.mapCenter.longitude)
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val places = fetchNearbyPlaces(center)
                                withContext(Dispatchers.Main) {
                                    isFetching = false
                                    addMarkers(mapView, places) { place ->
                                        selectedPlace = place
                                        showBottomSheet = true
                                    }
                                    Toast.makeText(context, "Znaleziono ${places.size} miejsc", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isFetching = false
                                    Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    containerColor = PrimaryDark,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Szukaj tutaj")
                }
                FloatingActionButton(
                    onClick = onAddMeal,
                    containerColor = PrimaryDark,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = {
                    mapView.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        locationOverlay.enableMyLocation()
                        locationOverlay.runOnFirstFix {
                            val myLocation = locationOverlay.myLocation
                            if (myLocation != null) {
                                post {
                                    controller.setCenter(myLocation)
                                    isFetching = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val places = fetchNearbyPlaces(myLocation)
                                            withContext(Dispatchers.Main) {
                                                isFetching = false
                                                addMarkers(this@apply, places) { place ->
                                                    selectedPlace = place
                                                    showBottomSheet = true
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isFetching = false
                                                Toast.makeText(context, "Błąd pobierania: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        overlays.add(locationOverlay)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isFetching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (showBottomSheet && selectedPlace != null) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = selectedPlace!!.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedPlace!!.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (selectedPlace!!.address.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedPlace!!.address,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                showBottomSheet = false
                                onPlaceSelected(selectedPlace!!)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryDark,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Dodaj danie")
                        }
                    }
                }
            }
        }
    }
}

private fun fetchNearbyPlaces(center: GeoPoint): List<Place> {
    return try {
        val query = """
            [out:json][timeout:25];
            (
              node["amenity"~"restaurant|cafe|fast_food|pub|bar|ice_cream|bakery"](around:2000,${center.latitude},${center.longitude});
              way["amenity"~"restaurant|cafe|fast_food|pub|bar|ice_cream|bakery"](around:2000,${center.latitude},${center.longitude});
            );
            out center;
        """.trimIndent()
        Log.d("MealMapActivity", "Sending Overpass POST request with query: $query")

        val url = URL("https://overpass-api.de/api/interpreter")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("User-Agent", "YummyDiary/1.0 (https://github.com/mintr/YummyDiary)")
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val postData = "data=${URLEncoder.encode(query, "UTF-8")}"
        connection.outputStream.use { os ->
            os.write(postData.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        Log.d("MealMapActivity", "Response Code: $responseCode")
        
        if (responseCode != HttpURLConnection.HTTP_OK) {
            val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
            Log.e("MealMapActivity", "Error response: $errorStream")
            return emptyList()
        }

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        Log.d("MealMapActivity", "Response received, length: ${response.length}")
        val json = JSONObject(response)
        val elements = json.getJSONArray("elements")

        val list = mutableListOf<Place>()
        for (i in 0 until elements.length()) {
            val obj = elements.getJSONObject(i)
            val id = obj.getLong("id")
            val lat = if (obj.has("lat")) obj.getDouble("lat") else obj.getJSONObject("center").getDouble("lat")
            val lon = if (obj.has("lon")) obj.getDouble("lon") else obj.getJSONObject("center").getDouble("lon")
            val tags = obj.optJSONObject("tags")
            
            val name = tags?.optString("name") 
                ?: tags?.optString("brand")
                ?: tags?.optString("operator")
                ?: "Gastronomia"
                
            val amenity = tags?.optString("amenity")
                ?: tags?.optString("cuisine")
                ?: "punkt"
                
            val street = tags?.optString("addr:street", "") ?: ""
            val houseNum = tags?.optString("addr:housenumber", "") ?: ""
            val address = if (street.isNotEmpty()) "$street $houseNum".trim() else ""
            
            list.add(Place(id, GeoPoint(lat, lon), name, amenity, address))
        }
        Log.d("MealMapActivity", "Found ${list.size} places")
        list
    } catch (e: Exception) {
        Log.e("MealMapActivity", "Error fetching places", e)
        emptyList()
    }
}

private fun addMarkers(mapView: MapView, places: List<Place>, onMarkerClick: (Place) -> Unit) {
    // Usuń stare markery, ale zostaw warstwę lokalizacji
    val overlaysToKeep = mapView.overlays.filter { it is MyLocationNewOverlay }
    mapView.overlays.clear()
    mapView.overlays.addAll(overlaysToKeep)

    for (place in places) {
        val marker = Marker(mapView)
        marker.position = place.geoPoint
        marker.title = place.name
        marker.subDescription = place.category
        marker.infoWindow = null // Używamy własnego BottomSheet
        marker.setOnMarkerClickListener { _, _ ->
            onMarkerClick(place)
            true
        }
        mapView.overlays.add(marker)
    }
    mapView.invalidate()
}

data class Place(val id: Long, val geoPoint: GeoPoint, val name: String, val category: String, val address: String)
