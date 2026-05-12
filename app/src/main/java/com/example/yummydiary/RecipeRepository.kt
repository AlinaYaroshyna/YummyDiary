package com.example.yummydiary

class RecipeRepository(private val recipeDao: RecipeDao) {
    suspend fun getAllRecipesWithMeals() = recipeDao.getAllRecipesWithMeals()
    suspend fun getRecipeById(id: Int) = recipeDao.getRecipeById(id)
    suspend fun insertRecipe(recipe: Recipe) = recipeDao.insertRecipe(recipe)
    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)
    suspend fun deleteRecipeById(id: Int) = recipeDao.deleteRecipeById(id)
}
