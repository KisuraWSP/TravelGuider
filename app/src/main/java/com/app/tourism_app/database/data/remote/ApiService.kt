package com.app.tourism_app.database.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // Geoapify Places (supports both categories+filter and text search)
    // Docs style: https://api.geoapify.com/v2/places
    @GET("v2/places")
    suspend fun getLocations(
        @Query("categories") categories: String? = null,
        @Query("filter") filter: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("text") text: String? = null,
        @Query("apiKey") apiKey: String
    ): GeoapifyPlacesResponse
}