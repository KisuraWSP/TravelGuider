package com.app.tourism_app.database.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object NetworkModule {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // IMPORTANT: build Moshi with KotlinJsonAdapterFactory
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.geoapify.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttp)
        .build()

    fun provideApiService(): ApiService = retrofit.create(ApiService::class.java)
}
