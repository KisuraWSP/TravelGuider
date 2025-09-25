package com.app.tourism_app.database.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("places")
    suspend fun getLocations(
        @Query("categories") categories: String,
        @Query("filter") filter: String,
        @Query("limit") limit: Int,
        @Query("apiKey") apiKey: String
    ): PlacesResponse
}