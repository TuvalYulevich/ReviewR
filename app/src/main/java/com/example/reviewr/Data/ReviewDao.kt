package com.example.reviewr.Data

import androidx.room.*

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun getReviewById(id: String): ReviewEntity?

    @Query("SELECT * FROM reviews")
    suspend fun getAllReviews(): List<ReviewEntity>

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()
}