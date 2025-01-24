package com.example.reviewr.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AppImageDao {
    @Insert
    fun insertAll(appImage_urls: List<AppImageEntity>)

    @Query("SELECT * FROM appImage_urls")
    fun getAll(): List<AppImageEntity>

    @Query("DELETE FROM appImage_urls")
    fun clearTable()

}

