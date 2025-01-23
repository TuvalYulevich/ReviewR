package com.example.reviewr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.reviewr.Data.AppDatabase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // MainActivity initiation

        try {
            val database = AppDatabase.getInstance(applicationContext)
            println("Database initialized successfully. Is Open: ${database.openHelper.writableDatabase.isOpen}")
        } catch (e: Exception) {
            println("Error initializing database: ${e.message}")
        }
    }
}