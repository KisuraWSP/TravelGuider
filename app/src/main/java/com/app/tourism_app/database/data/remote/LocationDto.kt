package com.app.tourism_app.database.data.remote

import com.squareup.moshi.JsonClass

// Root response
@JsonClass(generateAdapter = true)
data class PlacesResponse(
    val type: String,
    val features: List<LocationDto>
)

// Each feature
@JsonClass(generateAdapter = true)
data class LocationDto(
    val type: String,
    val properties: LocationProperties
)

@JsonClass(generateAdapter = true)
data class LocationProperties(
    val name: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)
