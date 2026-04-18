package com.example.yummydiary

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RecipeDetailsActivity : BaseActivity() {

    private lateinit var tvRecipeMealName: TextView
    private lateinit var tvRecipeRestaurantName: TextView
    private lateinit var tvRecipeIngredients: TextView
    private lateinit var tvRecipeInstructions: TextView
    private lateinit var tvRecipeInstructionsLabel: TextView

    private lateinit var database: AppDatabase
    private var recipeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)
        setToolbarTitle("Szczegóły przepisu")

        database = AppDatabase.getDatabase(this)
        recipeId = intent.getIntExtra("RECIPE_ID", -1)

        initializeViews()
        loadRecipeDetails()
    }

    private fun initializeViews() {
        tvRecipeMealName = findViewById(R.id.tvRecipeMealName)
        tvRecipeRestaurantName = findViewById(R.id.tvRecipeRestaurantName)
        tvRecipeIngredients = findViewById(R.id.tvRecipeIngredients)
        tvRecipeInstructions = findViewById(R.id.tvRecipeInstructions)
        tvRecipeInstructionsLabel = findViewById(R.id.tvRecipeInstructionsLabel)
    }

    private fun loadRecipeDetails() {
        if (recipeId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@RecipeDetailsActivity)
            // Pobieramy przepis wraz z powiązanym daniem, aby wyświetlić nazwę i restaurację
            val allRecipes = db.recipeDao().getAllRecipesWithMeals()
            val recipeWithMeal = allRecipes.find { it.recipe.id == recipeId }

            if (recipeWithMeal != null) {
                displayRecipe(recipeWithMeal)
            } else {
                finish()
            }
        }
    }

    private fun displayRecipe(recipeWithMeal: RecipeWithMeal) {
        tvRecipeMealName.text = recipeWithMeal.meal?.mealName ?: "Nieznane danie"
        tvRecipeRestaurantName.text = recipeWithMeal.meal?.restaurantName ?: ""
        tvRecipeIngredients.text = recipeWithMeal.recipe.ingredients
        
        val instructions = recipeWithMeal.recipe.instructions
        if (instructions.isNullOrBlank()) {
            tvRecipeInstructionsLabel.visibility = View.GONE
            tvRecipeInstructions.visibility = View.GONE
        } else {
            tvRecipeInstructionsLabel.visibility = View.VISIBLE
            tvRecipeInstructions.visibility = View.VISIBLE
            tvRecipeInstructions.text = instructions
        }
    }
}