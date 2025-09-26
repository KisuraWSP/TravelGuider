package com.app.tourism_app.database.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoapifyPlacesResponse(
    val features: List<Feature> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Feature(
    val properties: Properties? = null,
    val geometry: Geometry? = null
)