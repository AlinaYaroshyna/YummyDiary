package com.example.yummydiary
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.yummydiary.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val recycler = findViewById<RecyclerView>(R.id.menuRecycler)

        val menuItems = listOf(
            MenuItem("Dziennik dań", R.drawable.ic_food),
            MenuItem("Dodaj nowe danie", R.drawable.ic_food),
            MenuItem("Mapa dań", R.drawable.ic_food),
            MenuItem("Wszystkie przepisy", R.drawable.ic_food),
            MenuItem("O autorach", R.drawable.ic_food)
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = MenuAdapter(menuItems) {
            // handle click
        }
    }
}