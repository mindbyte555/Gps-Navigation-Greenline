package com.example.gpstest.apiresponse
class Admin {
    var iso_3166_1_alpha3: String? = null
    var iso_3166_1: String? = null
}

data class Geometry(
    val coordinates: List<List<Double>>,
    val type: String
)


class Root {
    var routes: ArrayList<Route>? = null
    var waypoints: ArrayList<Waypoint>? = null
    var code: String? = null
    var uuid: String? = null
}

public class Sirns{
}