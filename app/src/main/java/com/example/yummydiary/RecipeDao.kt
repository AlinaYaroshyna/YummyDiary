package com.example.yummydiary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @androidx.room.Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Int)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): Recipe?

    @androidx.room.Transaction
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesWithMeals(): List<RecipeWithMeal>
}