package com.example.gpstest.apiresponse

import com.example.gpstest.BannerInstruction
import com.example.gpstest.VoiceInstruction
import com.mapbox.search.result.ResultAccuracy

data class Leg(
    var via_waypoints: ArrayList<Any>? = null,
    var admins: ArrayList<Admin>? = null,
    var weight: Double = 0.0,
    var duration: Double = 0.0,
    var sirns: Sirns? = null,
    var steps:List<Step>? = null,
    var distance: Double = 0.0,
    var summary: String? = null
)
data class Step(
    val bannerInstructions: List<BannerInstruction>,
    val distance: Double,
    val driving_side: String,
    val duration: Double,
    val geometry: Geometry,
    val intersections: List<ResultAccuracy.Intersection>,
    val maneuver: Maneuver,
    val mode: String,
    val name: String,
    val speedLimitSign: String,
    val speedLimitUnit: String,
    val voiceInstructions: List<VoiceInstruction>? = null,
    val weight: Double
)
data class Maneuver(
    val bearing_after: Int,
    val bearing_before: Int,
    val instruction: String,
    val location: List<Double>,
    val modifier: String,
    val type: String
)