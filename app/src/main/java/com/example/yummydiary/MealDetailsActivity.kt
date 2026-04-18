package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MealDetailsActivity : BaseActivity() {

    private lateinit var ivMealDetail: ImageView
    private lateinit var tvMealNameDetail: TextView
    private lateinit var tvRestaurantDetail: TextView
    private lateinit var tvAddressDetail: TextView
    private lateinit var tvCategoryDetail: TextView
    private lateinit var ratingBarDetail: RatingBar
    private lateinit var tvDescriptionDetail: TextView
    private lateinit var tvDateDetail: TextView
    private lateinit var btnDeleteMeal: Button
    private lateinit var btnViewRecipe: Button
    
    private lateinit var database: AppDatabase
    private var mealId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_details)
        setToolbarTitle("Szczegóły dania")

        database = AppDatabase.getDatabase(this)
        mealId = intent.getIntExtra("MEAL_ID", -1)

        initializeViews()
        loadMealDetails()

        btnDeleteMeal.setOnClickListener {
            deleteMeal()
        }
    }

    private fun initializeViews() {
        ivMealDetail = findViewById(R.id.ivMealDetail)
        tvMealNameDetail = findViewById(R.id.tvMealNameDetail)
        tvRestaurantDetail = findViewById(R.id.tvRestaurantDetail)
        tvAddressDetail = findViewById(R.id.tvAddressDetail)
        tvCategoryDetail = findViewById(R.id.tvCategoryDetail)
        ratingBarDetail = findViewById(R.id.ratingBarDetail)
        tvDescriptionDetail = findViewById(R.id.tvDescriptionDetail)
        tvDateDetail = findViewById(R.id.tvDateDetail)
        btnDeleteMeal = findViewById(R.id.btnDeleteMeal)
        btnViewRecipe = findViewById(R.id.btnViewRecipe)
    }

    private fun loadMealDetails() {
        if (mealId == -1) {
            Toast.makeText(this, "Błąd ładowania danych", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val meal = database.mealDao().getMealById(mealId)

            if (meal != null) {
                displayMeal(meal)
            } else {
                Toast.makeText(this@MealDetailsActivity, "Nie znaleziono dania", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayMeal(meal: Meal) {
        tvMealNameDetail.text = meal.mealName
        tvRestaurantDetail.text = meal.restaurantName
        tvAddressDetail.text = meal.restaurantAddress
        tvCategoryDetail.text = meal.category
        ratingBarDetail.rating = meal.rating
        tvDescriptionDetail.text = meal.description

        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("pl"))
        tvDateDetail.text = "Dodano: ${sdf.format(Date(meal.date))}"

        if (meal.imagePath != null) {
            try {
                val uri = android.net.Uri.parse(meal.imagePath)
                ivMealDetail.setImageDrawable(null)
                ivMealDetail.setImageURI(uri)
            } catch (e: Exception) {
                ivMealDetail.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            ivMealDetail.setImageResource(R.drawable.ic_launcher_background)
        }

        // Obsługa przycisku przepisu
        if (meal.recipeId != null) {
            btnViewRecipe.visibility = View.VISIBLE
            btnViewRecipe.setOnClickListener {
                val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
                    putExtra("RECIPE_ID", meal.recipeId)
                }
                startActivity(intent)
            }
        } else {
            btnViewRecipe.visibility = View.GONE
        }
    }

    private fun deleteMeal() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Usuń danie")
            .setMessage("Czy na pewno chcesz usunąć to danie z dziennika?")
            .setPositiveButton("Usuń") { _, _ ->
                lifecycleScope.launch {
                    database.mealDao().deleteMealById(mealId)
                    Toast.makeText(this@MealDetailsActivity, "Danie usunięte", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}