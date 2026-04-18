package com.example.yummydiary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(
    private var recipes: List<RecipeWithMeal>,
    private val onItemClick: (RecipeWithMeal) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMealName: TextView = view.findViewById(R.id.tvRecipeMealName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipes[position]
        holder.tvMealName.text = item.meal?.mealName ?: "Nieznane danie"
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = recipes.size

    fun updateData(newRecipes: List<RecipeWithMeal>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}