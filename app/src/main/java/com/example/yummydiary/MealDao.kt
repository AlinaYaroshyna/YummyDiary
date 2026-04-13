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
}
