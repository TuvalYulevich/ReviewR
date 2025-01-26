package com.example.reviewr.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appImage_urls")
data class AppImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val url: String
)