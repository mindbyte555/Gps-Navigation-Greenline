package com.example.gpstest

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

 class CurrentLocation(var context: Context, var activity: Activity, val locationViewModel: LocationViewModel, var address:Int) {
    companion object {
        var latitude: Double = 0.0
        var longitude: Double = 0.0
        var fullAddress: String = ""
        var city: String = ""
        var country: String = ""
        var cityCountry: String = ""
        var cityCountry2: String = ""



    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

  fun checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                11
            )
        } else {
            getCurrentLocation()
        }
    }
    fun getCurrentLocation() {
        Log.d("testing", "checkLocationPermission: true ")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermission()
            return
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d("TAG", "getCurrentLocation: true ")
                    latitude = location.latitude
                    longitude = location.longitude

                    if (address == 1) {
                        cityCountry = getAddressFromLocation2(context, latitude, longitude)
                    }
                    else
                    {
                        fullAddress = getAddressFromLocation(latitude, longitude)
                    }

                    locationViewModel.updateLocation(longitude, latitude, fullAddress)
                    fusedLocationClient.removeLocationUpdates(this) // Stop updates after getting location
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

     fun getAddressFromLocation(latitude: Double, longitude: Double):String{
         Log.d("testing", "address full true ")
         var addressString=""
        try {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address: Address = addresses[0]
                    val cityy = address.locality ?: ""  // Get the city name
                    val countryy = address.countryName ?: ""  // Get the country name
                    city=cityy
                    country=countryy
                    cityCountry="$cityy,\n$countryy"
                    cityCountry2="$cityy,$countryy"
                    addressString = address.getAddressLine(0) // Full address
                  //  Toast.makeText(Context, "Current Address: $fullAddress", Toast.LENGTH_LONG).show()
                } else {
                    //Toast.makeText(Context, "Address not found", Toast.LENGTH_SHORT).show()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
          //  Toast.makeText(Context, "Error fetching address: ${e.message}", Toast.LENGTH_SHORT).show()
        }
         return addressString
    }
     fun getAddressFromLocation2(context: Context, latitude: Double, longitude: Double): String {
         Log.d("testing", "address city true ")
         var addressString = ""
         try {
             val geocoder = Geocoder(context, Locale.ENGLISH)
             val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

             if (!addresses.isNullOrEmpty()) {
                 val address: Address = addresses[0]
                 val cityy = address.locality ?: ""  // Get the city name
                 val countryy = address.countryName ?: ""  // Get the country name
                 city=cityy
                 country=countryy
                 cityCountry2="$cityy,$countryy"
                 addressString = "$cityy,\n$countryy"

             } else {
                // Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show()
             }
         } catch (e: Exception) {
             e.printStackTrace()
//
         }
         return addressString
     }

}