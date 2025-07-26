package com.example.gpstest.gps

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class DirectionRepository(private val serviceDirectionApi: DirectionService) {

    suspend fun getDrivingDirections(
        profile: String,
        coordinates: String,
        accessToken: String
    ): Response<Example> {
        return withContext(Dispatchers.IO) {
            serviceDirectionApi.getDrivingDirections(
                profile,
                coordinates,
                true,  // Default value for steps
                true,
                true,
                "imperial",
                "maxspeed",
                "full",
                "geojson",
                accessToken
            ).execute()
        }
    }
}