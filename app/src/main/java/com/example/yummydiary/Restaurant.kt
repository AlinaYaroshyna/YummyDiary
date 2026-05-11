package com.example.yummydiary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey val id: Long, // Using OSM ID as primary key
    val name: String,
    val address: String,
    val category: String
)
