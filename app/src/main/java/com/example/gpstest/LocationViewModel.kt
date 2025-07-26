package com.example.gpstest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double> = _longitude

    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double> = _latitude
    private val _fullAddress = MutableLiveData<String>()
    val fullAddress: LiveData<String> = _fullAddress

    // Function to update location values
    fun updateLocation(newLongitude: Double, newLatitude: Double,newAddress:String) {
        _longitude.postValue(newLongitude)
        _latitude.postValue(newLatitude)
        _fullAddress.postValue(newAddress)
    }
}
