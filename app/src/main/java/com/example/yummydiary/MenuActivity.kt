package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yummydiary.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Twoje menuItems z tekstem i ikonami
        val menuItems = listOf(
            MenuItem("Dziennik dań", R.drawable.cutlery_bw, DiaryActivity::class),
            MenuItem("Dodaj nowe danie", R.drawable.cutlery_bw, AddMealActivity::class),
            MenuItem("Mapa dań", R.drawable.cutlery_bw, MealMapActivity::class),
            MenuItem("Wszystkie przepisy", R.drawable.cutlery_bw, AllRecipesActivity::class),
            MenuItem("O autorach", R.drawable.cutlery_bw, AboutAuthorsActivity::class)
        )

        val adapter = MenuAdapter(menuItems) { item ->
            // Obsługa kliknięcia według tytułu
            when(item.title) {
                "Dziennik dań" -> startActivity(Intent(this, DiaryActivity::class.java))
                "Dodaj nowe danie" -> startActivity(Intent(this, AddMealActivity::class.java))
                "Mapa dań" -> startActivity(Intent(this, MealMapActivity::class.java))
                "Wszystkie przepisy" -> startActivity(Intent(this, AllRecipesActivity::class.java))
                "O autorach" -> startActivity(Intent(this, AboutAuthorsActivity::class.java))
            }
        }

        // GridLayoutManager: 2 kolumny
        binding.recyclerMenu.layoutManager = GridLayoutManager(this, 1)
        binding.recyclerMenu.adapter = adapter
    }
}