package com.example.gpstest.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService{
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherData
}
//data class WeatherData(
//    val name: String,
//    val main: Main,
//    val weather: List<Weather>
//)
data class WeatherData(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val sys: Sys,
    val visibility: Int,
)

data class Main(
    val temp: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)



//
//data class Main(
//    val temp: Double
//)
//
//data class Weather(
//    val icon: String
//)