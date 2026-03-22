package com.example.yummydiary
import android.app.Activity
import kotlin.reflect.KClass

data class MenuItem(
    val title: String,
    val iconRes: Int,
    val activityClass: KClass<out Activity>
)