package com.example.gpstest.activities

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.city
import com.example.gpstest.CurrentLocation.Companion.cityCountry
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.check1
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.formattedTemp
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.nativeEnabled
import com.example.gpstest.MyApp.Companion.satelliteReview
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityStartBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.AI_Bot_clicked
import com.example.gpstest.firebase.customevents.Companion.compass_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.drawer_close
import com.example.gpstest.firebase.customevents.Companion.drawer_open
import com.example.gpstest.firebase.customevents.Companion.homeactivity_open
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
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.example.gpstest.weather.WeatherData
import com.example.gpstest.weather.WeatherService
import com.example.gpstest.weather.WeatherViewModel
import com.example.gpstest.weather.WeatherViewModelFactory
import com.google.android.gms.ads.AdView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import kotlin.system.exitProcess

class StartActivity : BaseActivity() {
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
    lateinit var binding: ActivityStartBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var locationHelper: LocationPermissionHelper
    private var review = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // AdsManager.loadInterstitial(this)
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

        InfoUtil(this).setSystemBarsColor(R.attr.backgroundColor)
        if (intent != null) {
            review = intent.getBooleanExtra("review", false)
        }
        if (review) {
//                showReviewDialog()
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.reviewdialog_showed,
                "true"
            )
            if (!Prefutils(this@StartActivity).getBool(
                    "is_premium",
                    false
                ) && bannerEnabled && isEnabled
            ) {
                binding.adLayout.visibility = View.INVISIBLE
            }
            InfoUtil(this).showRatingDialog {
                if (!Prefutils(this@StartActivity).getBool(
                        "is_premium",
                        false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.VISIBLE
                }
            }


        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (!isFinishing) {
                        if (!Prefutils(this@StartActivity).getBool(
                                "is_premium",
                                false
                            ) && bannerEnabled && isEnabled
                        ) {
                            binding.adLayout.visibility = View.INVISIBLE
                        }
                        val dialog = Dialog(this@StartActivity)
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
                        val shimer =
                            dialog.findViewById<com.facebook.shimmer.ShimmerFrameLayout>(R.id.shimmer)
                        val adview = dialog.findViewById<RelativeLayout>(R.id.nativeAd)

                        // new native ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        Handler(Looper.getMainLooper()).postDelayed({
                            noWait.visibility = View.VISIBLE
                            yesSure.visibility = View.VISIBLE

                        }, 5000)

                        if (Prefutils(this@StartActivity).getBool(
                                "is_premium",
                                false
                            ) || !nativeEnabled || !isEnabled
                        ) {
                            shimer.visibility = View.GONE
                            noWait.visibility = View.VISIBLE
                            yesSure.visibility = View.VISIBLE
                        } else {
                            if (!isInternetAvailable(this@StartActivity)) {
                                shimer.visibility = View.GONE
                                noWait.visibility = View.VISIBLE
                                yesSure.visibility = View.VISIBLE
                            } else {
                                AdsManager.loadNative(
                                    adview,
                                    this@StartActivity,
                                    object : AdsManager.AdmobBannerAdListener {
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
                            if (!Prefutils(this@StartActivity).getBool(
                                    "is_premium",
                                    false
                                ) && bannerEnabled && isEnabled
                            ) {
                                binding.adLayout.visibility = View.VISIBLE
                            }

                            dismissDialogWithAnimation()
                        }
                        yesSure.setOnClickListener {
                            navigatingFromStart = true
                            Prefutils(this@StartActivity).setBool("first", true)
                            Log.d(
                                "check",
                                "onCreate:first ${
                                    Prefutils(this@StartActivity).getBool(
                                        "first",
                                        true
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
            }
        })

        //for notification in version 14+
//        if (Prefutils(this).getBool("isShowing", true)) {
//            Log.d("check", "onCreate:isShowing true ${Prefutils(this).getBool("first", true)} ")
//            if (Prefutils(this).getBool("first", true)) {
//                Log.d("check", "onCreate:first true ")
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    if (ContextCompat.checkSelfPermission(
//                            this,
//                            Manifest.permission.POST_NOTIFICATIONS
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {`
//                        notificationPermissionDialog()
//                    }
//                }
//            }
//        }

        binding.st1.isSelected = true
        binding.st2.isSelected = true
        binding.st3.isSelected = true
        binding.st4.isSelected = true
        binding.st5.isSelected = true
        binding.st6.isSelected = true
        Log.d("refresh", "onCreate: called ")
        val prefUtil = Prefutils(this)
        prefUtil.setBool("isFirstTime", false)
        // banner ads
        if (prefUtil.getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        } else {
            if (isInternetAvailable(this)) {
                AdsManager.loadHomeBannerAd(binding.adLayout,
                    this@StartActivity, "",
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
        FirebaseCustomEvents(this).createFirebaseEvents(homeactivity_open, "true")
        binding.textView66.isSelected = true
        binding.textView6.isSelected = true
        binding.grantperm.isSelected = true
        locationHelper = LocationPermissionHelper(this)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        currentLocation = CurrentLocation(this, this, locationViewModel, 1)
        val weatherService = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
        val factory = WeatherViewModelFactory(weatherService)
        weatherViewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]

        Glide.with(this).load(R.drawable.premiumcrow).into(binding.premiumimg)
        //drawer
        val isDarkMode = Prefutils(this).getBool("isDarkMode")
        checkPermissions()
        bounceAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.botLayout,
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

        bounceanimation = ObjectAnimator.ofPropertyValuesHolder(
            binding.grantperm,
            PropertyValuesHolder.ofFloat("scaleX", 0.97f, 1.02f),
            PropertyValuesHolder.ofFloat("scaleY", 0.97f, 1.05f)
        ).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = DecelerateInterpolator() // Smooth transition
        }
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
        binding.indrawerMenu.language.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(languagee_screen, "true")
            binding.indrawerMenu.language.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.indrawerMenu.language.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            drawerLayout.closeDrawer(GravityCompat.START)
            Handler(Looper.getMainLooper()).postDelayed({

                startActivity(Intent(this@StartActivity, LanguageScreen::class.java))
            }, 200)

        }
        binding.indrawerMenu.rateUs.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(rate_clicked, "true")

            binding.indrawerMenu.rateUs.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.indrawerMenu.rateUs.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
                FirebaseCustomEvents(this).createFirebaseEvents(
                    customevents.reviewdialog_showed,
                    "true"
                )
                if (!Prefutils(this@StartActivity).getBool(
                        "is_premium",
                        false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.INVISIBLE
                }
                InfoUtil(this).showRatingDialog {
                    if (!Prefutils(this@StartActivity).getBool(
                            "is_premium",
                            false
                        ) && bannerEnabled && isEnabled
                    ) {
                        binding.adLayout.visibility = View.VISIBLE
                    }

                }
            }, 200)

        }
        binding.indrawerMenu.share.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(share_clicked, "true")
            binding.indrawerMenu.share.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.indrawerMenu.share.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            Handler(Looper.getMainLooper()).postDelayed({
                drawerLayout.closeDrawer(GravityCompat.START)
                InfoUtil(this).shareApp()
            }, 200)

        }
        binding.indrawerMenu.privacyPolicy.clickWithDebounce {
            binding.indrawerMenu.privacyPolicy.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
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

            binding.cardView.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
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
        binding.botLayout.clickWithDebounce {

            FirebaseCustomEvents(this).createFirebaseEvents(AI_Bot_clicked, "true")
            if (!isInternetAvailable(this@StartActivity)) {
                Toast.makeText(
                    this,
                    "internet not available",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startActivity(Intent(this@StartActivity, AIChat::class.java))
            }
        }

        binding.constraintLayout4.clickWithDebounce {
//            binding.constraintLayout4.animate()
//                .scaleX(0.9f)
//                .scaleY(0.9f)
//                .setDuration(100)
//                .withEndAction {
//                    binding.constraintLayout4.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
            //   Handler(Looper.getMainLooper()).postDelayed({
            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true,
                    this@StartActivity,
                    object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@StartActivity,
                                    CompassActivity::class.java
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
                                this@StartActivity,
                                CompassActivity::class.java
                            )
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(
                        this@StartActivity,
                        this@StartActivity,
                        "compass_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@StartActivity,
                                        CompassActivity::class.java
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
        binding.constraintLayout5.clickWithDebounce {


//            binding.constraintLayout5.animate()
//                .scaleX(0.9f)
//                .scaleY(0.9f)
//                .setDuration(100)
//                .withEndAction {
//                    binding.constraintLayout5.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true,
                    this@StartActivity,
                    object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@StartActivity,
                                    Search::class.java
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
                                this@StartActivity,
                                Search::class.java
                            ).putExtra("voice", true)
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(
                        this@StartActivity,
                        this@StartActivity,
                        "search_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@StartActivity,
                                        Search::class.java
                                    ).putExtra("voice", true)
                                )
                            }
                        }
                    )
                }
            }

            FirebaseCustomEvents(this).createFirebaseEvents(searchVoice_btn_clicked, "true")
            //    }, 200)

        }
        binding.constraintLayout2.clickWithDebounce {
//
//            binding.constraintLayout2.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.constraintLayout2.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({
//

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true,
                    this@StartActivity,
                    object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(this@StartActivity, SateliteView::class.java)
                                    .putExtra("key", "sattelite")
                            )
                        }
                    }, "sattelite_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(
                            Intent(this@StartActivity, SateliteView::class.java)
                                .putExtra("key", "sattelite")
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(
                        this@StartActivity,
                        this@StartActivity,
                        "sattelite_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(this@StartActivity, SateliteView::class.java)
                                        .putExtra("key", "sattelite")
                                )
                            }
                        }
                    )
                }
            }

//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(setellite_btn_clicked, "true")
        }
        binding.constraintLayout.clickWithDebounce {
//
//            binding.constraintLayout.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.constraintLayout.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
////            Handler(Looper.getMainLooper()).postDelayed({

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true,
                    this@StartActivity,
                    object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    this@StartActivity,
                                    SateliteView::class.java
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
                                this@StartActivity,

                                SateliteView::class.java
                            ).putExtra("key", "streets")
                        )
                    }
                } else {
                    InterstitialClass.requestInterstitial(
                        this@StartActivity,
                        this@StartActivity,
                        "streets_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(
                                    Intent(
                                        this@StartActivity,
                                        SateliteView::class.java
                                    ).putExtra("key", "streets")
                                )
                            }
                        }
                    )
                }
            }

//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(streets_btn_clicked, "true")
        }
        binding.famouplaces.clickWithDebounce {

//            binding.famouplaces.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.famouplaces.animate().scaleX(1f).scaleY(1f).duration = 100
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
                    true,
                    this@StartActivity,
                    object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@StartActivity, Sevenwonders::class.java))
                        }
                    }, "sevenwonders_activity"
                )

            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(Intent(this@StartActivity, Sevenwonders::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(
                        this@StartActivity,
                        this@StartActivity,
                        "sevenwonders_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@StartActivity, Sevenwonders::class.java))
                            }
                        }
                    )
                }
            }


//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(sevenwonders_btn_clicked, "true")
        }
        binding.cardView2.clickWithDebounce {

            binding.cardView2.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.cardView2.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@StartActivity, InApp_Purchase_Screen::class.java)
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
                    true,
                    this@StartActivity,
                    object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@StartActivity, MainActivity::class.java))
                        }
                    }, "routeFinder_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this, true) {
                        startActivity(Intent(this@StartActivity, MainActivity::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(
                        this@StartActivity,
                        this@StartActivity,
                        "routeFinder_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@StartActivity, MainActivity::class.java))
                            }
                        }
                    )
                }
            }


//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(route_btn_clicked, "true")
        }
        binding.constraintLayout3.clickWithDebounce {
//
//
//            binding.constraintLayout3.animate()
//                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
//                    binding.constraintLayout3.animate().scaleX(1f).scaleY(1f).duration = 100
//                }
//            Handler(Looper.getMainLooper()).postDelayed({

            if (mInterstitialAd != null) {
                AdsManager.showInterstitial(
                    true,
                    this@StartActivity,
                    object : AdsManager.InterstitialAdListener {
                        override fun onAdClosed() {
                            startActivity(Intent(this@StartActivity, MyLoc::class.java))
                        }
                    }, "MyLocation_activity"
                )
            } else {
                if (interstitialAd != null) {
                    showAvailableInterstitial(this) {
                        startActivity(Intent(this@StartActivity, MyLoc::class.java))
                    }
                } else {
                    InterstitialClass.requestInterstitial(
                        this@StartActivity,
                        this@StartActivity,
                        "MyLocation_activity",
                        object : ActionOnAdClosedListener {
                            override fun ActionAfterAd() {
                                startActivity(Intent(this@StartActivity, MyLoc::class.java))
                            }
                        }
                    )
                }
            }

//            }, 200)
            FirebaseCustomEvents(this).createFirebaseEvents(my_loc_btn_clicked, "true")
        }
        binding.constraintLayout6.clickWithDebounce {
            FirebaseCustomEvents(this).createFirebaseEvents(near_by_btn_clicked, "true")
            binding.constraintLayout6.animate()
                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                    binding.constraintLayout6.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(
                    Intent(
                        this@StartActivity,
                        NearByplacesActivity::class.java
                    )
                )
            }, 200)
        }
        binding.grantperm.clickWithDebounce {
            binding.grantperm.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.grantperm.animate().scaleX(1f).scaleY(1f).duration = 100
                Handler(Looper.getMainLooper()).postDelayed({
                    if (locationHelper.isLocationPermissionGranted()) {
                        locationHelper.openLocationSettings(this)

                    } else {
                        locationHelper.requestLocationPermission(this)
                    }

                }, 200)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notificationPermissionDialog() {
        this.let {
            notificationPremDialog = Dialog(it)
            notificationPremDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            notificationPremDialog.setCancelable(true)
            Objects.requireNonNull(notificationPremDialog.window)
                ?.setBackgroundDrawableResource(android.R.color.transparent)
            notificationPremDialog.setContentView(R.layout.dialog_notification_permission)
            notificationPremDialog.setCanceledOnTouchOutside(false)

            // Set animation
            notificationPremDialog.window?.attributes?.windowAnimations =
                R.style.DialogSlideUpAnimation

            val allow: TextView? = notificationPremDialog.findViewById(R.id.allow)
            val notAllows: TextView? = notificationPremDialog.findViewById(R.id.don_allow)

            allow?.setOnClickListener {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
                if (notificationPremDialog.isShowing) {
                    notificationPremDialog.dismiss()
                }
                Prefutils(this).setBool("first", false)
                Log.d("check", "on allow: ${Prefutils(this).getBool("first", true)}")
            }
            notAllows?.setOnClickListener {
                if (notificationPremDialog.isShowing) {
                    notificationPremDialog.dismiss()
                }
                Prefutils(this).setBool("first", false)
                Log.d("check", "on dontallow: ${Prefutils(this).getBool("first", true)}")
            }
            try {
                notificationPremDialog.show()
            } catch (_: Exception) {
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
                bounceanimation?.start()
                binding.grantperm.text = resources.getString(R.string.grant_permission)
                binding.permissionLayout.visibility = View.VISIBLE
                binding.weatherContent.visibility = View.GONE
            }

            !locationHelper.isLocationEnabled() -> {
                bounceanimation?.start()
                binding.grantperm.text = resources.getString(R.string.enable_gps)
                binding.permissionLayout.visibility = View.VISIBLE
                binding.weatherContent.visibility = View.GONE
            }

            else -> {
                bounceanimation?.cancel()
                handleLocationAccess()
                binding.permissionLayout.visibility = View.GONE
                binding.weatherContent.visibility = View.VISIBLE
            }
        }
    }

    private fun weatherdata() {
        weatherViewModel.fetchWeather(city, apiKey)
        // Observe weather data
        weatherViewModel.weatherData.observe(this) { weatherData ->
            Log.d("TAG", "weatherdata:${weatherData.main.temp}")
            Log.d("TAG", "weatherdata:${weatherData.weather[0].icon}")
            updateUI(weatherData)
        }

        // Observe errors
        weatherViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                loactionflag = false
                //  Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }

        // Fetch weather data

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

    private fun handleLocationAccess() {
        currentLocation.getCurrentLocation()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isInternetAvailable(this@StartActivity)) {
                Log.d("weaether called", "false ")
                Toast.makeText(
                    this,
                    "internet not available",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loactionflag = true
                Log.d("weaether called", "true ")
                weatherdata()
            }
        }, 3000)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateUI(weatherData: WeatherData) {
        Log.d("TAG", "updateUI: ")
        val formatter = SimpleDateFormat("HH")
        val currentHour = formatter.format(Date()).toInt()
        binding.textView3.text = cityCountry
        formattedTemp = weatherData.main.temp.toInt()
        binding.textView.text = "${formattedTemp}°C"
        if (formattedTemp <= 10) {
            Log.d("TAG", "updateUI:rain ")
            binding.weatherimg.setImageResource(R.drawable.rain)
        } else {
            if (currentHour in 6..16) {
                Log.d("TAG", "updateUI:day ")
                binding.weatherimg.setImageResource(R.drawable.dayicon2)
            } else {
                Log.d("TAG", "updateUI:night ")
                binding.weatherimg.setImageResource(R.drawable.night)
            }

        }
    }

    override fun onResume() {
        navigatingFromStart = false
        bounceAnim!!.resume()
        Log.d("TAG", "onResume: resume called")
        if (Prefutils(this).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        }
        if (!loactionflag) {
            Log.d("TAG", "onResume: handle called")
            checkPermissions()
        }

        adView?.resume()

        if (satelliteReview) {
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.reviewdialog_showed,
                "true"
            )
            if (!Prefutils(this@StartActivity).getBool(
                    "is_premium",
                    false
                ) && bannerEnabled && isEnabled
            ) {
                binding.adLayout.visibility = View.INVISIBLE
            }
            InfoUtil(this).showRatingDialog {
                if (!Prefutils(this@StartActivity).getBool(
                        "is_premium",
                        false
                    ) && bannerEnabled && isEnabled
                ) {
                    binding.adLayout.visibility = View.VISIBLE
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


    private fun openDrawerWithAnimation(drawerLayout: DrawerLayout) {
        val drawer = drawerLayout.findViewById<View>(R.id.navView)
        val animator = ObjectAnimator.ofFloat(drawer, "translationX", -drawer.width.toFloat(), 0f)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 600 //milli seconds
        animator.start()
        drawerLayout.openDrawer(GravityCompat.START)
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
}

