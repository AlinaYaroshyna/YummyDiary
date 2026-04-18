package com.example.yummydiary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): Recipe?

    @androidx.room.Transaction
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesWithMeals(): List<RecipeWithMeal>
}