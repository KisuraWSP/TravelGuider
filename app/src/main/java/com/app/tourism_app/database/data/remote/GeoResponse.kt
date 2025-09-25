package com.app.tourism_app.database.data.remote

data class GeoApiResponse(
    val features: List<Feature>
)

data class Feature(
    val properties: Properties,
    val geometry: Geometry
)

data class Properties(
    val name: String?,
    val address_line1: String?,
    val categories: String?
)

data class Geometry(
    val coordinates: List<Double>
)