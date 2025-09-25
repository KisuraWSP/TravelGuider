package com.app.tourism_app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.tourism_app.database.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Insert
    suspend fun insert(review: Review): Long

    @Query("SELECT * FROM reviews WHERE locationId = :locationId ORDER BY createdAt DESC")
    fun reviewsForLocation(locationId: Long): Flow<List<Review>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun allReviews(): Flow<List<Review>>

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteById(id: Long)
}