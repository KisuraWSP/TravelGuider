package com.app.tourism_app.database.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Properties(
    val name: String? = null,
    @Json(name = "formatted") val formatted: String? = null,
    @Json(name = "place_id") val placeId: String? = null,
    val city: String? = null,
    val country: String? = null,
    val state: String? = null,
    val postcode: String? = null
)
