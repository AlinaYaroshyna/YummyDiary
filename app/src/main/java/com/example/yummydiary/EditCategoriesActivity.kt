package com.example.yummydiary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class EditCategoriesActivity : BaseActivity() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_categories)
        setToolbarTitle("Edytuj kategorie")

        database = AppDatabase.getDatabase(this)
        rvCategories = findViewById(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(this)
        
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val dbCategories = database.mealDao().getAllCategories()
            val uniqueCategories = dbCategories.flatMap { it.split(", ") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .toMutableList()

            adapter = CategoryAdapter(uniqueCategories) { category, action ->
                when (action) {
                    "EDIT" -> showEditDialog(category)
                    "DELETE" -> showDeleteConfirmDialog(category)
                }
            }
            rvCategories.adapter = adapter
        }
    }

    private fun showEditDialog(oldName: String) {
        val input = EditText(this)
        input.setText(oldName)
        AlertDialog.Builder(this)
            .setTitle("Zmień nazwę kategorii")
            .setView(input)
            .setPositiveButton("Zapisz") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != oldName) {
                    updateCategoryName(oldName, newName)
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showDeleteConfirmDialog(category: String) {
        AlertDialog.Builder(this)
            .setTitle("Usuń kategorię")
            .setMessage("Czy na pewno chcesz usunąć kategorię '$category' ze wszystkich dań?")
            .setPositiveButton("Usuń") { _, _ ->
                deleteCategory(category)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun updateCategoryName(oldName: String, newName: String) {
        lifecycleScope.launch {
            val meals = database.mealDao().getMealsWithCategory(oldName)
            meals.forEach { meal ->
                val updatedCategories = meal.category.split(", ")
                    .map { if (it.trim() == oldName) newName else it.trim() }
                    .distinct()
                    .joinToString(", ")
                database.mealDao().updateMeal(meal.copy(category = updatedCategories))
            }
            Toast.makeText(this@EditCategoriesActivity, "Zaktualizowano!", Toast.LENGTH_SHORT).show()
            loadCategories()
        }
    }

    private fun deleteCategory(category: String) {
        lifecycleScope.launch {
            val meals = database.mealDao().getMealsWithCategory(category)
            meals.forEach { meal ->
                val updatedCategories = meal.category.split(", ")
                    .filter { it.trim() != category }
                    .joinToString(", ")
                database.mealDao().updateMeal(meal.copy(category = updatedCategories))
            }
            Toast.makeText(this@EditCategoriesActivity, "Usunięto!", Toast.LENGTH_SHORT).show()
            loadCategories()
        }
    }

    inner class CategoryAdapter(
        private val categories: List<String>,
        private val onClick: (String, String) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvCategoryName)
            val btnEdit: ImageButton = view.findViewById(R.id.btnEditCategory)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteCategory)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_edit, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.tvName.text = category
            holder.btnEdit.setOnClickListener { onClick(category, "EDIT") }
            holder.btnDelete.setOnClickListener { onClick(category, "DELETE") }
        }

        override fun getItemCount() = categories.size
    }
}