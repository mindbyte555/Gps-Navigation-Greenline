package com.example.gpstest.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.CurrentLocation
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityNavigateBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class Navigate : BaseActivity() {
    lateinit var binding: ActivityNavigateBinding
    private var pointedLatitude: Double = 0.0
    private var pointedLongitude: Double = 0.0
    private var pointedAddress: String = ""
    private var activity: String = "search"
    private var place = "Route Finder"
    var navigatingFromnavigate = false
    private lateinit var cenTer: Point
    private lateinit var currentLocation: CurrentLocation
    private lateinit var locationViewModel: LocationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseCustomEvents(this).createFirebaseEvents(customevents.navigate, "true")
        binding.currentAddress.isSelected = true
        InfoUtil(this).setSystemBarsColor(R.attr.primarycolor)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        currentLocation = CurrentLocation(this, this, locationViewModel, 0)
        if (intent != null) {
            place = intent.getStringExtra("place").toString()
            activity = intent.getStringExtra("activity").toString()
            pointedLatitude = intent.getDoubleExtra("destinationLatitude", 0.0)
            pointedLongitude = intent.getDoubleExtra("destinationLongitude", 0.0)
            pointedAddress = intent.getStringExtra("address").toString()
        }
        if (pointedAddress == "") {
            pointedAddress =
                currentLocation.getAddressFromLocation(pointedLatitude, pointedLongitude)
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("TAG", "onCreate:$pointedAddress ")
                binding.currentAddress.text = pointedAddress
            }, 3000)

        } else {
            binding.currentAddress.text = pointedAddress
        }
        if (activity == "seven") {
            binding.title.text = place
            binding.navigate.visibility = View.GONE
            binding.mapView.getMapboxMap().loadStyleUri(Style.SATELLITE) {
                val originalBitmap = BitmapFactory.decodeResource(
                    resources,
                    R.drawable.destination_point
                )
                val resizedBitmap = resizeBitmap(originalBitmap) // Resize to 100x100 pixels
                it.addImage("destination-icon", resizedBitmap)
                Log.d("mapbox", "Style loaded: ")
            }
        } else {
            binding.mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                val originalBitmap = BitmapFactory.decodeResource(
                    resources,
                    R.drawable.destination_point
                )
                val resizedBitmap = resizeBitmap(originalBitmap) // Resize to 100x100 pixels
                it.addImage("destination-icon", resizedBitmap)
                Log.d("mapbox", "Style loaded: ")
            }
            binding.navigate.visibility = View.VISIBLE
        }
        addDestinationMarker(Point.fromLngLat(pointedLongitude, pointedLatitude))
        cenTer = Point.fromLngLat(pointedLongitude, pointedLatitude)
        zoomMap(cenTer)
        binding.navigate.clickWithDebounce {
            if (isLocationEnabled()) {
                if (mInterstitialAd != null) {
                    AdsManager.showInterstitial(
                        true,
                        this@Navigate,
                        object : AdsManager.InterstitialAdListener {
                            override fun onAdClosed() {
                                val intent = Intent(this@Navigate, NavigationView::class.java)
                                intent.putExtra("destinationLatitude", pointedLatitude)
                                intent.putExtra("destinationLongitude", pointedLongitude)
                                startActivity(intent)
                                finish()
                            }
                        }, ""
                    )
                } else {
                    if (interstitialAd != null) {
                        showAvailableInterstitial(this) {
                            val intent = Intent(this, NavigationView::class.java)
                            intent.putExtra("destinationLatitude", pointedLatitude)
                            intent.putExtra("destinationLongitude", pointedLongitude)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        InterstitialClass.requestInterstitial(
                            this@Navigate,
                            this@Navigate,
                            "",
                            object : ActionOnAdClosedListener {
                                override fun ActionAfterAd() {
                                    val intent = Intent(this@Navigate, NavigationView::class.java)
                                    intent.putExtra("destinationLatitude", pointedLatitude)
                                    intent.putExtra("destinationLongitude", pointedLongitude)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        )
                    }
                }
            } else {
                showLocationAlert()
            }

        }
        binding.icBack.clickWithDebounce {
            finish()
        }
        loadads()
    }

    private fun loadads() {
        if (Prefutils(this@Navigate).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        } else {
            binding.adLayout.visibility = View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadHomeBannerAd(binding.adLayout,
                    this@Navigate, "search",
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TEST TAG", "onAdFailed: Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "onAdLoaded: Banner")
                        }
                    })
            }
        }
    }

    private fun showLocationAlert() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.enable_gps))
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to use this app.")
            .setPositiveButton("Location Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 100, 100, false)
    }

    private fun addDestinationMarker(point: Point) {
        binding.mapView.annotations.createPointAnnotationManager().apply {
            val annotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage("destination-icon")
                .withIconSize(0.5) // Ensure this image is added to the style
            create(annotationOptions)
        }
    }

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
    }

    override fun onResume() {
        navigatingFromnavigate = false
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (!navigatingFromnavigate) {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.navigate_disappear, "true")
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromnavigate = true
    }
}