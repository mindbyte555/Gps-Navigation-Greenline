package com.example.gpstest.apiresponse

data class Waypoint(
    var distance: Double = 0.0,
    var name: String? = null,
    var location: ArrayList<Double>? = null
)