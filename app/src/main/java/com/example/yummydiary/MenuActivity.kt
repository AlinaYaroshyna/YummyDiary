package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide

class MenuActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val logoImageView = findViewById<ImageView>(R.id.logo)
        Glide.with(this)
            .asGif()
            .load(R.drawable.logo_animated)
            .into(logoImageView)

        findViewById<android.view.View>(R.id.btnNavAddMeal).setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnNavDiary).setOnClickListener {
            startActivity(Intent(this, DiaryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnNavMap).setOnClickListener {
            startActivity(Intent(this, MealMapActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnNavAddRecipe).setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnNavAllRecipes).setOnClickListener {
            startActivity(Intent(this, AllRecipesActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnNavHowToUse)?.setOnClickListener {
            startActivity(Intent(this, HowToUseActivity::class.java))
        }
    }
}