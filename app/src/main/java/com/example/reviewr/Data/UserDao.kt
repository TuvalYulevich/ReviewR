package com.example.reviewr.Data

import androidx.room.*

@Dao
interface UserDao {

    // Query to fetch a user by ID
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUser(userId: String): UserEntity?

    // Insert or update a user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity): Unit
}




