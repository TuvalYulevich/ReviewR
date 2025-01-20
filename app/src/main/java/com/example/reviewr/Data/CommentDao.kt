package com.example.reviewr.Data

import androidx.room.*

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM comments WHERE postId = :postId")
    suspend fun getCommentsByPostId(postId: String): List<CommentEntity>

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteCommentById(id: String)
}
