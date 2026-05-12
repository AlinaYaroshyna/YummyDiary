package com.example.yummydiary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeWithMeal>>(emptyList())
    val recipes: StateFlow<List<RecipeWithMeal>> = _recipes.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    fun loadRecipes() {
        viewModelScope.launch {
            val list = repository.getAllRecipesWithMeals()
            _recipes.value = list
            _categories.value = list.flatMap { it.meal?.category?.split(",") ?: emptyList() }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        }
    }

    fun loadRecipeById(id: Int) {
        viewModelScope.launch {
            _selectedRecipe.value = repository.getRecipeById(id)
        }
    }

    fun saveRecipe(recipe: Recipe, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = if (recipe.id == 0) {
                repository.insertRecipe(recipe)
            } else {
                repository.updateRecipe(recipe)
                recipe.id.toLong()
            }
            loadRecipes()
            onComplete(id)
        }
    }

    fun deleteRecipe(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecipeById(id)
            loadRecipes()
            onComplete()
        }
    }

    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
