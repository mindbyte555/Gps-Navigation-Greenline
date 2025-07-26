package com.example.gpstest.gps

import android.location.Location

interface GPSCallback {
    fun onGPSUpdate(location: Location?)
}