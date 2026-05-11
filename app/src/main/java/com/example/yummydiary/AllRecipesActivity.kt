package com.example.yummydiary

import android.os.Bundle
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class AllRecipesActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var cgCategories: ChipGroup
    private lateinit var cgRestaurants: ChipGroup
    private lateinit var etSearch: EditText
    private var allRecipes: List<RecipeWithMeal> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_recipes)
        setToolbarTitle("Wszystkie przepisy")

        recyclerView = findViewById(R.id.rvRecipes)
        cgCategories = findViewById(R.id.cgRecipeCategories)
        cgRestaurants = findViewById(R.id.cgRecipeRestaurants)
        etSearch = findViewById(R.id.etSearchRecipes)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(emptyList()) { recipeWithMeal ->
            val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
                putExtra("RECIPE_ID", recipeWithMeal.recipe.id)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        setupSearch()
        loadRecipes()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilters()
            }
        })
    }

    private fun loadRecipes() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AllRecipesActivity)
            allRecipes = db.recipeDao().getAllRecipesWithMeals()
            adapter.updateData(allRecipes)
            setupCategories()
            setupRestaurants()
        }
    }

    private fun setupCategories() {
        val categories = allRecipes.flatMap { it.meal?.category?.split(Regex(",\\s*")) ?: emptyList() }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        cgCategories.removeAllViews()

        categories.forEach { category ->
            val chip = createChip(category, cgCategories)
            chip.id = View.generateViewId()
            cgCategories.addView(chip)
        }

        cgCategories.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun setupRestaurants() {
        val restaurants = allRecipes.mapNotNull { it.meal?.restaurantName }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        cgRestaurants.removeAllViews()

        restaurants.forEach { restaurant ->
            val chip = createChip(restaurant, cgRestaurants)
            chip.id = View.generateViewId()
            cgRestaurants.addView(chip)
        }

        cgRestaurants.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = etSearch.text.toString().trim().lowercase()
        
        val selectedCategoryIds = cgCategories.checkedChipIds
        val selectedCategories = selectedCategoryIds.map { id ->
            cgCategories.findViewById<Chip>(id).text.toString()
        }

        val selectedRestaurantIds = cgRestaurants.checkedChipIds
        val selectedRestaurants = selectedRestaurantIds.map { id ->
            cgRestaurants.findViewById<Chip>(id).text.toString()
        }

        val filtered = allRecipes.filter { item ->
            val matchesSearch = if (query.isEmpty()) true else {
                val mealName = item.meal?.mealName?.lowercase() ?: ""
                val ingredients = item.recipe.ingredients.lowercase()
                mealName.contains(query) || ingredients.contains(query)
            }

            val matchesCategory = if (selectedCategories.isEmpty()) true else {
                val mealCategories = item.meal?.category?.split(Regex(",\\s*"))?.map { it.trim() } ?: emptyList()
                selectedCategories.any { label -> mealCategories.contains(label) }
            }

            val matchesRestaurant = if (selectedRestaurants.isEmpty()) true else {
                val restaurantName = item.meal?.restaurantName ?: ""
                selectedRestaurants.contains(restaurantName)
            }

            matchesSearch && matchesCategory && matchesRestaurant
        }
        adapter.updateData(filtered)
    }

    private fun createChip(label: String, parent: ChipGroup): Chip {
        val chip = LayoutInflater.from(this).inflate(R.layout.view_filter_chip, parent, false) as Chip
        chip.text = label
        return chip
    }
}