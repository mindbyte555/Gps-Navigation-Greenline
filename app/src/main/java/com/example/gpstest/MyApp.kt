package com.example.gpstest

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_AppOpen
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_Banner
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_Inline_Banner
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_Interstitial
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_Lang_Inter
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_Native
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_RecBanner
import com.example.gpstest.AdsManager.AdIds.Companion.Admob_RecNew
import com.example.gpstest.AdsManager.AdIds.Companion.AppOpen_resume
import com.example.gpstest.AdsManager.AdIds.Companion.aiadunit_id
import com.example.gpstest.AdsManager.AppOpenAdManager
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.RemoteConfigManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MyApp : Application(), DefaultLifecycleObserver {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var remoteConfigManager: RemoteConfigManager

    private val supportedCountries = listOf(
        "US",
        "BR",
        "TR",
        "TH",
        "AR",
        "IT",
        "MX",
        "TW",
        "GB",
        "CO",
        "FR",
        "RO",
        "ES",
        "AU",
        "KR",
        "ZA",
        "CA",
        "JP",
        "DE",
        "NL"
    )

    private val firebaseToken by lazy {
        FirebaseMessaging.getInstance().token
    }

    override fun onCreate() {
        super<Application>.onCreate()
//        val config = ClarityConfig("rbo6et4tze")
//        Clarity.initialize(applicationContext, config)
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkCountryCode = telephonyManager.networkCountryIso?.uppercase() ?: "Unknown"
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        if (networkCountryCode in supportedCountries) {
            whiteregion = true
            Log.d("MyApp", "Supported country:$whiteregion $networkCountryCode")
        } else {
            whiteregion = false
            Log.d("MyApp", "not country:$whiteregion $networkCountryCode")
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        remoteConfigManager = RemoteConfigManager()



        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                Log.d("MyApp", "Activity Resumed: ${activity.localClassName}")
            }

            override fun onActivityPaused(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
                Log.d("MyApp", "Activity Paused: ${activity.localClassName}")
            }

            // Other overrides are optional
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityStopped(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })




        firebaseToken.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Current Token: $token")
            }
        }
        fetchAndApplyRemoteConfig()
        setupConfigUpdateListener()
        FirebaseCustomEvents(this)

    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        val excludedActivities = setOf(
            "InApp_Purchase_Screen",
            "Splash",
            "OnbActivity",
            "LanguageScreen",
            "AdActivity"
        )

        val currentName = currentActivity?.javaClass?.simpleName
        Log.e("AppOpenAdResume", "onStart activity: $currentName")

        if (!excludedActivities.contains(currentName)) {
            currentActivity?.let { AppOpenAdManager.showAdIfAvailable(it) }
            Log.e("AppOpenAdResume", "Showing AppOpenAd in $currentName")
        } else {
            Log.e("AppOpenAdResume", "Skipping AppOpenAd for $currentName")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        AppOpenAdManager.loadAd(this)
        Log.e("AppOpenAdResume", "onStop")
    }

    private fun fetchAndApplyRemoteConfig() {
        remoteConfigManager.fetchRemoteConfig { isSuccess ->
            if (isSuccess) {
                applyRemoteConfigValues()
            }
        }
    }

    private fun applyRemoteConfigValues() {
        CoroutineScope(Dispatchers.IO).launch {
            parseAdConfig(remoteConfigManager.getString("ad_config"))
            ModelConfig(remoteConfigManager.getString("ai_config"))
            newUi = remoteConfigManager.getBoolean("new_ui")
            weatherInter = remoteConfigManager.getBoolean("weather_inter")
        }
    }

    private fun ModelConfig(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val apiConfig = jsonObject.getJSONObject("api_config")
            selected_model = apiConfig.getInt("selected_model")
            baseUrl = apiConfig.getString("base_url")
            apiKey = apiConfig.getString("api_key")

            val model1 = apiConfig.getJSONObject("1")
            modelName1 = model1.getString("model")

            val model2 = apiConfig.getJSONObject("2")
            modelName2 = model2.getString("model")

            val model3 = apiConfig.getJSONObject("3")
            modelName3 = model3.getString("model")


            if (BuildConfig.DEBUG) {
                Log.d("TEST TAG", "Selected Model: $selected_model")
                Log.d("TEST TAG", "Base URL: $baseUrl")
                Log.d("TEST TAG", "API Key: $apiKey")

                Log.d("TEST TAG", "Model 1: $modelName1")
                Log.d("TEST TAG", "Model 2: $modelName2")
                Log.d("TEST TAG", "Model 3: $modelName3")
            }

        } catch (e: Exception) {
            Log.e("TEST TAG", "Error parsing Model JSON: ${e.message}")
        }
    }

    private fun parseAdConfig(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)

            isEnabled = jsonObject.getBoolean("enabled")

            val appOpen = jsonObject.getJSONObject("AppOpen")
            appOpenEnabled = appOpen.getBoolean("enabled")
            appOpen_resume_enabled = appOpen.getBoolean("appOpen_resume_enabled")
            Admob_AppOpen = appOpen.getString("ad_unit_id")
            AppOpen_resume = appOpen.getString("ad_unit_on_resume")

            val interstitial = jsonObject.getJSONObject("Interstitial")
            interstitialEnabled = interstitial.getBoolean("enabled")
            interstitialMappilaryEnabled = interstitial.getBoolean("interstitialMappilaryEnabled")
            Admob_Interstitial = interstitial.getString("ad_unit_id")
            interstitialCounter = interstitial.getInt("load_counter")
            firstinterCounter = interstitial.getInt("inter_first_count")
            botInter = interstitial.getBoolean("bot_inter_enabled")
            exitInterEnabled = interstitial.getBoolean("exit_inter_enabled")

            val banner = jsonObject.getJSONObject("Banner")
            bannerEnabled = banner.getBoolean("enabled")
            Admob_Banner = banner.getString("ad_unit_id")
            bannerAdaptive = banner.getBoolean("Banner_Adaptive_Enabled")
            bannerCollapsible = banner.getBoolean("Banner_Collapsible_Enabled")
            bannerHomeEnabled = banner.getBoolean("Banner_Home_Collpsible")
            aicollapsable = banner.getBoolean("Banner_Ai_Collpsible")
            aiadunit_id = banner.getString("ai_ad_unit_id")

            val inlineBanner = jsonObject.getJSONObject("InlineBanner")
            inLineBannerEnabled = inlineBanner.getBoolean("enabled")
            Admob_Inline_Banner = inlineBanner.getString("ad_unit_id")
            InlineBannerN = inlineBanner.getBoolean("InlineBannerN")

            val nativeAd = jsonObject.getJSONObject("Native")
            nativeEnabled = nativeAd.getBoolean("enabled")
            native_exit_enabled = nativeAd.getBoolean("native_exit_enabled")
            Admob_Native = nativeAd.getString("ad_unit_id")
            templateStr = nativeAd.optString("onb_template_native_template", "medium").uppercase()

            val bannerRet = jsonObject.getJSONObject("RectBanner")
            Admob_RecBanner = bannerRet.getString("ad_unit_id")
            Admob_RecNew = bannerRet.getString("ad_unit_id_new")
            bannerRecEnabled = bannerRet.getBoolean("enabled")
            sevenRecenabled = bannerRet.getBoolean("seven_rec_enabled")
            nearRecEnabled = bannerRet.getBoolean("near_rec_enabled")


            val lanInter = jsonObject.getJSONObject("Lang_interstitial")
            lan_inter_enable = lanInter.getBoolean("enabled")
            Admob_Lang_Inter = lanInter.getString("ad_unit_id")


            // Logging values
            if (BuildConfig.DEBUG) {
                Log.d("TEST TAG", "new ui  - Enabled: $newUi")
                Log.d("TEST TAG", "Enabled: $isEnabled")
                Log.d("TEST TAG", "App Open - Enabled: $appOpenEnabled")
                Log.d("TEST TAG", "appOpen_resume_enabledn - Enabled: $appOpen_resume_enabled")
                Log.d(
                    "TEST TAG",
                    "Interstitial - Enabled: $interstitialEnabled, Counter: $interstitialCounter , First Counter: $firstinterCounter , bot inter :$botInter"
                )
                Log.d(
                    "TEST TAG",
                    "Banner - Enabled: $bannerEnabled, Adaptive: $bannerAdaptive, Collapsible: $bannerCollapsible"
                )
                Log.d(
                    "TEST TAG",
                    "InlineBanner - inLineBannerEnabled: $inLineBannerEnabled, Admob_Inline_Banner: $Admob_Inline_Banner, InlineBannerN: $InlineBannerN"
                )
                Log.d("TEST TAG", "Native - Enabled: $nativeEnabled")
                Log.d("TEST TAG", "Banner Ret language - Enabled: $bannerRecEnabled")
                Log.d("TEST TAG", "Banner Ret seven - Enabled: $sevenRecenabled")
                Log.d("TEST TAG", "Banner Ret near - Enabled: $nearRecEnabled")
            }


        } catch (e: Exception) {
            Log.e("TEST TAG", "Error parsing JSON: ${e.message}")
        }
    }

    private fun setupConfigUpdateListener() {
        val keysToWatch = setOf("ad_config", "ai_config", "new_ui", "weather_inter")
        remoteConfigManager.addConfigUpdateListener(keysToWatch = keysToWatch,
            onUpdate = { updatedKeys ->
                when {
                    updatedKeys.contains("ad_config") -> {
                        applyRemoteConfigValues()
                    }

                    updatedKeys.contains("ai_config") -> {
                        applyRemoteConfigValues()
                    }

                    updatedKeys.contains("new_ui") -> {
                        applyRemoteConfigValues()
                    }

                    updatedKeys.contains("weather_inter") -> {
                        applyRemoteConfigValues()
                    }
                }
            },
            onError = { error ->
                Log.w("MainActivity", "Config update error", error)

            })
    }

    companion object {
        var currentActivity: Activity? = null
        var weatherInter = false
        var botInter = false
        var satelliteReview = false
        var check1 = false
        var formattedTemp = 0
        var whiteregion = false
        var firstkey = true
        var isEnabled = true
        var appOpenEnabled = true
        var appOpen_resume_enabled = true
        var interstitialEnabled = true
        var interstitialMappilaryEnabled = true
        var interstitialCounter = 5
        var firstinterCounter = 2
        var bannerEnabled = true
        var exitInterEnabled = true
        var inLineBannerEnabled = true
        var InlineBannerN = true
        var bannerHomeEnabled = false
        var bannerAdaptive = false
        var bannerCollapsible = false
        var nativeEnabled = true
        var native_exit_enabled = true
        var templateStr = ""
        var bannerRecEnabled = true

        var lan_inter_enable = true
        var sevenRecenabled = true
        var nearRecEnabled = true
        var aicollapsable = false
        var version = "1.0"
        var adcounter = 5
        var baseUrl: String = "https://openrouter.ai/api/v1/"
        var apiKey: String =
            "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"
        var modelName1: String = "meta-llama/llama-3.1-70b-instruct"
        var modelName2: String = "openai/gpt-4o"
        var modelName3: String = "openchat/openchat-7b:free"
        var selected_model = 1
        var newUi = true


        fun View.clickWithDebounce(debounceTime: Long = 2000L, action: () -> Unit) {
            this.setOnClickListener(object : View.OnClickListener {
                private var lastClickTime: Long = 0

                override fun onClick(v: View) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                    else action()

                    lastClickTime = SystemClock.elapsedRealtime()
                }
            })
        }

        fun View.clickWithDebounce2(debounceTime: Long = 500L, action: () -> Unit) {
            this.setOnClickListener(object : View.OnClickListener {
                private var lastClickTime: Long = 0

                override fun onClick(v: View) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                    else action()

                    lastClickTime = SystemClock.elapsedRealtime()
                }
            })
        }

        fun View.clickWithDebounce3(debounceTime: Long = 3000L, action: () -> Unit) {
            this.setOnClickListener(object : View.OnClickListener {
                private var lastClickTime: Long = 0

                override fun onClick(v: View) {
                    if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                    else action()

                    lastClickTime = SystemClock.elapsedRealtime()
                }
            })
        }

        fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ))
        }

    }


}