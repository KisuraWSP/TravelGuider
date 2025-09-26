package com.app.tourism_app.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey
    val placeId: Long,          // use API-generated id or repo-generated id
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)