package com.example.gpstest.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityMainBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.location

class MainActivity : BaseActivity() {
    private lateinit var locationViewModel: LocationViewModel
    private lateinit  var currentLocation: CurrentLocation
    private lateinit var mapboxMap: MapboxMap
    private var adView: AdView? = null
    private var review=false
    var navigatingFrommain=false
    lateinit var binding: ActivityMainBinding
    private lateinit var locationHelper: LocationPermissionHelper

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding=ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            if(intent!=null)
            {
                review=intent.getBooleanExtra("review",false)
            }
            if (review)
            {
//                showReviewDialog()
                FirebaseCustomEvents(this).createFirebaseEvents(customevents.reviewdialog_showed, "true")
                if (!Prefutils(this@MainActivity).getBool(
                        "is_premium",
                        false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.INVISIBLE
                }
                InfoUtil(this).showRatingDialog{
                    if (!Prefutils(this@MainActivity).getBool(
                            "is_premium",
                            false
                        ) && bannerEnabled && isEnabled
                    ) {
                        binding.adLayout.visibility = View.VISIBLE
                    }
                }
            }
            InfoUtil(this).setSystemBarsColor(R.attr.primarycolor)
            locationHelper = LocationPermissionHelper(this)
            locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
            currentLocation= CurrentLocation(this,this,locationViewModel,0)
            binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                mapboxMap = binding.mapView.getMapboxMap()
            }
            checkPermissions()
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.Mainactivity_open, "true")

            // Initialize MapView
           binding.more.setOnClickListener{
                showDialogWithItems()
            }
     binding.icBack.clickWithDebounce {
         finish()
       }
            binding.getlocation.setOnClickListener{
                val center = Point.fromLngLat(longitude, latitude)
                zoomMap(center)
            }
            binding.edField.clickWithDebounce{
                startActivity(Intent(this, Search::class.java))
            }
            binding.grantpermission.setOnClickListener {
                locationHelper.requestLocationPermission(this)
            }

            binding.enablegps.setOnClickListener {
                locationHelper.openLocationSettings(this)
            }
        }
//    private fun showReviewDialog() {
//        val manager = ReviewManagerFactory.create(this)
//        val request = manager.requestReviewFlow()
//
//        request.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//
//                // Here you can proceed with showing the review flow.
//            } else {
//                // Handle the error properly by checking the exception type
//                val exception = task.exception
//                if (exception is ReviewException) {
//                    // It's a ReviewException, we can access the error code
//                    // Handle the specific review error here
//                } else {
//                    // Other exceptions, such as RemoteException
//                    Log.e("ReviewError", "Error occurred: ${exception?.message}")
//                }
//            }
//        }
//    }
    private fun showAd(){
        if (Prefutils(this@MainActivity).getBool("is_premium", false)||!bannerEnabled || !isEnabled) {

            binding.adLayout.visibility=View.GONE

        } else {

            binding.adLayout.visibility=View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadBannerAd(binding.adLayout,
                    this@MainActivity,
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TEST TAG", "onAdFailed: Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "onAdLoaded: Banner")
                        }
                    })?.let { adView = it }
            }
        }
    }
    private fun checkPermissions() {
        when {
            !locationHelper.isLocationPermissionGranted() -> {
                binding.permissionLayout.visibility = View.VISIBLE
                binding.settinglayout.visibility = View.GONE
                binding.groupcontent.visibility = View.GONE
            }
            !locationHelper.isLocationEnabled() -> {
                binding.permissionLayout.visibility = View.GONE
                binding.settinglayout.visibility = View.VISIBLE
                binding.groupcontent.visibility = View.GONE
            }
            else -> {
                currentLocation.getCurrentLocation()
                binding.permissionLayout.visibility = View.GONE
                binding.settinglayout.visibility = View.GONE
                binding.groupcontent.visibility = View.VISIBLE
               // showAd()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions()
            }
            else {

                locationHelper.openAppSettings(this)
            }
        }
    }
    private fun zoomMap(center: Point) {
        Log.d("TAG", "zoomMap: $center")
        Log.d("TAG", "zoomMap lat long: $longitude : $latitude")
        binding.mapView.getMapboxMap().flyTo(cameraOptions{
            center(center)
            zoom(16.0)
            },
            MapAnimationOptions.mapAnimationOptions { duration(6_000) }
        )
        binding.mapView.getMapboxMap().apply {
            loadStyle(
                style(Style.MAPBOX_STREETS) {
                    binding.mapView.location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                    }

                }
            )
        }
    }
    private fun showDialogWithItems() {
        // List of items to display in the dialog
        val items = arrayOf(" Mapbox Standard", "Mapbox Streets", "Mapbox Satellite", " Mapbox Dark", " Mapbox Traffic")


        // Create an AlertDialog.Builder
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Select an Item")

        // Set the items to the dialog with click listeners
        builder.setItems(items) { dialog, which ->
            when (which) {
                0 -> {
                    binding.mapView.getMapboxMap().loadStyleUri(Style.OUTDOORS) {
                        mapboxMap = binding.mapView.getMapboxMap()
                        dialog.dismiss()

                    }
                }
                1 -> {
                    binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                        mapboxMap = binding.mapView.getMapboxMap()
                        dialog.dismiss()

                    }
                }
                2 -> {
                    binding.mapView.getMapboxMap().loadStyleUri(Style.SATELLITE) {
                        mapboxMap = binding.mapView.getMapboxMap()
                        dialog.dismiss()

                    }
                }
                3 -> {
                    binding.mapView.getMapboxMap().loadStyleUri(Style.DARK) {
                        mapboxMap = binding.mapView.getMapboxMap()
                        dialog.dismiss()

                    }
                }
                4 -> {
                    binding.mapView.getMapboxMap().loadStyleUri(Style.TRAFFIC_DAY) {
                        mapboxMap = binding.mapView.getMapboxMap()
                        dialog.dismiss()

                    }
                }
            }
        }

        // Create and show the dialog
        builder.create().show()
    }

    override fun onResume() {

        super.onResume()
        navigatingFrommain =false
        checkPermissions()
    }

    @SuppressLint("Lifecycle")
    override fun onStart() {
            super.onStart()
        binding.mapView.onStart()
        }


        @SuppressLint("Lifecycle")
        override fun onStop() {
            super.onStop()
            binding.mapView.onStop()
        }

        @SuppressLint("Lifecycle")
        override fun onDestroy() {
            super.onDestroy()
            binding.mapView.onDestroy()
        }
    override fun onPause() {
        super.onPause()
        if(!navigatingFrommain)
        {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.mainactivity_disappear, "true")
        }
    }
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFrommain = true
    }
    }