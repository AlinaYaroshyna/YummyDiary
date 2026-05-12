package com.example.yummydiary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealViewModel(private val repository: MealRepository) : ViewModel() {

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _restaurants = MutableStateFlow<List<String>>(emptyList())
    val restaurants: StateFlow<List<String>> = _restaurants.asStateFlow()

    private val _selectedMeal = MutableStateFlow<Meal?>(null)
    val selectedMeal: StateFlow<Meal?> = _selectedMeal.asStateFlow()

    fun loadAllData() {
        viewModelScope.launch {
            _meals.value = repository.getAllMeals()
            
            val dbCats = repository.getAllCategories()
            val uniqueCats = dbCats.flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
            val defaultCats = listOf("Obiad", "Śniadanie", "Kolacja", "Deser")
            _categories.value = (defaultCats + uniqueCats).distinct()
            
            _restaurants.value = repository.getAllRestaurantNames()
        }
    }

    fun loadMealById(id: Int) {
        viewModelScope.launch {
            _selectedMeal.value = repository.getMealById(id)
        }
    }

    fun deleteMeal(id: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMealById(id)
            loadAllData()
            onComplete()
        }
    }

    fun saveMeal(meal: Meal, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (meal.id == 0) {
                repository.insertMeal(meal)
            } else {
                repository.updateMeal(meal)
            }
            loadAllData()
            onComplete()
        }
    }

    fun updateCategoryName(oldName: String, newName: String) {
        viewModelScope.launch {
            val mealsWithCategory = repository.getMealsWithCategory(oldName)
            mealsWithCategory.forEach { meal ->
                val updatedCategories = meal.category.split(", ")
                    .map { if (it.trim() == oldName) newName else it.trim() }
                    .distinct()
                    .joinToString(", ")
                repository.updateMeal(meal.copy(category = updatedCategories))
            }
            loadAllData()
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            val mealsWithCategory = repository.getMealsWithCategory(category)
            mealsWithCategory.forEach { meal ->
                val updatedCategories = meal.category.split(", ")
                    .filter { it.trim() != category }
                    .joinToString(", ")
                repository.updateMeal(meal.copy(category = updatedCategories))
            }
            loadAllData()
        }
    }

    class Factory(private val repository: MealRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MealViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
