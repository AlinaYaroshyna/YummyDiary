package com.example.yummydiary

import android.os.Bundle
import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AllRecipesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_recipes)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
    }
}