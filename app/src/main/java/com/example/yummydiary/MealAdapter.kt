package com.example.yummydiary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class MealAdapter(
    private var meals: List<Meal>,
    private val onItemClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivMeal: ImageView = view.findViewById(R.id.ivMeal)
        val tvMealName: TextView = view.findViewById(R.id.tvMealName)
        val tvRestaurantName: TextView = view.findViewById(R.id.tvRestaurantName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBarSmall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.tvMealName.text = meal.mealName
        holder.tvRestaurantName.text = meal.restaurantName
        holder.ratingBar.rating = meal.rating

        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("pl"))
        holder.tvDate.text = sdf.format(Date(meal.date))

        if (meal.imagePath != null) {
            Glide.with(holder.ivMeal.context)
                .load(android.net.Uri.parse(meal.imagePath))
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .override(200, 200) // Small thumbnail for list
                .into(holder.ivMeal)
        } else {
            holder.ivMeal.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener { onItemClick(meal) }
    }

    override fun getItemCount() = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}