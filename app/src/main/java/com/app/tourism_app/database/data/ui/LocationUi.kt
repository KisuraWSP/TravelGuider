package com.app.tourism_app.database.data.ui

import com.app.tourism_app.database.model.Review

data class LocationUi(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val reviews: List<Review> = emptyList()
) {
    val averageRating: Float
        get() = if (reviews.isEmpty()) 0f else reviews.map { it.rating }.average().toFloat()
}