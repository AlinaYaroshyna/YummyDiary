package com.example.yummydiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yummydiary.databinding.ActivityMenuBinding

class MenuActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<android.view.View>(R.id.btnNavAddMeal).setOnClickListener {
            startActivity(Intent(this, AddMealActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnNavDiary).setOnClickListener {
            startActivity(Intent(this, DiaryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnNavMap).setOnClickListener {
            startActivity(Intent(this, MealMapActivity::class.java))
        }
    }
}