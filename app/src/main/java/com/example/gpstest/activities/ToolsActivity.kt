package com.example.gpstest.activities

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdIds
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.InlineBannerN
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.botInter
import com.example.gpstest.MyApp.Companion.check1
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.inLineBannerEnabled
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.satelliteReview
import com.example.gpstest.MyApp.Companion.weatherInter
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityToolsBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.AI_Bot_clicked
import com.example.gpstest.firebase.customevents.Companion.compass_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.drawer_close
import com.example.gpstest.firebase.customevents.Companion.drawer_open
import com.example.gpstest.firebase.customevents.Companion.my_loc_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.near_by_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.premmium_clicked
import com.example.gpstest.firebase.customevents.Companion.route_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.searchVoice_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.setellite_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.sevenwonders_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.streets_btn_clicked
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.nearby_places.NearByplacesActivity
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.example.gpstest.weather.WeatherService
import com.example.gpstest.weather.WeatherViewModel
import com.example.gpstest.weather.WeatherViewModelFactory
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ToolsActivity : BaseActivity() {
    lateinit var binding: ActivityToolsBinding

    private var navigatingFromStart = false
    private var loactionflag = false
    private var bounceAnim: ObjectAnimator? = null
    private val apiKey = "a6450a304b61929119a1967808ae10a0"
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var currentLocation: CurrentLocation
    private lateinit var drawerLayout: DrawerLayout
    private var firsttime = true
    private var adView: AdView? = null
    private lateinit var locationHelper: LocationPermissionHelper
    private var review = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)
//******* bar colors tool bar and status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION") window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!Prefutils(this@ToolsActivity).getBool(
                "is_premium", false
            ) && inLineBannerEnabled && isEnabled
        ) {
            Log.e("TEST TAG", "onCreate inLineBanner: $InlineBannerN")
            if (InlineBannerN) {
                AdsManager.showInlineBannerAd(
                    binding.adContainerView,
                    this,
                    AdIds.AdmobInlineBannerId(), object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TEST TAG", "Banner inline Ad Failed")
                            binding.shimmer.hideShimmer()
                            binding.shimmer.stopShimmer()
                            binding.shimmer.setBackgroundResource(R.color.bg)
                            binding.shimmer.visibility = View.GONE
                        }

                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "Banner inline Ad Loaded")
                            binding.shimmer.hideShimmer()
                            binding.shimmer.stopShimmer()
                            binding.shimmer.setBackgroundResource(R.color.bg)

                        }
                    }
                )
            } else {
                Log.e("TEST TAG", "onCreate inLineBanner else: $InlineBannerN")
                setShimmerHorizontalMargin(16, 16)
                AdsManager.loadNative(
                    binding.adContainerView,
                    this,
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "loadNative Ad Failed")
                            binding.shimmer.hideShimmer()
                            binding.shimmer.stopShimmer()
                            binding.shimmer.setBackgroundResource(R.color.white)
                        }

                        override fun onAdFailed() {
                            Log.e("TEST TAG", "loadNativeAd Loaded")
                            binding.shimmer.hideShimmer()
                            binding.shimmer.stopShimmer()
                            binding.shimmer.setBackgroundResource(R.drawable.rounded_with_gray_light)
                            binding.shimmer.visibility = View.GONE
                        }
                    }, AdsManager.NativeAdType.SMALL
                )
            }
        } else {
            binding.shimmer.visibility = View.GONE
        }

        InfoUtil(this).setSystemBarsColor(R.attr.backgroundColor2)
        //   hideNavigationBarTemporarily()
//*******review points
        if (intent != null) {
            review = intent.getBooleanExtra("review", false)
        }
        if (review) {
//                showReviewDialog()
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.reviewdialog_showed, "true"
            )


            InfoUtil(this).showRatingDialog {
                if (!Prefutils(this@ToolsActivity).getBool(
                        "is_premium", false
                    ) && bannerEnabled && isEnabled
                ) {
                    //new
//                    binding.adLayout.visibility = View.VISIBLE
                }
            }
        }
//back press logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        setTabSelected(binding.navTools)

        binding.navHome.setOnClickListener {
//            setTabSelected(binding.navHome)
            finish()
        }

        binding.navTools.setOnClickListener {
            setTabSelected(binding.navTools)

        }
        FirebaseCustomEvents(this).createFirebaseEvents("home2 launch", "true")
        binding.textView5.isSelected = true
        binding.textView55.isSelected = true
        binding.textView15.isSelected = true
        binding.textView14.isSelected = true
        binding.textView16.isSelected = true
        binding.textView13.isSelected = true
        Log.d("refresh", "onCreate: called ")
        val prefUtil = Prefutils(this)
        prefUtil.setBool("isFirstTime", false)

// getting location
        locationHelper = LocationPermissionHelper(this)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        currentLocation = CurrentLocation(this, this, locationViewModel, 2)

        val weatherService = Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(WeatherService::class.java)
        val factory = WeatherViewModelFactory(weatherService)
        weatherViewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]

//++++++Drawer
//        Glide.with(this).load(R.drawable.premiumcrow).into(binding.premiumimg)
//        Glide.with(this).load(R.drawable.cloudy).into(binding.weathericon)

        val isDarkMode = Prefutils(this).getBool("isDarkMode")
        checkPermissions()
        bounceAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.cardView2,
            PropertyValuesHolder.ofFloat("scaleX", 0.97f, 1.05f),
            PropertyValuesHolder.ofFloat("scaleY", 0.97f, 1.05f),
            PropertyValuesHolder.ofFloat("rotation", -5f, 5f) // Slight rotation
        ).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = DecelerateInterpolator() // Smooth easing
            start()
        }
        bounceAnim!!.start()
        locationViewModel.latitude.observe(this) {
            val center = Point.fromLngLat(longitude, latitude)
            zoomMap(center)
        }


        locationViewModel.longitude.observe(this) {
            val center = Point.fromLngLat(longitude, latitude)
            zoomMap(center)
        }
//        bounceanimation = ObjectAnimator.ofPropertyValuesHolder(
//            binding.grantperm,
//            PropertyValuesHolder.ofFloat("scaleX", 0.97f, 1.02f),
//            PropertyValuesHolder.ofFloat("scaleY", 0.97f, 1.05f)
//        ).apply {
//            duration = 500
//            repeatCount = ObjectAnimator.INFINITE
//            repeatMode = ObjectAnimator.REVERSE
//            interpolator = DecelerateInterpolator() // Smooth transition
//        }

        binding.cardView.clickWithDebounce {

            binding.cardView.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.cardView.animate().scaleX(1f).scaleY(1f).duration = 100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    FirebaseCustomEvents(this).createFirebaseEvents(drawer_open, "true")
                    openDrawerWithAnimation(drawerLayout)

                } else {
                    FirebaseCustomEvents(this).createFirebaseEvents(drawer_close, "true")

                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }, 200)

        }
        binding.share.setOnClickListener {
            val locationUrl = "https://www.google.com/maps?q=$latitude,$longitude"
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, locationUrl)
            sendIntent.type = "text/plain"
            startActivity(
                Intent.createChooser(
                    sendIntent, "share Location via"
                )
            )
        }

        binding.weatherN.clickWithDebounce {
            if (weatherInter) {
                if (mInterstitialAd != null) {
                    AdsManager.showInterstitial(
                        true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                            override fun onAdClosed() {
                                startActivity(Intent(this@ToolsActivity, weather::class.java))
                            }


                        }, "weather_activity"
                    )
                } else {
                    if (interstitialAd != null) {
                        showAvailableInterstitial(this) {
                            startActivity(Intent(this@ToolsActivity, weather::class.java))
                        }
                    } else {
                        InterstitialClass.requestInterstitial(this@ToolsActivity,
                            this@ToolsActivity,
                            "weather_activity",
                            object : ActionOnAdClosedListener {
                                override fun ActionAfterAd() {
                                    startActivity(Intent(this@ToolsActivity, weather::class.java))
                                }
                            }

                        )
                    }
                }
            } else {
                startActivity(Intent(this@ToolsActivity, weather::class.java))
            }
            FirebaseCustomEvents(this).createFirebaseEvents("weather_clicked", "true")

        }


        binding.currentLocation.clickWithDebounce {
            binding.currentLocation.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction {
                    binding.currentLocation.animate().scaleX(1f).scaleY(1f).duration = 100
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!locationHelper.isLocationPermissionGranted()) {
                            locationHelper.requestLocationPermission(this)


                        } else if (!locationHelper.isLocationEnabled()) {
                            locationHelper.openLocationSettings(this)
                        } else {
                            binding.mapView.getMapboxMap().apply {
                                loadStyle(style(Style.MAPBOX_STREETS) {
                                    binding.mapView.location.updateSettings {
                                        enabled = true
                                        pulsingEnabled = true
                                    }

                                })
                            }
                            binding.mapView.camera.apply {
                                val bearing =
                                    createBearingAnimator(CameraAnimatorOptions.cameraAnimatorOptions(
                                        latitude, longitude
                                    ) { startValue(15.0) }) {
                                        duration = 6000
                                        interpolator = AnticipateOvershootInterpolator()
                                    }
                                val pitch =
                                    createPitchAnimator(CameraAnimatorOptions.cameraAnimatorOptions(
                                        30.0
                                    ) {
                                        startValue(
                                            15.0
                                        )
                                    }) {
                                        duration = 2000
                                    }
                                playAnimatorsTogether(bearing, pitch)
                            }
                            val center = Point.fromLngLat(longitude, latitude)
                            binding.mapView.getMapboxMap().flyTo(cameraOptions {
                                center(center)
                                zoom(16.0)
                            }, MapAnimationOptions.mapAnimationOptions {
                                duration(6_000)
                            })
                        }

                    }, 200)
                }
        }


        binding.botLayout.clickWithDebounce {
            if (!isInternetAvailable(this@ToolsActivity)) {
                Toast.makeText(
                    this, "internet not available", Toast.LENGTH_SHORT
                ).show()
            } else {
                if (botInter) {
                    if (mInterstitialAd != null) {
                        AdsManager.showInterstitial(
                            true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                                override fun onAdClosed() {
                                    startActivity(Intent(this@ToolsActivity, AIChat::class.java))
                                }
                            }, "weather_activity"
                        )
                    } else {
                        if (interstitialAd != null) {
                            showAvailableInterstitial(this) {
                                startActivity(Intent(this@ToolsActivity, AIChat::class.java))
                            }
                        } else {
                            InterstitialClass.requestInterstitial(this@ToolsActivity,
                                this@ToolsActivity,
                                "Ai_activity",
                                object : ActionOnAdClosedListener {
                                    override fun ActionAfterAd() {
                                        startActivity(
                                            Intent(
                                                this@ToolsActivity, AIChat::class.java
                                            )
                                        )
                                    }
                                })
                        }
                    }
                } else {
                    startActivity(Intent(this@ToolsActivity, AIChat::class.java))
                }
            }
            FirebaseCustomEvents(this).createFirebaseEvents(AI_Bot_clicked, "true")
        }
        binding.compass.clickWithDebounce {
//            binding.compass.animate()
//                .scaleX(0.9f)
//                .scaleY(0.9f)
//                .setDuration(100)
//                .withEndAction {
//                    binding.compass.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
            //   Handler(Looper.getMainLooper()).postDelayed({
            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@ToolsActivity, CompassActivity::class.java
                                )
                            )
                        }


                    }, "compass_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(
                            Intent(
                                this@ToolsActivity, CompassActivity::class.java
                            )
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@ToolsActivity,
                        this@ToolsActivity,
                        "compass_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@ToolsActivity, CompassActivity::class.java
                                    )
                                )
                            }
                        }

                    )
                }
            }
//
//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(compass_btn_clicked, "true")

        }
        binding.voiceMap.clickWithDebounce {


//            binding.voiceMap.animate()
//                .scaleX(0.9f)
//                .scaleY(0.9f)
//                .setDuration(100)
//                .withEndAction {
//                    binding.voiceMap.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@ToolsActivity, Search::class.java
                                ).putExtra("voice", true)
                            )
                        }
                    }, "search_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(
                            Intent(
                                this@ToolsActivity, Search::class.java
                            ).putExtra("voice", true)
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@ToolsActivity,
                        this@ToolsActivity,
                        "search_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@ToolsActivity, Search::class.java
                                    ).putExtra("voice", true)
                                )
                            }
                        })
                }
            }

            FirebaseCustomEvents(this).createFirebaseEvents(searchVoice_btn_clicked, "true")
            //    }, 200)

        }
        binding.edField.clickWithDebounce {
            startActivity(
                Intent(
                    this@ToolsActivity, Search::class.java
                ).putExtra("search", true)
            )
        }
        binding.satelliteLayout.clickWithDebounce {
//
//            binding.sateliteMap.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.sateliteMap.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({
//

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(this@ToolsActivity, SateliteView::class.java).putExtra(
                                    "key", "sattelite"
                                )
                            )
                        }
                    }, "sattelite_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(
                            Intent(this@ToolsActivity, SateliteView::class.java).putExtra(
                                "key", "sattelite"
                            )
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@ToolsActivity,
                        this@ToolsActivity,
                        "sattelite_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@ToolsActivity, SateliteView::class.java
                                    ).putExtra("key", "sattelite")
                                )
                            }
                        })
                }
            }

//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(setellite_btn_clicked, "true")
        }
        binding.streetView.clickWithDebounce {
//
//            binding.streetView.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.streetView.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
////            Handler(Looper.getMainLooper()).postDelayed({

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@ToolsActivity, SateliteView::class.java
                                ).putExtra("key", "streets")
                            )
                        }
                    }, "streets_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(
                            Intent(
                                this@ToolsActivity,

                                SateliteView::class.java
                            ).putExtra("key", "streets")
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@ToolsActivity,
                        this@ToolsActivity,
                        "streets_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@ToolsActivity, SateliteView::class.java
                                    ).putExtra("key", "streets")
                                )
                            }
                        })
                }
            }

//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(streets_btn_clicked, "true")
        }
        binding.placesLayout.clickWithDebounce {

//            binding.sevenwonders.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.sevenwonders.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({
//                    AdsManager.showInterstitial(
//                        true,
//                        this@StartActivity,
//                        object : AdsManager.InterstitialAdListener {
//                            override fun onAdClosed() {
//                                startActivity(Intent(this@StartActivity, Sevenwonders::class.java))
//                            }
//                        }, "sevenwonders_activity"
//                    )
            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@ToolsActivity, Sevenwonders::class.java))
                        }
                    }, "sevenwonders_activity"
                )

            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(Intent(this@ToolsActivity, Sevenwonders::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@ToolsActivity,
                        this@ToolsActivity,
                        "sevenwonders_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@ToolsActivity, Sevenwonders::class.java))
                            }
                        })
                }
            }


//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(sevenwonders_btn_clicked, "true")
        }
        binding.cardView2.clickWithDebounce {

            binding.cardView2.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.cardView2.animate().scaleX(1f).scaleY(1f).duration = 100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@ToolsActivity, InApp_Purchase_Screen::class.java)
                intent.putExtra("startActivity", true)
                startActivity(intent)
            }, 100)
            FirebaseCustomEvents(this).createFirebaseEvents(premmium_clicked, "true")
            //  startOpenVpn()
        }
        binding.routefinder.clickWithDebounce {

//            binding.routefinder.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.routefinder.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({
            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@ToolsActivity, MainActivity::class.java))
                        }
                    }, "routeFinder_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this, true) {
                        startActivity(Intent(this@ToolsActivity, MainActivity::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@ToolsActivity,
                        this@ToolsActivity,
                        "routeFinder_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@ToolsActivity, MainActivity::class.java))
                            }
                        })
                }
            }


//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(route_btn_clicked, "true")
        }
        binding.myloc.clickWithDebounce {
//
//
//            binding.myLoc.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.v.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@ToolsActivity, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@ToolsActivity, MyLoc::class.java))
                        }
                    }, "MyLocation_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(Intent(this@ToolsActivity, MyLoc::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@ToolsActivity,
                        this@ToolsActivity,
                        "MyLocation_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@ToolsActivity, MyLoc::class.java))
                            }
                        })
                }
            }

//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(my_loc_btn_clicked, "true")
        }
        binding.nearBy.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(near_by_btn_clicked, "true")
            binding.nearBy.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.nearBy.animate().scaleX(1f).scaleY(1f).duration = 100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(
                    Intent(
                        this@ToolsActivity, NearByplacesActivity::class.java
                    )
                )
            }, 200)
        }
    }

    private fun zoomMap(center: Point) {
        binding.mapView.getMapboxMap().flyTo(cameraOptions {
            center(center)
            zoom(16.0)
        }, MapAnimationOptions.mapAnimationOptions {
            duration(6_000)
        })

        binding.mapView.getMapboxMap().apply {
            loadStyle(style(Style.MAPBOX_STREETS) {
                binding.mapView.location.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                }

            })
        }
    }

    private fun openDrawerWithAnimation(drawerLayout: DrawerLayout) {
        val drawer = drawerLayout.findViewById<View>(R.id.navView)
        val animator = ObjectAnimator.ofFloat(drawer, "translationX", -drawer.width.toFloat(), 0f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 600 //milli seconds
        animator.start()
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions()
            } else {

                locationHelper.openAppSettings(this)
            }
        } else if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Prefutils(this).setBool("isShowing", false)
            } else {
                Prefutils(this).setBool("isShowing", false)

            }
        }
    }

    private fun checkPermissions() {

        when {
            !locationHelper.isLocationPermissionGranted() -> {
                binding.share.visibility = View.INVISIBLE
                loactionflag = false
//                bounceanimation?.start()
//                binding.grantperm.text = resources.getString(R.string.grant_permission)
//                binding.permissionLayout.visibility = View.VISIBLE
//                binding.weatherContent.visibility = View.GONE
            }

            !locationHelper.isLocationEnabled() -> {
                binding.share.visibility = View.INVISIBLE
                loactionflag = false
//                bounceanimation?.start()
//                binding.grantperm.text = resources.getString(R.string.enable_gps)
//                binding.permissionLayout.visibility = View.VISIBLE
//                binding.weatherContent.visibility = View.GONE
            }

            else -> {
//                bounceanimation?.cancel()
                handleLocationAccess()
                binding.share.visibility = View.VISIBLE
//                binding.permissionLayout.visibility = View.GONE
//                binding.weatherContent.visibility = View.VISIBLE
            }
        }
    }

    override fun onPause() {
        Log.e("TEST TAG", "onPause: of start called $navigatingFromStart")
        bounceAnim!!.pause()
        firsttime = false
        super.onPause()
        if (!navigatingFromStart) {
            Log.e("TEST TAG", "onPause: of start disapper false")
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.start_disappear, "true")
        }
    }

    override fun onResume() {
        navigatingFromStart = false
        bounceAnim!!.resume()
        Log.d("TAG", "onResume: resume called")
        if (Prefutils(this).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            //new
//            binding.adLayout.visibility = View.GONE
        }

        Handler(Looper.getMainLooper()).postDelayed({
            checkPermissions()
        }, 3000)

        adView?.resume()

        if (satelliteReview) {
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.reviewdialog_showed, "true"
            )
            if (!Prefutils(this@ToolsActivity).getBool(
                    "is_premium", false
                ) && bannerEnabled && isEnabled
            ) {
                //new
//                binding.adLayout.visibility = View.INVISIBLE
            }
            InfoUtil(this).showRatingDialog {
                if (!Prefutils(this@ToolsActivity).getBool(
                        "is_premium", false
                    ) && bannerEnabled && isEnabled
                ) {
                    //new
//                    binding.adLayout.visibility = View.VISIBLE
                }

            }
            satelliteReview = false
            check1 = true
        }
        super.onResume()
    }

    override fun onDestroy() {
        bounceAnim!!.cancel()
        adView?.let {
            it.destroy()
            val parent = it.parent as ViewGroup?
            parent?.removeView(adView)
        }
        super.onDestroy()
    }

    private fun handleLocationAccess() {
        if (!loactionflag) {
            currentLocation.getCurrentLocation()
//            Handler(Looper.getMainLooper()).postDelayed({
//                if (!isInternetAvailable(this@StartTwo)) {
//                    Log.d("weaether called", "false ")
//                    Toast.makeText(
//                        this,
//                        "internet not available",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    loactionflag = true
//                    Log.d("weaether called", "true ")
//                    weatherdata()
//                }
//            }, 3000)
        }
    }

//    private fun weatherdata() {
//        Log.e("weathr", "weatherdata: true $city")
//        weatherViewModel.fetchWeather(city, apiKey)
//        // Observe weather data
//        weatherViewModel.weatherData.observe(this) { weatherData ->
//            updateUI(weatherData)
//            Log.e("weathr", "vewmodel: true")
//        }
//
//        // Observe errors
//        weatherViewModel.error.observe(this) { errorMessage ->
//            errorMessage?.let {
//                Log.e("weathr", "vewmodel: ${errorMessage}")
//                loactionflag = false
//                //  Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
//            }
//        }
//
//        // Fetch weather data
//
//    }

    //    @SuppressLint("SimpleDateFormat", "SetTextI18n")
//    private fun updateUI(weatherData: WeatherData) {
//        Log.d("TAG", "updateUI: ")
//        val formatter = SimpleDateFormat("HH")
//        val currentHour = formatter.format(Date()).toInt()
////        binding.textView3.text = cityCountry
//        Log.e("weathr", "uidata: ${weatherData.main.temp.toInt()}")
//        formattedTemp = weatherData.main.temp.toInt()
//        binding.tempText.text = "${formattedTemp}°C"
////        if (StartActivity.formattedTemp <= 10) {
////            Log.d("TAG", "updateUI:rain ")
////            binding.weatherimg.setImageResource(R.drawable.rain)
////        } else {
////            if (currentHour in 6..16) {
////                Log.d("TAG", "updateUI:day ")
////                binding.weatherimg.setImageResource(R.drawable.dayicon2)
////            } else {
////                Log.d("TAG", "updateUI:night ")
////                binding.weatherimg.setImageResource(R.drawable.night)
////            }
////
////        }
//    }

    private fun setShimmerHorizontalMargin(startDp: Int, endDp: Int) {
        val layoutParams = binding.shimmer.layoutParams as ViewGroup.MarginLayoutParams
        val startPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            startDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val endPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            endDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        layoutParams.marginStart = startPx
        layoutParams.marginEnd = endPx
        binding.shimmer.layoutParams = layoutParams
    }

    private fun setTabSelected(selectedTab: ConstraintLayout) {
        val allTabs = listOf(binding.navHome, binding.navTools)

        for (tab in allTabs) {
            val isSelected = tab == selectedTab

            // Get references to inner layout, icon, and label
            val innerLayout = tab.findViewById<LinearLayout>(R.id.nav_home_inner)
                ?: tab.findViewById(R.id.nav_tools_inner)
            val icon =
                tab.findViewById<ImageView>(R.id.icon_home) ?: tab.findViewById(R.id.icon_tools)
            val label =
                tab.findViewById<TextView>(R.id.text_home) ?: tab.findViewById(R.id.text_tools)

            // Update background and colors
            if (isSelected) {
                innerLayout?.setBackgroundResource(R.drawable.bg_nav_selected)
                icon?.setColorFilter(ContextCompat.getColor(this, R.color.mBlue))
                label?.setTextColor(ContextCompat.getColor(this, R.color.mBlue))
            } else {
                innerLayout?.setBackgroundColor(Color.TRANSPARENT)
                icon?.setColorFilter(ContextCompat.getColor(this, R.color.grey))
                label?.setTextColor(ContextCompat.getColor(this, R.color.grey))
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data = intent.getBooleanExtra("refresh", false)
        val language = intent.getStringExtra("language")
        if (data) {
            Log.e("refresh", "onNewIntent: refresh true $language")
            recreate()
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromStart = true
    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            hideNavigationBarTemporarily()
//        }
//    }
//
//    fun hideNavigationBarTemporarily() {
//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
//    }
}