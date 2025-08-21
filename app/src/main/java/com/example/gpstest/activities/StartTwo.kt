package com.example.gpstest.activities

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.bannerHomeEnabled
import com.example.gpstest.MyApp.Companion.botInter
import com.example.gpstest.MyApp.Companion.check1
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.exitInterEnabled
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.native_exit_enabled
import com.example.gpstest.MyApp.Companion.satelliteReview
import com.example.gpstest.MyApp.Companion.weatherInter
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityStartTwoBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.AI_Bot_clicked
import com.example.gpstest.firebase.customevents.Companion.compass_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.drawer_close
import com.example.gpstest.firebase.customevents.Companion.drawer_open
import com.example.gpstest.firebase.customevents.Companion.languagee_screen
import com.example.gpstest.firebase.customevents.Companion.my_loc_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.near_by_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.premmium_clicked
import com.example.gpstest.firebase.customevents.Companion.rate_clicked
import com.example.gpstest.firebase.customevents.Companion.route_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.searchVoice_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.setellite_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.sevenwonders_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.share_clicked
import com.example.gpstest.firebase.customevents.Companion.streets_btn_clicked
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.nearby_places.NearByplacesActivity
import com.example.gpstest.utls.Constants.getScreenSizeInInches
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
import java.util.Objects
import kotlin.system.exitProcess

class StartTwo : BaseActivity() {
    lateinit var binding: ActivityStartTwoBinding
    var doubleBackToExitPressedOnce = false
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private var cameFromSettings = false

    private var navigatingFromStart = false
    private var loactionflag = false
    private lateinit var notificationPremDialog: Dialog
    private var bounceanimation: ObjectAnimator? = null
    private var bounceAnim: ObjectAnimator? = null
    private val apiKey = "a6450a304b61929119a1967808ae10a0"
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var currentLocation: CurrentLocation
    private lateinit var drawerLayout: DrawerLayout
    private var firsttime = true
    private var adView: AdView? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var locationHelper: LocationPermissionHelper
    private var review = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartTwoBinding.inflate(layoutInflater)

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
            if (!Prefutils(this@StartTwo).getBool(
                    "is_premium", false
                ) && bannerEnabled && isEnabled
            ) {
                binding.adLayout.visibility = View.INVISIBLE
            }
            InfoUtil(this).showRatingDialog {
                if (!Prefutils(this@StartTwo).getBool(
                        "is_premium", false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.VISIBLE
                }
            }
        }
//back press logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.myDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.myDrawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (doubleBackToExitPressedOnce) {
                        showExitInter()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@StartTwo, "Press back again to exit", Toast.LENGTH_SHORT)
                        .show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000) // 2 seconds to reset
                }
            }
        })

        setTabSelected(binding.navHome)

        binding.navHome.setOnClickListener {
            setTabSelected(binding.navHome)
//            Toast.makeText(this, "home", Toast.LENGTH_SHORT).show()
        }

        binding.navTools.setOnClickListener {
//            setTabSelected(binding.navTools)
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ToolsActivity::class.java))
        }
        FirebaseCustomEvents(this).createFirebaseEvents("home2 launch", "true")
        binding.textView5.isSelected = true
        binding.textView55.isSelected = true
//        binding.textView15.isSelected = true
//        binding.textView14.isSelected = true
//        binding.textView16.isSelected = true
//        binding.textView13.isSelected = true
        Log.d("refresh", "onCreate: called ")
        val prefUtil = Prefutils(this)
        prefUtil.setBool("isFirstTime", false)
        // banner ads
        if (prefUtil.getBool(
                "is_premium", false
            ) || !bannerEnabled || !isEnabled || !bannerHomeEnabled
        ) {
            binding.adLayout.visibility = View.GONE
        } else {
            if (isInternetAvailable(this)) {
                AdsManager.loadBannerAd(
                    binding.adLayout,
                    this@StartTwo,
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TEST TAG", "onAdFailed: Home Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "onAdLoaded: Home Banner")
                        }
                    })?.let { adView = it }
            }
        }
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
        drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        binding.indrawerMenu.a5.isChecked = isDarkMode
        binding.indrawerMenu.a5.setOnCheckedChangeListener { _, isChecked ->

            Prefutils(this).setBool("isDarkMode", isChecked)

            Handler(Looper.getMainLooper()).postDelayed({
                AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
                recreate()
                drawerLayout.closeDrawer(GravityCompat.START)
            }, 1000)
            // Recreate act
        }


        // Initialize permission launcher
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            setPermissionSwitchState(isGranted)

            Toast.makeText(
                this,
                if (isGranted) "Permission granted" else "Permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
        // === ON CREATE ===

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.indrawerMenu.notiPermission.visibility = View.VISIBLE

            // Set current permission state
            val isGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            setPermissionSwitchState(isGranted)
        } else {
            binding.indrawerMenu.notiPermission.visibility = View.GONE
        }


        binding.indrawerMenu.language.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(languagee_screen, "true")
            binding.indrawerMenu.language.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction {
                    binding.indrawerMenu.language.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            drawerLayout.closeDrawer(GravityCompat.START)
            Handler(Looper.getMainLooper()).postDelayed({

                startActivity(Intent(this@StartTwo, LanguageScreen::class.java))
            }, 200)

        }
        binding.indrawerMenu.rateUs.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(rate_clicked, "true")

            binding.indrawerMenu.rateUs.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction {
                    binding.indrawerMenu.rateUs.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
                FirebaseCustomEvents(this).createFirebaseEvents(
                    customevents.reviewdialog_showed, "true"
                )
                if (!Prefutils(this@StartTwo).getBool(
                        "is_premium", false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.INVISIBLE
                }
                InfoUtil(this).showRatingDialog {
                    if (!Prefutils(this@StartTwo).getBool(
                            "is_premium", false
                        ) && bannerEnabled && isEnabled
                    ) {
                        binding.adLayout.visibility = View.VISIBLE
                    }

                }
            }, 200)

        }
        binding.indrawerMenu.share.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(share_clicked, "true")
            binding.indrawerMenu.share.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction {
                    binding.indrawerMenu.share.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
                InfoUtil(this).shareApp()
            }, 200)

        }
        binding.indrawerMenu.privacyPolicy.clickWithDebounce {
            binding.indrawerMenu.privacyPolicy.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                .withEndAction {
                    binding.indrawerMenu.privacyPolicy.animate().scaleX(1f).scaleY(1f).duration =
                        100
                }
            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
                InfoUtil(this).openPrivacy()
            }, 200)

        }
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

        binding.btnWeather.clickWithDebounce {
            if (weatherInter) {
                if (mInterstitialAd != null) {
                    AdsManager.showInterstitial(
                        true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                            override fun onAdClosed() {
                                startActivity(Intent(this@StartTwo, weather::class.java))
                            }

                        }, "weather_activity"
                    )
                } else {
                    if (interstitialAd != null) {
                        showAvailableInterstitial(this) {
                            startActivity(Intent(this@StartTwo, weather::class.java))
                        }
                    } else {
                        InterstitialClass.requestInterstitial(this@StartTwo,
                            this@StartTwo,
                            "weather_activity",
                            object : ActionOnAdClosedListener {
                                override fun ActionAfterAd() {
                                    startActivity(Intent(this@StartTwo, weather::class.java))
                                }
                            }

                        )
                    }
                }
            } else {
                startActivity(Intent(this@StartTwo, weather::class.java))
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
            if (!isInternetAvailable(this@StartTwo)) {
                Toast.makeText(
                    this, "internet not available", Toast.LENGTH_SHORT
                ).show()
            } else {
                if (botInter) {
                    if (mInterstitialAd != null) {
                        AdsManager.showInterstitial(
                            true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                                override fun onAdClosed() {
                                    startActivity(Intent(this@StartTwo, AIChat::class.java))
                                }
                            }, "weather_activity"
                        )
                    } else {
                        if (interstitialAd != null) {
                            showAvailableInterstitial(this) {
                                startActivity(Intent(this@StartTwo, AIChat::class.java))
                            }
                        } else {
                            InterstitialClass.requestInterstitial(this@StartTwo,
                                this@StartTwo,
                                "Ai_activity",
                                object : ActionOnAdClosedListener {
                                    override fun ActionAfterAd() {
                                        startActivity(Intent(this@StartTwo, AIChat::class.java))
                                    }
                                })
                        }
                    }
                } else {
                    startActivity(Intent(this@StartTwo, AIChat::class.java))
                }
            }
            FirebaseCustomEvents(this).createFirebaseEvents(AI_Bot_clicked, "true")
        }
        binding.compass1.clickWithDebounce {
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
                    true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@StartTwo, CompassActivity::class.java
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
                                this@StartTwo, CompassActivity::class.java
                            )
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@StartTwo,
                        this@StartTwo,
                        "compass_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@StartTwo, CompassActivity::class.java
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
                    true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@StartTwo, Search::class.java
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
                                this@StartTwo, Search::class.java
                            ).putExtra("voice", true)
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@StartTwo,
                        this@StartTwo,
                        "search_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@StartTwo, Search::class.java
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
                    this@StartTwo, Search::class.java
                ).putExtra("search", true)
            )
        }
        binding.satelliteLayout1.clickWithDebounce {
//
//            binding.sateliteMap.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.sateliteMap.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({
//

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(this@StartTwo, SateliteView::class.java).putExtra(
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
                            Intent(this@StartTwo, SateliteView::class.java).putExtra(
                                "key", "sattelite"
                            )
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@StartTwo,
                        this@StartTwo,
                        "sattelite_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(this@StartTwo, SateliteView::class.java).putExtra(
                                        "key", "sattelite"
                                    )
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
                    true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@StartTwo, SateliteView::class.java
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
                                this@StartTwo,

                                SateliteView::class.java
                            ).putExtra("key", "streets")
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@StartTwo,
                        this@StartTwo,
                        "streets_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@StartTwo, SateliteView::class.java
                                    ).putExtra("key", "streets")
                                )
                            }
                        })
                }
            }

//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(streets_btn_clicked, "true")
        }
        binding.placesLayout1.clickWithDebounce {

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
                    true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@StartTwo, Sevenwonders::class.java))
                        }
                    }, "sevenwonders_activity"
                )

            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(Intent(this@StartTwo, Sevenwonders::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@StartTwo,
                        this@StartTwo,
                        "sevenwonders_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@StartTwo, Sevenwonders::class.java))
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
                val intent = Intent(this@StartTwo, InApp_Purchase_Screen::class.java)
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
                    true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@StartTwo, MainActivity::class.java))
                        }
                    }, "routeFinder_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this, true) {
                        startActivity(Intent(this@StartTwo, MainActivity::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@StartTwo,
                        this@StartTwo,
                        "routeFinder_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@StartTwo, MainActivity::class.java))
                            }
                        })
                }
            }


//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(route_btn_clicked, "true")
        }
        binding.myloc1.clickWithDebounce {
//
//
//            binding.myLoc.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.v.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@StartTwo, MyLoc::class.java))
                        }
                    }, "MyLocation_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(Intent(this@StartTwo, MyLoc::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(this@StartTwo,
                        this@StartTwo,
                        "MyLocation_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@StartTwo, MyLoc::class.java))
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
                        this@StartTwo, NearByplacesActivity::class.java
                    )
                )
            }, 200)
        }

    }

    private val permissionSwitchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (isChecked) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED

            if (!alreadyGranted) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    // User denied before, can ask again
                    notificationPermissionLauncher.launch(permission)
                } else {
                    // Denied permanently or from settings
                    showPermissionSettingsDialog()
                    // Revert the switch
                    setPermissionSwitchState(false)
                }
            }
        } else {
            // Can't revoke from code, just show message
            Toast.makeText(
                this,
                "You can't revoke this permission from the app. Go to App Settings.",
                Toast.LENGTH_LONG
            ).show()

            // Reset to real state
            val actuallyGranted = ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED

            setPermissionSwitchState(actuallyGranted)
        }
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Notification permission has been permanently denied. Please enable it in App Settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                cameFromSettings = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
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
            Log.e("TAG", "onCreate:5")

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
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.start_disappear, "true"
            )
        }
    }


    override fun onResume() {
        navigatingFromStart = false
        bounceAnim!!.resume()
        Log.d("TAG", "onResume: resume called")
        if (Prefutils(this).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        }

        Handler(Looper.getMainLooper()).postDelayed({
            checkPermissions()
        }, 3000)

        adView?.resume()

        if (satelliteReview) {
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.reviewdialog_showed, "true"
            )
            if (!Prefutils(this@StartTwo).getBool(
                    "is_premium", false
                ) && bannerEnabled && isEnabled
            ) {
                binding.adLayout.visibility = View.INVISIBLE
            }
            InfoUtil(this).showRatingDialog {
                if (!Prefutils(this@StartTwo).getBool(
                        "is_premium", false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.VISIBLE
                }
            }
            satelliteReview = false
            check1 = true
        }
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && cameFromSettings) {
            cameFromSettings = false
            val isGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            setPermissionSwitchState(isGranted)
        }
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
//        // Observe errors
//        weatherViewModel.error.observe(this) { errorMessage ->
//            errorMessage?.let {
//                Log.e("weathr", "vewmodel: ${errorMessage}")
//                loactionflag = false
//                //  Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
//            }
//        }
//        // Fetch weather data
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

    private fun bacPressLogic() {
        if (!isFinishing) {
            if (!Prefutils(this@StartTwo).getBool(
                    "is_premium", false
                ) && bannerEnabled && isEnabled
            ) {
                binding.adLayout.visibility = View.INVISIBLE
            }
            val dialog = Dialog(this@StartTwo)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            Objects.requireNonNull(dialog.window)
                ?.setBackgroundDrawableResource(android.R.color.transparent)
//                        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
            dialog.setContentView(R.layout.exitdialog)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog.setCancelable(false)
            //Animation
            val dialogView = dialog.window?.decorView
            dialogView?.scaleX = 0f
            dialogView?.scaleY = 0f
            val scaleXAnimator = ObjectAnimator.ofFloat(dialogView, "scaleX", 0f, 1f)
            val scaleYAnimator = ObjectAnimator.ofFloat(dialogView, "scaleY", 0f, 1f)
            scaleXAnimator.duration = 300
            scaleYAnimator.duration = 300
            scaleXAnimator.interpolator = DecelerateInterpolator()
            scaleYAnimator.interpolator = DecelerateInterpolator()
            scaleXAnimator.start()
            scaleYAnimator.start()

            dialog.show()

            fun dismissDialogWithAnimation() {
                val scaleXDismiss = ObjectAnimator.ofFloat(dialogView, "scaleX", 1f, 0f)
                val scaleYDismiss = ObjectAnimator.ofFloat(dialogView, "scaleY", 1f, 0f)
                scaleXDismiss.duration = 100
                scaleYDismiss.duration = 100
                scaleXDismiss.interpolator = DecelerateInterpolator()
                scaleYDismiss.interpolator = DecelerateInterpolator()

                // Start the dismiss animation and dismiss the dialog after it's done
                scaleXDismiss.start()
                scaleYDismiss.start()

                // Add a listener to dismiss the dialog after the animation
                scaleXDismiss.addListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        dialog.dismiss()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
            }
            dialog.show()
            val noWait = dialog.findViewById<AppCompatButton>(R.id.nobtn)
            val yesSure = dialog.findViewById<AppCompatButton>(R.id.yessure)
            val shimer = dialog.findViewById<com.facebook.shimmer.ShimmerFrameLayout>(R.id.shimmer)
            val adview = dialog.findViewById<RelativeLayout>(R.id.nativeAd)

            // new native ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            Handler(Looper.getMainLooper()).postDelayed({
                noWait.visibility = View.VISIBLE
                yesSure.visibility = View.VISIBLE

            }, 5000)

            if (Prefutils(this@StartTwo).getBool(
                    "is_premium", false
                ) || !native_exit_enabled || !isEnabled
            ) {
                shimer.visibility = View.GONE
                noWait.visibility = View.VISIBLE
                yesSure.visibility = View.VISIBLE
            } else {
                if (!isInternetAvailable(this@StartTwo)) {
                    shimer.visibility = View.GONE
                    noWait.visibility = View.VISIBLE
                    yesSure.visibility = View.VISIBLE
                } else {
                    AdsManager.loadNative(
                        adview, this@StartTwo, object : AdsManager.AdmobBannerAdListener {
                            override fun onAdLoaded() {
                                shimer.hideShimmer()
                                shimer.stopShimmer()
                                shimer.setBackgroundResource(R.color.white)
                                noWait.visibility = View.VISIBLE
                                yesSure.visibility = View.VISIBLE
                            }

                            override fun onAdFailed() {
                                shimer.hideShimmer()
                                shimer.stopShimmer()
                                shimer.setBackgroundResource(R.drawable.rounded_with_gray_light)
                                noWait.visibility = View.VISIBLE
                                yesSure.visibility = View.VISIBLE
                            }
                        }, AdsManager.NativeAdType.MEDIUM
                    )

                }
            }
            // new native ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            noWait.setOnClickListener {
                if (!Prefutils(this@StartTwo).getBool(
                        "is_premium", false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.VISIBLE
                }

                dismissDialogWithAnimation()
            }
            yesSure.setOnClickListener {
                navigatingFromStart = true
                Prefutils(this@StartTwo).setBool("first", true)
                Log.d(
                    "check", "onCreate:first ${
                        Prefutils(this@StartTwo).getBool(
                            "first", true
                        )
                    } "
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    finishAffinity()
                    exitProcess(0)
                }, 500)

            }
        } else {
            finish()
        }
    }

    private fun showExitInter() {
        if (!Prefutils(this@StartTwo).getBool(
                "is_premium", false
            ) && exitInterEnabled && MyApp.isEnabled && mInterstitialAd != null
        ) {
            AdsManager.showInterstitial(
                true, this@StartTwo, object : AdsManager.InterstitialAdListener {
                    override fun onAdClosed() {
                        bacPressLogic()
                    }

                }, ""
            )
        } else {
            bacPressLogic()
        }
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

    private fun setPermissionSwitchState(isChecked: Boolean) {
        binding.indrawerMenu.permissionSwitch.setOnCheckedChangeListener(null)
        binding.indrawerMenu.permissionSwitch.isChecked = isChecked
        binding.indrawerMenu.permissionSwitch.setOnCheckedChangeListener(permissionSwitchListener)
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