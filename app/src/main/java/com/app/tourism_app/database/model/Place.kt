package com.app.tourism_app.database.model

data class Place(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val categories: List<String>
)
