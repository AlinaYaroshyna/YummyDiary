package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class DiaryActivity : BaseActivity() {

    private lateinit var rvMeals: RecyclerView
    private lateinit var mealAdapter: MealAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvEmptyState: TextView
    private lateinit var etSearch: EditText
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)
        setToolbarTitle("Dziennik dań")

        database = AppDatabase.getDatabase(this)
        
        rvMeals = findViewById(R.id.rvMeals)
        chipGroup = findViewById(R.id.chipGroupCategories)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        etSearch = findViewById(R.id.etSearchMeals)

        rvMeals.layoutManager = LinearLayoutManager(this)
        mealAdapter = MealAdapter(emptyList()) { meal ->
            val intent = Intent(this, MealDetailsActivity::class.java)
            intent.putExtra("MEAL_ID", meal.id)
            startActivity(intent)
        }
        rvMeals.adapter = mealAdapter

        setupSearch()
        setupFilter()
        loadCategories()
        loadMeals()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                loadMeals()
            }
        })
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val dbCategories = database.mealDao().getAllCategories()
            
            val uniqueDbCategories = dbCategories.flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()

            val defaultCategories = listOf("Obiad", "Śniadanie", "Kolacja", "Deser", "Z przepisem")
            val allCategories = (defaultCategories + uniqueDbCategories).distinct()

            chipGroup.removeAllViews()
            allCategories.forEach { category ->
                val chip = layoutInflater.inflate(R.layout.view_filter_chip, chipGroup, false) as Chip
                chip.text = category
                chip.id = View.generateViewId()
                chipGroup.addView(chip)
            }
        }
    }

    private fun setupFilter() {
        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            loadMeals()
        }
    }

    private fun loadMeals() {
        if (!::chipGroup.isInitialized || !::etSearch.isInitialized) return

        val searchQuery = etSearch.text.toString().trim().lowercase()
        val selectedChipIds = chipGroup.checkedChipIds
        val selectedLabels = selectedChipIds.map { id ->
            findViewById<Chip>(id).text.toString()
        }

        lifecycleScope.launch {
            val allMeals = database.mealDao().getAllMeals()
            
            val filteredMeals = allMeals.filter { meal ->
                // Filtr wyszukiwania
                val matchesSearch = if (searchQuery.isEmpty()) true else {
                    meal.mealName.lowercase().contains(searchQuery) ||
                    meal.restaurantName.lowercase().contains(searchQuery) ||
                    meal.description.lowercase().contains(searchQuery)
                }

                // Filtr kategorii
                val mealCategories = meal.category.split(Regex(",\\s*")).map { it.trim() }
                val hasCategorySelection = selectedLabels.any { it != "Z przepisem" }
                val hasRecipeSelection = selectedLabels.contains("Z przepisem")
                
                val categoryMatch = if (!hasCategorySelection) true else {
                    selectedLabels.any { sel -> sel != "Z przepisem" && mealCategories.contains(sel) }
                }
                val recipeMatch = if (!hasRecipeSelection) true else {
                    meal.recipeId != null
                }
                
                matchesSearch && categoryMatch && recipeMatch
            }
            
            if (filteredMeals.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                rvMeals.visibility = View.GONE
                mealAdapter.updateMeals(emptyList())
            } else {
                tvEmptyState.visibility = View.GONE
                rvMeals.visibility = View.VISIBLE
                mealAdapter.updateMeals(filteredMeals)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMeals()
    }
}