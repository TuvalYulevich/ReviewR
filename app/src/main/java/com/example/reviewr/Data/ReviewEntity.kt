package com.example.reviewr.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val category: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val userId: String
)