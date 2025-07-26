package com.example.gpstest.apiresponse

data class response(
    val code: String,
    val routes: List<Route>,
    val waypoints: List<Waypoint>
)