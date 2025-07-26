package com.example.gpstest

import com.example.gpstest.apiresponse.Leg

data class Route(
    var weightName: String? = null,
    var weight: Double = 0.0,
    var duration: Double = 0.0,
    var distance: Double = 0.0,
    var legs: ArrayList<Leg>? = null,
    var geometry: com.example.gpstest.apiresponse.Geometry? = null
)

data class  BannerInstruction(
    val distanceAlongGeometry: Double,
    val primary: Primary,
    val sub: Sub
)
data class Primary(
    val components: List<Component>,
    val modifier: String,
    val text: String,
    val type: String
)
data class Component(
    val text: String,
    val type: String
)
data class Sub(
    val components: List<ComponentX>,
    val modifier: String,
    val text: String,
    val type: String
)
data class ComponentX(
    val active: Boolean,
    val activeDirection: String,
    val directions: List<String>,
    val text: String,
    val type: String
)

data class VoiceInstruction(
            val announcement: String,
            val distanceAlongGeometry: Double,
            val ssmlAnnouncement: String
        )

data class Waypoint(
    val distance: Double,
    val name: String,
    val location: List<Double>
)
