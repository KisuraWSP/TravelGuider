package com.app.tourism_app.database.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApiService {
    @GET("v1/places")
    suspend fun getPlaces(
        @Query("categories") categories: String,
        @Query("filter") filter: String,
        @Query("apiKey") apiKey: String
    ): GeoApiResponse
}