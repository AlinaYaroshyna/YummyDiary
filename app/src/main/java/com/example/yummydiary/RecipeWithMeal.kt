package com.example.yummydiary

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithMeal(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val meal: Meal?
)