package com.example.gpstest.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherViewModel(private val weatherService: WeatherService) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData>  = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = weatherService.getWeather(city, apiKey)
                withContext(Dispatchers.Main) {
                    _weatherData.value = response
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message
                }
            }
        }
    }
}