package com.udacity.asteroidradar.api

import com.udacity.asteroidradar.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * A retrofit service to fetch from the Asteroid NeoWs API
 * */
interface AsteroidApiService {
    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String
    ): String
}

object AsteroidApi {
    // configure the http client for a longer timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(100, TimeUnit.SECONDS)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val asteroids: AsteroidApiService by lazy {
        retrofit.create(AsteroidApiService::class.java)
    }
}