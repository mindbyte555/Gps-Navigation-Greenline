package com.example.gpstest.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.AdIds
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.counter
import com.example.gpstest.AdsManager.GoogleMobileAdsConsentManager
import com.example.gpstest.CurrentLocation
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.adcounter
import com.example.gpstest.MyApp.Companion.appOpenEnabled
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.firstinterCounter
import com.example.gpstest.MyApp.Companion.firstkey
import com.example.gpstest.MyApp.Companion.interstitialCounter
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.whiteregion
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivitySplashBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.splash_open
import com.example.gpstest.subscription.GooglePlayBuySubscription
import com.example.gpstest.utls.Constants.getScreenSizeInInches
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

class Splash : BaseActivity() {
    lateinit var binding: ActivitySplashBinding
    val prefUtil = Prefutils(this)
    private var adView: AdView? = null

    private lateinit var currentLocation: CurrentLocation
    private lateinit var locationViewModel: LocationViewModel
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private var fullScreenContentCallback: FullScreenContentCallback? = null
    var openAd: AppOpenAd? = null
    private var isAdLoaded = false
    private var isAdFailed = false
    private var start = 0
    private var isTimeUP = false
    var delaytime: Long = 180
    private var isAlReadyShow = false
    private var navigatingFromSplash = false
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    //   private val splashHandler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var updateRunnable: Runnable

    //    private val splashRunnable = Runnable {
//        binding.progress.visibility = View.GONE
//        if (!isAdFailed) {
//            if (!isAdLoaded) {
//                launchNextActivity()
//                isTimeUP = true
//            }
//        } else {
//            launchNextActivity()
//        }
//    }
    companion object {
        var isConsent = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        Screen size: 5.97 inches
//        Screen size: 5.46 inches
//        Screen size: 6.67 inches

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val statusBarInsets =
                insets.getInsets(WindowInsetsCompat.Type.statusBars()) // Only Status Bar
            v.setPadding(
                statusBarInsets.left,
                statusBarInsets.top,
                statusBarInsets.right,
                0
            ) // No bottom padding
            insets
        }
        //init billing client
        GooglePlayBuySubscription.initBillingClient(this)
        GooglePlayBuySubscription.makeGooglePlayConnectionRequest()
        showBanner()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            window?.decorView?.windowInsetsController?.setSystemBarsAppearance(
                APPEARANCE_LIGHT_NAVIGATION_BARS, APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window?.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }


        if (Prefutils(this@Splash).getBool("is_premium", false)) {
            delaytime = 40
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        counter = 0
        FirebaseCustomEvents(this).createFirebaseEvents(splash_open, "true")
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)

        if (InfoUtil(this).isNetworkAvailable(this)) {
            googleMobileAdsConsentManager.gatherConsent(this)
            {
                if (googleMobileAdsConsentManager.canRequestAds) {
                    if (googleMobileAdsConsentManager.isConsentAvailable) {
                        calledOnlyInter()
                    } else {
                        callBoth()
                    }
                }
                if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                    invalidateOptionsMenu()
                }
            }
        } else {
            delaytime = 40
            simulateExternalUpdates()
        }
        if (InfoUtil(this).isNetworkAvailable(this)) {
            if (googleMobileAdsConsentManager.canRequestAds) {
                if (googleMobileAdsConsentManager.isConsentAvailable) {
                    calledOnlyInter()
                } else {
                    callBoth()
                }
            }
        }

        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        currentLocation = CurrentLocation(this, this, locationViewModel, 0)

    }

    private fun showBanner() {
        if (prefUtil.getBool("is_premium", false) || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        } else {
            if (isInternetAvailable(this)) {
                AdsManager.loadSplashBanner(binding.adLayout,
                    this@Splash,
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TEST TAG", "onAdFailed: Splash Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "onAdLoaded: Splash Banner")
                        }
                    })?.let { adView = it }
            }
        }
    }

    private fun callBoth() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            Log.e("TEST TAG", "1 called other than consent")
            return
        }
        AdsManager(applicationContext)
        Log.e("TEST TAG", "called other than consent")

        Log.e("TEST TAG", "FIRST TIME222")
        Handler(Looper.getMainLooper()).postDelayed({
            if (Prefutils(this).getBool("isFirstTime", true)) {
                FirebaseCustomEvents(this).createFirebaseEvents(
                    customevents.first_time_user,
                    "true"
                )
                firstkey = true
                adcounter = firstinterCounter
                //   AdsManager.loadInterstitial(this)
                //    AdsManager.load_Lan_Interstitial(this)
            } else {
                FirebaseCustomEvents(this).createFirebaseEvents(
                    customevents.second_time_user,
                    "true"
                )
                firstkey = false
                adcounter = interstitialCounter
                //   AdsManager.loadInterstitial(this)
            }

            if (appOpenEnabled && isEnabled) {
                loadSplashOpenAd()
            }
        }, 3000)
        isConsent = false
        simulateExternalUpdates()
    }

    private fun calledOnlyInter() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            Log.e("TEST TAG", "called once when consent")
            return
        }
        AdsManager(applicationContext)
        Log.e("TEST TAG", "called once when consent")

        Log.e("TEST TAG", "FIRST TIME3332")
        Handler(Looper.getMainLooper()).postDelayed({
            if (Prefutils(this).getBool("isFirstTime", true)) {
                FirebaseCustomEvents(this).createFirebaseEvents(
                    customevents.first_time_user,
                    "true"
                )
                firstkey = true
                adcounter = firstinterCounter
                // AdsManager.loadInterstitial(this)
                //    AdsManager.load_Lan_Interstitial(this)
            } else {
                firstkey = false
                FirebaseCustomEvents(this).createFirebaseEvents(
                    customevents.second_time_user,
                    "true"
                )
                adcounter = interstitialCounter
                //   AdsManager.loadInterstitial(this)
            }
            if (appOpenEnabled && isEnabled) {
                loadSplashOpenAd()
            }
        }, 3000)

        isConsent = false

        simulateExternalUpdates()

    }

    private fun simulateExternalUpdates() {
        // Simulating random updates from an external source
        updateRunnable = Runnable {
            if (start < 100) {
                start += 1
                updateProgressContinuously()
                handler.postDelayed(updateRunnable, delaytime) // Continue updating every 50ms
            }
        }

        handler.post(updateRunnable) // Start the updates
    }

    private val maxProgress = 100

    private fun updateProgressContinuously() {
        // Calculate progress percentage based on currentValue
        val percentage = (start * 100) / maxProgress
        binding.progress.progress = percentage

        binding.progress.progress = start

        binding.tvPercentage.text = "$percentage%"
        if (binding.progress.progress == 100) {
            // Stop updates and go to MainActivity
            handler.removeCallbacks(updateRunnable) // Stop the updates
            proceedToMainActivity()
        }
    }

    private fun proceedToMainActivity() {
        Log.e("TEST TAG", "Before Handler")
        binding.progress.visibility = View.INVISIBLE
        binding.tvPercentage.visibility = View.INVISIBLE
        if (!isAdFailed) {
            if (!isAdLoaded) {
                launchNextActivity()
                isTimeUP = true
            }
        } else {
            launchNextActivity()
        }
        //  splashHandler.postDelayed(splashRunnable, 12000)
    }

    private fun loadSplashOpenAd() {
        Log.e("TEST TAG", "load app open")
        Prefutils(this).setBool("isShowed", false)

        if (Prefutils(this@Splash).getBool(
                "is_premium",
                false
            ) || (!appOpenEnabled) || !isEnabled
        ) {
            return
        }

        val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.e("AdManagerAds", "onAdLoaded: $ad")
                    openAd = ad
                    isAdLoaded = true // Set the flag to indicate that the ad is loaded
                    showSplashOpenAd()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    isAdFailed = true

                    Log.e("AdManagerAds", "onAdLoaded: $p0")
                    // Handle ad loading failure
                }
            }
        val request: AdRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            AdIds.getAppOpenId(),
            request,
            loadCallback
        )
    }

    fun showSplashOpenAd() {
        if (isAlReadyShow) return
        if (isTimeUP) return
        Log.e("TEST TAG", "app open called")
        fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                launchNextActivity()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                if (!isFinishing) {
                    launchNextActivity()
                }
            }

            override fun onAdShowedFullScreenContent() {
                binding.progress.visibility = View.INVISIBLE
                binding.tvPercentage.visibility = View.INVISIBLE
                binding.adLayout.visibility = View.GONE

            }
        }
        openAd?.fullScreenContentCallback = fullScreenContentCallback
        openAd!!.show(this)
        isAlReadyShow = true
    }

    private fun launchNextActivity() {
        if (Prefutils(this).getBool("isFirstTime", true)) {
            startActivity(Intent(this, OnbActivity::class.java))
            finish()
        } else {
            if (Prefutils(this@Splash).getBool("is_premium", false)) {
//                if (newUi) {
                startActivity(Intent(this, StartTwo::class.java))
                finish()
//                } else {
//                    startActivity(Intent(this, StartActivity::class.java))
//                    finish()
//                }

            } else {
                startActivity(Intent(this, InApp_Purchase_Screen::class.java))
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!navigatingFromSplash) {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.splash_disappear, "true")
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromSplash = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::updateRunnable.isInitialized) {
            handler.removeCallbacks(updateRunnable)
        }
        adView?.let {
            it.destroy()
            val parent = it.parent as ViewGroup?
            parent?.removeView(adView)
        }
    }
}