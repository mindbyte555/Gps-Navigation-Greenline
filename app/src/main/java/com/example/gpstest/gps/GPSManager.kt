package com.example.gpstest.gps

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener


class GPSManager(var mcontext: Context) : GnssStatus.Callback() {
    var TAG: String = "GPSMAnager"

    init {
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e(TAG, "onLocationChanged Test::$location")
                if (gpsCallback != null) {
                    gpsCallback!!.onGPSUpdate(location)
                }
            }

            override fun onProviderDisabled(provider: String) {
                Log.e(TAG, "onProviderDisabled Test::$provider")
            }

            override fun onProviderEnabled(provider: String) {
                Log.e(TAG, "onProviderEnabled Test::$provider")
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                Log.e(TAG, "onStatusChanged Test::$provider &&: $status")
            }
        }
    }

    var gPSCallback: GPSCallback?
        get() = gpsCallback
        set(gpsCallback) {
            Log.e(TAG, "setGPSCallback")
            Companion.gpsCallback = gpsCallback
        }

    companion object {
        private const val gpsMinTime = 5000
        private const val gpsMinDistance = 10
        private const val REQUEST_CHECK_SETTINGS = 400
        private var locationManager: LocationManager? = null
        private var locationListener: LocationListener? = null

        private var gpsCallback: GPSCallback? = null
    }
}