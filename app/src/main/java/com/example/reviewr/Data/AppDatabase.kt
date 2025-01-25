package com.example.reviewr.Data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Setting up the app database
@Database(entities = [UserEntity::class, AppImageEntity:: class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appImageDao(): AppImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to version 2: Add profileImageUrl column
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN profileImageUrl TEXT")
            }
        }

        // Migration from version 2 to version 3: Add password column
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN password TEXT NOT NULL DEFAULT ''")
            }
        }

        // Migration from version 3 to version 4: Add App image column
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS appImage_urls (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url TEXT NOT NULL)")
            }
        }


        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    Log.d("AppDatabase", "Initializing Room database")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Add all migrations
                        .build()
                    INSTANCE = instance
                    Log.d("AppDatabase", "Database initialized successfully")
                    instance
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error initializing Room: ${e.message}")
                    throw e
                }
            }
        }
    }
}

