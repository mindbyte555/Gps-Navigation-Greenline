package com.example.gpstest.apiresponse

data class Route(
    var weight_name: String? = null,
    var weight: Double = 0.0,
    var duration: Double = 0.0,
    var distance: Double = 0.0,
    var legs: ArrayList<Leg>? = null,
    var geometry: Geometry? = null
)