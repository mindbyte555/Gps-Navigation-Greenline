package com.example.gpstest.gps

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DirectionService {
    @GET("directions/v5/mapbox/{profile}/{coordinates}")
    fun getDrivingDirections(
        @Path("profile") profile: String,
        @Path("coordinates") coordinates: String,
        @Query("steps") steps: Boolean = true,
        @Query("voice_instructions") voiceInstructions: Boolean = true,
        @Query("banner_instructions") bannerInstructions: Boolean = true,
        @Query("voice_units") voiceUnits: String = "imperial",
        @Query("annotations") annotations: String = "maxspeed",
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("access_token") accessToken: String
    ): Call<Example>
}