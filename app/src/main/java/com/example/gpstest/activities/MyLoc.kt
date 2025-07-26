package com.example.gpstest.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.fullAddress
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityMyLocBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.myloc
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.showToast
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.location

class MyLoc : BaseActivity() {

    private lateinit var locationViewModel: LocationViewModel
    lateinit var binding: ActivityMyLocBinding
    private lateinit  var currentLocation: CurrentLocation
    private var adView: AdView? = null
    private lateinit var locationHelper: LocationPermissionHelper
    private var flag = false
    private var navigatingFromlocation=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMyLocBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseCustomEvents(this).createFirebaseEvents(myloc, "true")
        InfoUtil(this).setSystemBarsColor(R.attr.backgroundColor)
        locationHelper = LocationPermissionHelper(this)
       locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
         currentLocation= CurrentLocation(this,this,locationViewModel,2)
            checkPermissions()
        locationViewModel.latitude.observe(this) {
            val center = Point.fromLngLat(longitude, latitude)
                zoomMap(center)
                binding.currentAddress.text = fullAddress
            }


        locationViewModel.longitude.observe(this) {
            val center = Point.fromLngLat(longitude, latitude)
            zoomMap(center)
        }

        locationViewModel.fullAddress.observe(this) { address ->
            binding.currentAddress.text = address
        }

//        binding.mapView.getMapboxMap().addOnMapClickListener { point ->
//            performReverseGeocoding(point)
//            true
//        }
        binding.icBack.setOnClickListener {
          finish()
        }
        binding.currentLocation.setOnClickListener {

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
            binding.mapView.camera.apply {
                val bearing = createBearingAnimator(
                    CameraAnimatorOptions.cameraAnimatorOptions(
                    latitude,
                    longitude
                ) { startValue(15.0) }) {
                    duration = 6000
                    interpolator = AnticipateOvershootInterpolator()
                }
                val pitch = createPitchAnimator(CameraAnimatorOptions.cameraAnimatorOptions(30.0) { startValue(15.0) })
                {
                    duration = 2000
                }
                playAnimatorsTogether(bearing, pitch)
            }
            val center = Point.fromLngLat(longitude, latitude)
            binding.mapView.getMapboxMap().flyTo(
                cameraOptions {
                    center(center)
                    zoom(16.0)
                },
                MapAnimationOptions.mapAnimationOptions {
                    duration(6_000)
                }
            )
            binding.currentAddress.text=fullAddress
        }
        binding.copyaddress.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Address", fullAddress)
            clipboard.setPrimaryClip(clip)
            showToast("Address copied to clipboard")
        }
        binding.shareAdress.setOnClickListener {
            val locationUrl = "https://www.google.com/maps?q=$latitude,$longitude"
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "My Location")
            sendIntent.putExtra(Intent.EXTRA_TEXT, locationUrl)
            sendIntent.type = "text/plain"
            startActivity(
                Intent.createChooser(
                    sendIntent,
                    "share Location via"
                )
            )

        }
        binding.grantpermission.setOnClickListener {
            locationHelper.requestLocationPermission(this)
        }

        binding.enablegps.setOnClickListener {
            locationHelper.openLocationSettings(this)
        }
    }
    private fun load()
    {
        if (Prefutils(this@MyLoc).getBool("is_premium", false)||!bannerEnabled || !isEnabled) {

            binding.adLayout.visibility=View.GONE

        } else {
            binding.adLayout.visibility=View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadBannerAd(binding.adLayout,
                    this@MyLoc,
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

    override fun onPause() {
        super.onPause()
        if(!navigatingFromlocation)
        {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.myloc_disappear, "true")
        }
    }
private fun checkPermissions() {
    Handler(Looper.getMainLooper()).postDelayed({
        flag=true
    },3000)

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
            load()

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
    override fun onResume() {
        super.onResume()
        navigatingFromlocation=false
        if (flag) {
            checkPermissions()
        }


    }
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromlocation = true
    }

//    private fun performReverseGeocoding(point: Point) {
//        val geocoding = MapboxGeocoding.builder()
//            .accessToken(getString(R.string.mapbox_access_token))
//            .query(Point.fromLngLat(point.longitude(), point.latitude()))
//            .geocodingTypes("place") // Request detailed address information
//            .build()
//
//        geocoding.enqueueCall(object : retrofit2.Callback<com.mapbox.api.geocoding.v5.models.GeocodingResponse> {
//            override fun onResponse(
//                call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
//                response: retrofit2.Response<com.mapbox.api.geocoding.v5.models.GeocodingResponse>
//            ) {
//                if (response.isSuccessful && response.body() != null) {
//                    val results = response.body()!!.features()
//                    if (results.isNotEmpty()) {
//                        // Get the most accurate feature (first result is usually the best match)
//                        val feature = results[0]
//                        val address = feature.placeName() // Human-readable address
//                        val latitude = feature.center()?.latitude()
//                        val longitude = feature.center()?.longitude()
//
//                        Toast.makeText(
//                            this@MyLoc,
//                            "Address: $address\nLat: $latitude, Lng: $longitude",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    } else {
//                        Toast.makeText(this@MyLoc, "No address found.", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this@MyLoc, "Geocoding failed.", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(
//                call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
//                t: Throwable
//            ) {
//                Toast.makeText(this@MyLoc, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    private fun zoomMap(center: Point) {
        binding.mapView.getMapboxMap().flyTo(
            cameraOptions {
                center(center)
                zoom(16.0)
            },
            MapAnimationOptions.mapAnimationOptions {
                duration(6_000)
            }
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

}