package com.example.reviewr.Data

import androidx.room.*

// Setting up the user Dao
@Dao
interface UserDao {

    // Query to fetch a user by ID
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUser(userId: String): UserEntity?

    // Insert or update a user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity): Unit

    // Delete current user from Room
    @Query("DELETE FROM users WHERE userId = :userId")
    fun deleteCurrentUser(userId: String): Int

    // Delete all users from Room
    @Query("DELETE FROM users")
    fun deleteAllUsers()
}




