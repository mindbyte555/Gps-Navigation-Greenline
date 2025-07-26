package com.example.gpstest.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.check1
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.satelliteReview
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivitySateliteViewBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.satelite
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.atmosphere.generated.atmosphere
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.location

class SateliteView : BaseActivity(){
    private lateinit var locationViewModel: LocationViewModel
    private lateinit  var currentLocation: CurrentLocation
    lateinit var binding: ActivitySateliteViewBinding
    private var navigatingFromSatellite=false
    private var adView: AdView? = null
    private lateinit var locationHelper: LocationPermissionHelper
    private companion object {
        private const val ZOOM = 0.45
        private val CENTER = Point.fromLngLat(30.0, 50.0)
    }
    private var key: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySateliteViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseCustomEvents(this).createFirebaseEvents(satelite, "true")
        InfoUtil(this).setSystemBarsColor(R.attr.primarycolor)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!check1)
                {
                    satelliteReview=true
                }
                else{
                     satelliteReview=false
                }

                    finish()
            }
        })

        if (intent != null)
        {
            key =intent.getStringExtra("key")
        }
        if (key=="streets"){
            binding.title.text=resources.getString(R.string.street_view)
        }
        else{
            binding.title.text=resources.getString(R.string.satellite_view)
        }
        locationHelper = LocationPermissionHelper(this)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        currentLocation= CurrentLocation(this,this,locationViewModel,0)
        checkPermissions()
    binding.icBack.clickWithDebounce{
        onBackPressedDispatcher.onBackPressed()
    }
        binding.getlocation.setOnClickListener {
            binding.mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
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
        }
        binding.grantpermission.setOnClickListener {
            locationHelper.requestLocationPermission(this)
        }

        binding.enablegps.setOnClickListener {
            locationHelper.openLocationSettings(this)
        }

    }

    private fun updateui() {
        if (Prefutils(this@SateliteView).getBool("is_premium", false)||!bannerEnabled || !isEnabled) {

            binding.adLayout.visibility=View.GONE

        } else {
            binding.adLayout.visibility=View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadBannerAd(binding.adLayout,
                    this@SateliteView,
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TESTTAG", "onAdFailed: Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TESTTAG", "onAdLoaded: Banner")
                        }
                    })?.let { adView = it }
            }
        }
        if (key=="streets")
        {
            binding.title.text=resources.getString(R.string.street_view)
            binding.mapView.getMapboxMap().apply {
                loadStyleUri(Style.TRAFFIC_DAY) {
                    val cameraOptions = CameraOptions.Builder()
                        .center(Point.fromLngLat(0.0, 0.0))
                        .zoom(0.3)
                        .pitch(30.0)
                        .bearing(0.0)
                        .build()
                    setCamera(cameraOptions)
                }
                binding.mapView.location.updateSettings {
                    enabled = true
                    pulsingEnabled = true
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
            }
        }
        else
        {
            binding.title.text=resources.getString(R.string.satellite_view)
            binding.mapView.getMapboxMap().apply {
                setCamera(
                    cameraOptions {
                        center(CENTER)
                        zoom(ZOOM)
                    }
                )
                loadStyle(style(Style.SATELLITE_STREETS) {
                    +atmosphere { }
                    +projection(ProjectionName.GLOBE)
                }
                )
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
                updateui()

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
        navigatingFromSatellite=false
        checkPermissions()
    }
    override fun onPause() {
        super.onPause()
        if(!navigatingFromSatellite)
        {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.satelite_disappear, "true")
        }
    }
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromSatellite = true
    }
}