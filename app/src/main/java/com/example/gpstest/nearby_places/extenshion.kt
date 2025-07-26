package com.example.gpstest.nearby_places

import android.content.Context
import com.example.gpstest.R

fun Context.getNearByPlaces(): ArrayList<NearbyPlacesModel>{
    val placesList = ArrayList<NearbyPlacesModel>()
    placesList.add(NearbyPlacesModel(R.drawable.atms,        getString(R.string.bank),            "atm"))
    placesList.add(NearbyPlacesModel(R.drawable.petrolpump,  getString(R.string.petrol_pump), "petrol_pump"))
    placesList.add(NearbyPlacesModel(R.drawable.gym,         getString(R.string.gym),             "gymnastics"))
    placesList.add(NearbyPlacesModel(R.drawable.airport,      getString(R.string.airport),       "airport"))
    placesList.add(NearbyPlacesModel(R.drawable.hospitals,    getString(R.string.hospital),    "hospital"))
    placesList.add(NearbyPlacesModel(R.drawable.cofee,          getString(R.string.coffee),          "Coffee"))
    placesList.add(NearbyPlacesModel(R.drawable.pharmacy,       getString(R.string.pharmacy),      "pharmacy"))
    placesList.add(NearbyPlacesModel(R.drawable.hotels,          getString(R.string.hotels),         "hotel"))
    placesList.add(NearbyPlacesModel(R.drawable.mosque,         getString(R.string.mosque),        "mosque"))
    placesList.add(NearbyPlacesModel(R.drawable.parks,           getString(R.string.park),           "park"))
    placesList.add(NearbyPlacesModel(R.drawable.artgallery,        getString(R.string.gallery),      "gallery"))
    placesList.add(NearbyPlacesModel(R.drawable.resturants,     getString(R.string.food),      "restaurant"))
    placesList.add(NearbyPlacesModel(R.drawable.busstop,       getString(R.string.bus_stop),     "bus_stop"))
    placesList.add(NearbyPlacesModel(R.drawable.policestation,       getString(R.string.police),     "police_station"))
    return placesList
}
fun getTurnDrawable(turn: String): Int {
    return when (turn) {
        "left" ->      R.drawable.left_black
        "right" ->      R.drawable.right_black
        "slight right" -> R.drawable.slight_right
        "straight" ->  R.drawable.straight
        "slight left" -> R.drawable.slight_left
        "sharp right" -> R.drawable.sharp_right
        "sharp left" -> R.drawable.sharp_left
        "uturn" ->        R.drawable.uturn
        else -> R.drawable.des
    }
}