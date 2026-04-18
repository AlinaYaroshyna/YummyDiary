package com.example.yummydiary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY date DESC")
    suspend fun getAllMeals(): List<Meal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Int)

    @Query("SELECT * FROM meals WHERE category LIKE '%' || :category || '%' ORDER BY date DESC")
    suspend fun getMealsByCategory(category: String): List<Meal>

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: Int): Meal?

    @Query("SELECT DISTINCT category FROM meals")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM meals WHERE recipeId IS NOT NULL ORDER BY date DESC")
    suspend fun getMealsWithRecipes(): List<Meal>
}
