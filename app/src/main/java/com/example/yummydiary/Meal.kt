package com.example.yummydiary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantName: String,
    val restaurantAddress: String,
    val mealName: String,
    val category: String,
    val description: String,
    val rating: Float,
    val date: Long,
    val imagePath: String? = null,
    val recipeId: Int? = null
)
