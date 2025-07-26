package com.example.gpstest.nearby_places

import com.example.gpstest.searchPlacesApiResponse.Places
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NearByPlacesService {
    @GET("geocoding/v5/mapbox.places/{place}.json")
    fun getPlaces(
        @Path("place") place: String,
        @Query("proximity") proximity: String,
        @Query("bbox") bbox: String,
        @Query("limit") limit: Int,
        @Query("autocomplete") autocomplete: Boolean,
        @Query("fuzzyMatch") fuzzyMatch: Boolean,
        @Query("routing") routing: Boolean,
        @Query("type") type: String,
        @Query("access_token") accessToken: String
    ): Call<Places>
}