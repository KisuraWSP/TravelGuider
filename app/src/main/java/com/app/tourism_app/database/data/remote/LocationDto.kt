package com.app.tourism_app.database.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Root response
@JsonClass(generateAdapter = true)
data class PlacesResponse(
    val type: String,
    val features: List<LocationDto>
)

// Each feature (a location)
@JsonClass(generateAdapter = true)
data class LocationDto(
    val type: String,
    val properties: LocationProperties,
    val geometry: Geometry
)

// Properties of the location
@JsonClass(generateAdapter = true)
data class LocationProperties(
    val name: String? = null,
    val description: String? = null,
    @Json(name = "formatted") val formattedAddress: String? = null,
    val address_line1: String? = null,
    val address_line2: String? = null,
    val categories: List<String>? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val imageUrl: String? = null
)

// Geometry field with coordinates
@JsonClass(generateAdapter = true)
data class Geometry(
    val type: String? = null,
    val coordinates: List<Double>?
)
