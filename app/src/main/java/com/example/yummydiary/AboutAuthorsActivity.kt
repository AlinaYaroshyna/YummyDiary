package com.example.yummydiary

import android.os.Bundle
import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AboutAuthorsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_authors)
        setToolbarTitle("O autorkach")
    }
}