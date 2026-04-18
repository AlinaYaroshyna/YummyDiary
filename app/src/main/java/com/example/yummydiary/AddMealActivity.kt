package com.example.yummydiary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddMealActivity : BaseActivity() {

    private lateinit var etRestaurantName: EditText
    private lateinit var etRestaurantAddress: EditText
    private lateinit var etMealName: EditText
    private lateinit var etDescription: EditText
    private lateinit var cgCategories: ChipGroup
    private lateinit var btnAddCategory: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var btnAddRecipe: Button
    private lateinit var btnSaveMeal: Button
    private lateinit var btnSelectPhoto: Button
    private lateinit var ivMealPhoto: ImageView
    private var selectedImageUri: String? = null
    private var currentRecipeId: Int? = null
    private var photoFile: File? = null

    private val categories = mutableListOf("Obiad", "Śniadanie", "Kolacja", "Deser")

    private val addRecipeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            currentRecipeId = result.data?.getIntExtra("RECIPE_ID", -1)
            if (currentRecipeId == -1) currentRecipeId = null
            btnAddRecipe.text = "Przepis dodany ✓"
            btnAddRecipe.isEnabled = false
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            showImage(it)
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            photoFile?.let {
                val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
                showImage(uri)
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Wymagana zgoda na aparat", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)
        setToolbarTitle("Dodaj nowe danie")

        initializeViews()
        setupCategories()

        btnSaveMeal.setOnClickListener {
            saveMeal()
        }

        btnSelectPhoto.setOnClickListener {
            showImageSourceDialog()
        }

        btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        btnAddRecipe.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java).apply {
                putExtra("MEAL_NAME", etMealName.text.toString())
                putExtra("RESTAURANT_NAME", etRestaurantName.text.toString())
            }
            addRecipeLauncher.launch(intent)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Galeria", "Aparat")
        android.app.AlertDialog.Builder(this)
            .setTitle("Wybierz źródło zdjęcia")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectImageLauncher.launch("image/*")
                    1 -> checkCameraPermissionAndLaunch()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()
        photoFile?.let {
            val photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePhotoLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun showImage(uri: Uri) {
        ivMealPhoto.visibility = View.VISIBLE
        ivMealPhoto.setImageURI(uri)
        selectedImageUri = uri.toString()
    }

    private fun initializeViews() {
        etRestaurantName = findViewById(R.id.etRestaurantName)
        etRestaurantAddress = findViewById(R.id.etRestaurantAddress)
        etMealName = findViewById(R.id.etMealName)
        etDescription = findViewById(R.id.etDescription)
        cgCategories = findViewById(R.id.cgCategories)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        ratingBar = findViewById(R.id.ratingBar)
        btnAddRecipe = findViewById(R.id.btnAddRecipe)
        btnSaveMeal = findViewById(R.id.btnSaveMeal)
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto)
        ivMealPhoto = findViewById(R.id.ivMealPhoto)
    }

    private fun setupCategories() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@AddMealActivity)
            val dbCategories = database.mealDao().getAllCategories()
            
            val allUniqueCategories = dbCategories.flatMap { it.split(", ") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()

            val finalCategories = (categories + allUniqueCategories).distinct()
            
            cgCategories.removeAllViews()
            finalCategories.forEach { category ->
                addCategoryChip(category)
            }
        }
    }

    private fun addCategoryChip(category: String) {
        val chip = Chip(this).apply {
            text = category
            isCheckable = true
            setChipBackgroundColorResource(R.color.white)
            setTextColor(getColor(R.color.black))
        }
        cgCategories.addView(chip)
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        android.app.AlertDialog.Builder(this)
            .setTitle("Nowa kategoria")
            .setView(input)
            .setPositiveButton("Dodaj") { _, _ ->
                val newCategory = input.text.toString()
                if (newCategory.isNotEmpty() && !categories.contains(newCategory)) {
                    categories.add(newCategory)
                    addCategoryChip(newCategory)
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun saveMeal() {
        val restaurantName = etRestaurantName.text.toString()
        val mealName = etMealName.text.toString()
        val description = etDescription.text.toString()
        
        val selectedCategories = mutableListOf<String>()
        for (i in 0 until cgCategories.childCount) {
            val chip = cgCategories.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedCategories.add(chip.text.toString())
            }
        }
        val categoryString = selectedCategories.joinToString(", ")

        if (mealName.isEmpty() || restaurantName.isEmpty()) {
            Toast.makeText(this, "Wypełnij nazwę restauracji i dania", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "Wybierz przynajmniej jedną kategorię", Toast.LENGTH_SHORT).show()
            return
        }

        val meal = Meal(
            restaurantName = restaurantName,
            restaurantAddress = etRestaurantAddress.text.toString(),
            mealName = mealName,
            category = categoryString,
            description = description,
            rating = ratingBar.rating,
            date = System.currentTimeMillis(),
            imagePath = selectedImageUri,
            recipeId = currentRecipeId
        )

        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@AddMealActivity)
            database.mealDao().insertMeal(meal)
            Toast.makeText(this@AddMealActivity, "Danie zapisane!", Toast.LENGTH_SHORT).show()
            
            val intent = Intent(this@AddMealActivity, DiaryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}
