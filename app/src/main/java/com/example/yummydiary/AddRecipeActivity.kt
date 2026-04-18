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
    private val ingredientEdits = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)
        setToolbarTitle("Dodaj nowy przepis")

        val mealName = intent.getStringExtra("MEAL_NAME") ?: ""
        val restaurantName = intent.getStringExtra("RESTAURANT_NAME") ?: ""

        findViewById<TextView>(R.id.tvMealNameDisplay).text = mealName
        findViewById<TextView>(R.id.tvRestaurantNameDisplay).text = restaurantName

        llIngredientsContainer = findViewById(R.id.llIngredientsContainer)
        val btnAddIngredient = findViewById<Button>(R.id.btnAddIngredient)
        val btnFinalSave = findViewById<Button>(R.id.btnFinalSave)
        val etInstructions = findViewById<EditText>(R.id.etInstructions)

        // Dodaj pierwsze pole składnika na starcie
        addNewIngredientField()

        btnAddIngredient.setOnClickListener { addNewIngredientField() }

        btnFinalSave.setOnClickListener {
            saveRecipeAndFinish(etInstructions.text.toString())
        }
    }

    private fun addNewIngredientField() {
        val editText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8.toPx())
            }
            hint = "Składnik ${ingredientEdits.size + 1}"
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

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Dodaj przynajmniej jeden składnik", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val recipe = Recipe(ingredients = ingredients, instructions = instructions)
            val recipeId = AppDatabase.getDatabase(this@AddRecipeActivity).recipeDao().insertRecipe(recipe)
            
            // Zwróć ID przepisu do AddMealActivity
            intent.putExtra("RECIPE_ID", recipeId.toInt())
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun Int.toPx(): Int = (this * resources.displayMetrics.density).toInt()
}