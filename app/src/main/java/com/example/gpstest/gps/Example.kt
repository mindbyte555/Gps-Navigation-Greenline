package com.example.gpstest.gps

import com.example.gpstest.Route
import com.example.gpstest.Waypoint

data class Example(
    val code: String,
    val routes: List<Route>,
    val uuid: String,
    val waypoints: List<Waypoint>
)