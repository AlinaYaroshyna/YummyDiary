package com.example.yummydiary

import android.content.Intent
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout

    override fun setContentView(layoutResID: Int) {
        drawerLayout = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        val activityContainer: FrameLayout = drawerLayout.findViewById(R.id.activity_content)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(drawerLayout)

        val navView: NavigationView = drawerLayout.findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toolbar: androidx.appcompat.widget.Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false) // Disable default title
            val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
        }

        findViewById<android.view.View>(R.id.btnHome)?.setOnClickListener {
            if (this !is MenuActivity) {
                val intent = Intent(this, MenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        }
    }

    fun setToolbarTitle(title: String) {
        findViewById<android.widget.TextView>(R.id.toolbar_title)?.text = title
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                if (this !is MenuActivity) {
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }
            R.id.nav_diary -> {
                if (this !is DiaryActivity) {
                    startActivity(Intent(this, DiaryActivity::class.java))
                }
            }
            R.id.nav_add_meal -> {
                if (this !is AddMealActivity) {
                    startActivity(Intent(this, AddMealActivity::class.java))
                }
            }
            R.id.nav_map -> {
                if (this !is MealMapActivity) {
                    startActivity(Intent(this, MealMapActivity::class.java))
                }
            }
            R.id.nav_recipes -> {
                if (this !is AllRecipesActivity) {
                    startActivity(Intent(this, AllRecipesActivity::class.java))
                }
            }
            R.id.nav_about -> {
                if (this !is AboutAuthorsActivity) {
                    startActivity(Intent(this, AboutAuthorsActivity::class.java))
                }
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}