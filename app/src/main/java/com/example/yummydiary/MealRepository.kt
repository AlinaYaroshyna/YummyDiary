package com.example.yummydiary

import kotlinx.coroutines.flow.Flow

class MealRepository(private val mealDao: MealDao) {
    suspend fun getAllMeals(): List<Meal> = mealDao.getAllMeals()
    
    suspend fun insertMeal(meal: Meal) = mealDao.insertMeal(meal)
    
    suspend fun updateMeal(meal: Meal) = mealDao.updateMeal(meal)
    
    suspend fun deleteMealById(id: Int) = mealDao.deleteMealById(id)
    
    suspend fun getMealById(id: Int): Meal? = mealDao.getMealById(id)
    
    suspend fun getAllCategories(): List<String> = mealDao.getAllCategories()
    
    suspend fun getAllRestaurantNames(): List<String> = mealDao.getAllRestaurantNames()

    suspend fun getMealsWithCategory(category: String): List<Meal> = mealDao.getMealsWithCategory(category)
}
