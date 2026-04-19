package com.example.yummydiary

import android.os.Bundle

import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddRecipeActivity : BaseActivity() {
    private lateinit var llIngredientsContainer: LinearLayout
    private lateinit var etMealNameRecipe: EditText
    private lateinit var tvMealNameDisplay: TextView
    private lateinit var tvRestaurantNameDisplay: TextView
    private lateinit var etInstructions: EditText
    private val ingredientEdits = mutableListOf<EditText>()
    private var editingRecipeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)
        
        editingRecipeId = intent.getIntExtra("RECIPE_ID", -1).takeIf { it != -1 }
        
        if (editingRecipeId != null) {
            setToolbarTitle("Edytuj przepis")
        } else {
            setToolbarTitle("Dodaj nowy przepis")
        }

        val mealName = intent.getStringExtra("MEAL_NAME")
        val restaurantName = intent.getStringExtra("RESTAURANT_NAME")

        etMealNameRecipe = findViewById(R.id.etMealNameRecipe)
        tvMealNameDisplay = findViewById(R.id.tvMealNameDisplay)
        tvRestaurantNameDisplay = findViewById(R.id.tvRestaurantNameDisplay)

        if (mealName.isNullOrEmpty() && editingRecipeId == null) {
            etMealNameRecipe.visibility = android.view.View.VISIBLE
            tvMealNameDisplay.visibility = android.view.View.GONE
            tvRestaurantNameDisplay.text = "Własne danie"
        } else {
            // Sprawdzamy czy to edycja "Własnego dania" bez restauracji
            if (editingRecipeId != null && restaurantName.isNullOrEmpty()) {
                etMealNameRecipe.visibility = android.view.View.VISIBLE
                tvMealNameDisplay.visibility = android.view.View.GONE
                etMealNameRecipe.setText(mealName ?: "")
                tvRestaurantNameDisplay.text = "Własne danie"
            } else {
                etMealNameRecipe.visibility = android.view.View.GONE
                tvMealNameDisplay.visibility = android.view.View.VISIBLE
                tvMealNameDisplay.text = mealName ?: ""
                tvRestaurantNameDisplay.text = restaurantName ?: ""
            }
        }

        llIngredientsContainer = findViewById(R.id.llIngredientsContainer)
        val btnAddIngredient = findViewById<Button>(R.id.btnAddIngredient)
        val btnFinalSave = findViewById<Button>(R.id.btnFinalSave)
        etInstructions = findViewById(R.id.etInstructions)

        if (editingRecipeId != null) {
            loadRecipeDataForEditing()
        }

        btnAddIngredient.setOnClickListener { addNewIngredientField() }

        btnFinalSave.setOnClickListener {
            saveRecipeAndFinish(etInstructions.text.toString())
        }
    }

    private fun loadRecipeDataForEditing() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddRecipeActivity)
            val recipe = db.recipeDao().getRecipeById(editingRecipeId!!)
            recipe?.let {
                etInstructions.setText(it.instructions)
                
                // Pobieramy dane powiązanego posiłku, aby wypełnić pola nazwy/restauracji
                val allRecipes = db.recipeDao().getAllRecipesWithMeals()
                val recipeWithMeal = allRecipes.find { r -> r.recipe.id == editingRecipeId }
                val meal = recipeWithMeal?.meal

                if (meal != null) {
                    if (meal.restaurantName.isEmpty()) {
                        etMealNameRecipe.visibility = android.view.View.VISIBLE
                        tvMealNameDisplay.visibility = android.view.View.GONE
                        etMealNameRecipe.setText(meal.mealName)
                        tvRestaurantNameDisplay.text = "Własne danie"
                    } else {
                        etMealNameRecipe.visibility = android.view.View.GONE
                        tvMealNameDisplay.visibility = android.view.View.VISIBLE
                        tvMealNameDisplay.text = meal.mealName
                        tvRestaurantNameDisplay.text = meal.restaurantName
                    }
                }

                // Wypełnianie składników
                llIngredientsContainer.removeAllViews()
                ingredientEdits.clear()
                val ingredientsList = it.ingredients.split("\n")
                ingredientsList.forEach { ingredient ->
                    if (ingredient.isNotBlank()) {
                        addNewIngredientField(ingredient)
                    }
                }
                if (ingredientEdits.isEmpty()) addNewIngredientField()
            }
        }
    }

    private fun addNewIngredientField(text: String = "") {
        val editText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8.toPx())
            }
            hint = "Składnik ${ingredientEdits.size + 1}"
            setText(text)
            setPadding(12.toPx(), 12.toPx(), 12.toPx(), 12.toPx())
            setBackgroundResource(R.drawable.bg_edittext)
            setTextColor(getColor(R.color.black))
            setHintTextColor(android.graphics.Color.parseColor("#999999"))
        }
        llIngredientsContainer.addView(editText)
        ingredientEdits.add(editText)
    }

    private fun saveRecipeAndFinish(instructions: String) {
        val ingredients = ingredientEdits.map { it.text.toString() }
            .filter { it.isNotBlank() }
            .joinToString("\n")

        val customMealName = etMealNameRecipe.text.toString()

        if (etMealNameRecipe.visibility == android.view.View.VISIBLE && customMealName.isBlank()) {
            Toast.makeText(this, "Podaj nazwę dania", Toast.LENGTH_SHORT).show()
            return
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Dodaj przynajmniej jeden składnik", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddRecipeActivity)
            val recipe = Recipe(
                id = editingRecipeId ?: 0,
                ingredients = ingredients,
                instructions = instructions
            )
            
            val recipeId: Long = if (editingRecipeId == null) {
                db.recipeDao().insertRecipe(recipe)
            } else {
                db.recipeDao().updateRecipe(recipe)
                editingRecipeId!!.toLong()
            }

            // Jeśli dodajemy/edytujemy przepis bezpośrednio (nie przez AddMealActivity)
            if (etMealNameRecipe.visibility == android.view.View.VISIBLE) {
                // Szukamy czy istnieje już Meal powiązany z tym przepisem
                val allMeals = db.mealDao().getMealsWithRecipes()
                val existingMeal = if (editingRecipeId != null) {
                    allMeals.find { it.recipeId == editingRecipeId }
                } else null

                val meal = Meal(
                    id = existingMeal?.id ?: 0,
                    restaurantName = "",
                    restaurantAddress = "",
                    mealName = customMealName,
                    category = "Własne",
                    description = "Przepis",
                    rating = existingMeal?.rating ?: 5f,
                    date = existingMeal?.date ?: System.currentTimeMillis(),
                    imagePath = existingMeal?.imagePath,
                    recipeId = recipeId.toInt()
                )
                
                if (existingMeal == null) {
                    db.mealDao().insertMeal(meal)
                } else {
                    db.mealDao().updateMeal(meal)
                }
                Toast.makeText(this@AddRecipeActivity, "Zapisano!", Toast.LENGTH_SHORT).show()
            }
            
            val resultIntent = android.content.Intent()
            resultIntent.putExtra("RECIPE_ID", recipeId.toInt())
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()
}