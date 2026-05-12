package com.example.yummydiary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.yummydiary.ui.theme.*

class AddMealActivity : ComponentActivity() {

    private val viewModel: MealViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        MealViewModel.Factory(MealRepository(database.mealDao()))
    }

    private var selectedImageUri by mutableStateOf<String?>(null)
    private var tempImageUri by mutableStateOf<Uri?>(null)
    private var currentRecipeId by mutableStateOf<Int?>(null)

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            selectedImageUri = it.toString()
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            selectedImageUri = tempImageUri.toString()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Wymagane uprawnienie do aparatu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCamera() {
        val uri = createImageUri()
        if (uri != null) {
            tempImageUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createImageUri(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    }

    private val addRecipeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            currentRecipeId = result.data?.getIntExtra("RECIPE_ID", -1)?.takeIf { it != -1 }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadAllData()
        
        val editingMealId = intent.getIntExtra("MEAL_ID", -1).takeIf { it != -1 }
        if (editingMealId != null) {
            viewModel.loadMealById(editingMealId)
        }

        setContent {
            YummyDiaryTheme {
                val mealToEdit by viewModel.selectedMeal.collectAsState()
                val categories by viewModel.categories.collectAsState()
                
                // Initialize state from mealToEdit if available
                var mealName by remember { mutableStateOf("") }
                var restaurantName by remember { mutableStateOf("") }
                var restaurantAddress by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var rating by remember { mutableStateOf(0f) }
                var selectedCats by remember { mutableStateOf(setOf<String>()) }

                LaunchedEffect(mealToEdit) {
                    mealToEdit?.let {
                        mealName = it.mealName
                        restaurantName = it.restaurantName
                        restaurantAddress = it.restaurantAddress
                        description = it.description
                        rating = it.rating
                        selectedImageUri = it.imagePath
                        currentRecipeId = it.recipeId
                        selectedCats = it.category.split(", ").filter { it.isNotEmpty() }.toSet()
                    }
                }

                // If coming from map
                LaunchedEffect(Unit) {
                    if (editingMealId == null) {
                        intent.getStringExtra("RESTAURANT_NAME")?.let { restaurantName = it }
                        intent.getStringExtra("RESTAURANT_ADDRESS")?.let { restaurantAddress = it }
                    }
                }

                AddMealScreen(
                    mealName = mealName,
                    onMealNameChange = { mealName = it },
                    restaurantName = restaurantName,
                    onRestaurantNameChange = { restaurantName = it },
                    restaurantAddress = restaurantAddress,
                    onRestaurantAddressChange = { restaurantAddress = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    rating = rating,
                    onRatingChange = { rating = it },
                    categories = categories,
                    selectedCats = selectedCats,
                    onCategoryToggle = { cat ->
                        selectedCats = if (cat in selectedCats) selectedCats - cat else selectedCats + cat
                    },
                    imageUri = selectedImageUri,
                    onSelectImage = { selectImageLauncher.launch("image/*") },
                    onTakePicture = {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            launchCamera()
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onAddRecipe = {
                        val intent = Intent(this, AddRecipeActivity::class.java).apply {
                            putExtra("MEAL_NAME", mealName)
                            putExtra("RESTAURANT_NAME", restaurantName)
                            currentRecipeId?.let { putExtra("RECIPE_ID", it) }
                        }
                        addRecipeLauncher.launch(intent)
                    },
                    hasRecipe = currentRecipeId != null,
                    onSave = {
                        val meal = Meal(
                            id = editingMealId ?: 0,
                            restaurantName = restaurantName,
                            restaurantAddress = restaurantAddress,
                            mealName = mealName,
                            category = selectedCats.joinToString(", "),
                            description = description,
                            rating = rating,
                            date = mealToEdit?.date ?: System.currentTimeMillis(),
                            imagePath = selectedImageUri,
                            recipeId = currentRecipeId
                        )
                        viewModel.saveMeal(meal) {
                            if (editingMealId == null) {
                                playAddMealSound()
                                startActivity(Intent(this@AddMealActivity, DiaryActivity::class.java))
                            }
                            finish()
                        }
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun playAddMealSound() {
        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.youpi)
            mediaPlayer.setOnCompletionListener { mp -> mp.release() }
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            val isSelected = i <= rating
            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Gwiazdka $i",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChange(i.toFloat()) },
                tint = if (isSelected) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMealScreen(
    mealName: String, onMealNameChange: (String) -> Unit,
    restaurantName: String, onRestaurantNameChange: (String) -> Unit,
    restaurantAddress: String, onRestaurantAddressChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    rating: Float, onRatingChange: (Float) -> Unit,
    categories: List<String>,
    selectedCats: Set<String>,
    onCategoryToggle: (String) -> Unit,
    imageUri: String?,
    onSelectImage: () -> Unit,
    onTakePicture: () -> Unit,
    onAddRecipe: () -> Unit,
    hasRecipe: Boolean,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var showImageDialog by remember { mutableStateOf(false) }

    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Dodaj zdjęcie") },
            text = { Text("Wybierz źródło zdjęcia:") },
            confirmButton = {
                TextButton(onClick = {
                    showImageDialog = false
                    onSelectImage()
                }) { Text("Galeria") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageDialog = false
                    onTakePicture()
                }) { Text("Aparat") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DODAJ DANIE", fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = PrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryRed,
                    titleContentColor = PrimaryDark
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clickable { showImageDialog = true },
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = { showImageDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = PrimaryDark
                )
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (imageUri != null) "Zmień zdjęcie" else "Dodaj zdjęcie")
            }

            OutlinedTextField(value = mealName, onValueChange = onMealNameChange, label = { Text("Nazwa dania") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = restaurantName, onValueChange = onRestaurantNameChange, label = { Text("Restauracja") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = restaurantAddress, onValueChange = onRestaurantAddressChange, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth())
            
            Text("Kategorie:", modifier = Modifier.align(Alignment.Start))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = cat in selectedCats,
                        onClick = { onCategoryToggle(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryRed,
                            selectedLabelColor = PrimaryDark,
                            containerColor = Color.Transparent,
                            labelColor = if (isSystemInDarkTheme()) Color.White else PrimaryDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = cat in selectedCats,
                            borderColor = PrimaryRed.copy(alpha = 0.6f),
                            selectedBorderColor = PrimaryRed,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 2.dp
                        )
                    )
                }
            }

            Text("Ocena:", modifier = Modifier.align(Alignment.Start))
            StarRatingBar(rating = rating, onRatingChange = onRatingChange)

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Opis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = onAddRecipe,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = PrimaryDark
                )
            ) {
                Text(if (hasRecipe) "Przepis dodany ✓" else "Dodaj przepis")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = PrimaryDark
                )
            ) {
                Text("Zapisz danie")
            }
        }
    }
}
