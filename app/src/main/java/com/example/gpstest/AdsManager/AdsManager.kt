package com.example.gpstest.AdsManager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.NonNull
import com.example.gpstest.BuildConfig
import com.example.gpstest.MyApp.Companion.adcounter
import com.example.gpstest.MyApp.Companion.aicollapsable
import com.example.gpstest.MyApp.Companion.bannerCollapsible
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.bannerHomeEnabled
import com.example.gpstest.MyApp.Companion.interstitialEnabled
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.nativeEnabled
import com.example.gpstest.R
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents.Companion.inter_load_failed
import com.example.gpstest.firebase.customevents.Companion.inter_load_success
import com.example.gpstest.firebase.customevents.Companion.inter_normalload_request_sent
import com.example.gpstest.utls.Prefutils
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import java.lang.ref.WeakReference

class AdsManager(val context: Context) {
    companion object {
        private var adView: AdView? = null
        private val TAG = "AdsManager"
        var native_id = ""
        var id = ""
        var cachedNativeAd: NativeAd? = null
        var ad = ""
        private var retryCount = 0
        fun getAdSize(ad_view_container: RelativeLayout, context: Context): AdSize {
            val adSize: AdSize
            val display = (context as Activity).windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density
            var adWidthPixels = ad_view_container.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

        fun getAdSize1(adLayout: ViewGroup, context: Context): AdSize? {
            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density

            var adWidthPixels = adLayout.width.toFloat()
            if (adWidthPixels <= 0f) {
                adWidthPixels = displayMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            if (adWidth <= 0) return null

            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }


        private fun getInlineAdaptiveAdSize(context: Context, adLayout: RelativeLayout): AdSize {
            val outMetrics = context.resources.displayMetrics
            val widthPixels = adLayout.width.takeIf { it > 0 } ?: outMetrics.widthPixels
            val density = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
//            return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth)
            return AdSize.getInlineAdaptiveBannerAdSize(adWidth, 120)
        }

        fun showInlineBannerAd(
            adLayout: RelativeLayout?, context: Context, id: String, listener: AdmobBannerAdListener
        ): AdView? {
            if (Prefutils(context).getBool("is_premium", false)) {
                Log.e(TAG, "showBannerAd not showed to premium users")
                adLayout?.visibility = View.GONE
                return null
            }
            if (adLayout == null) return null

            adLayout.removeAllViews()
            adLayout.visibility = View.VISIBLE

            val adRequest = AdRequest.Builder().build()
            val adView = AdView(context)

            adView.adUnitId = id
            // Use adaptive size
            val adSize = getInlineAdaptiveAdSize(context, adLayout)
            adView.setAdSize(adSize)

            adLayout.addView(adView)
            adView.loadAd(adRequest)

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.e("TESTTAG", "Inline Adaptive Banner Ad Loaded")
                    listener.onAdLoaded()
                    adLayout.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.e("TESTTAG", "Inline Adaptive Banner Ad Failed To Load: ${p0.message}")
                    super.onAdFailedToLoad(p0)
                    listener.onAdFailed()
                }
            }

            return adView
        }

        fun loadRecBannerAd(
            adLayout: RelativeLayout?,
            adid: String,
            context: Context,
            listener: AdmobBannerAdListener
        ): AdView? {
//        if (!PrefUtil(context).getBool("Show_ad", false)) {
//            return null
//        }

            if (Prefutils(context).getBool(
                    "is_premium",
                    false
                ) || !isEnabled
            ) {
                return null
            }

            adLayout?.removeAllViews()
            adLayout?.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            adView = context?.let { AdView(it) }
            adView!!.adUnitId = adid

            adView!!.setAdSize(AdSize.MEDIUM_RECTANGLE)
            adLayout?.addView(adView)
            adView!!.loadAd(adRequest)
            adView!!.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adLayout?.visibility = View.VISIBLE
                    listener.onAdLoaded()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    listener.onAdFailed()
                }
            }
            return adView
        }

        fun loadBannerAd(
            adLayout: RelativeLayout?,
            context: Context,
            listener: AdmobBannerAdListener,
        ): AdView? {
            if (Prefutils(context).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
                return null
            }
            adLayout?.removeAllViews()
            adLayout?.visibility = View.VISIBLE
            val adView = AdView(context)
            val adRequestBuilder = AdRequest.Builder()
            if (bannerCollapsible) {
                adView.adUnitId = AdIds.AdmobCollaspeBannerId()
                adView.setAdSize(getAdSize(adLayout!!, context))
                val extras = Bundle()
                extras.putString("collapsible", "top")
                adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)

                Log.d("AdLoader", "Loading Collapsible Banner Ad")
            } else {
                adView.adUnitId = AdIds.AdmobAdaptiveBannerId()
                adView.setAdSize(getAdaptiveBannerAdSize(context, adLayout!!))

                Log.d("AdLoader", "Loading Adaptive Banner Ad")
            }

            val adRequest = adRequestBuilder.build()
            adLayout.addView(adView)
            adView.loadAd(adRequest)

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adLayout.visibility = View.VISIBLE
                    Log.d("AdLoader", "Ad Loaded Successfully")
                    listener.onAdLoaded()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.e("AdLoader", "Ad Failed to Load: ${p0.message}")
                    listener.onAdFailed()
                }
            }
            return adView
        }

        fun loadSplashBanner(
            adLayout: RelativeLayout?,
            context: Context,
            listener: AdmobBannerAdListener
        ): AdView? {
            if (Prefutils(context).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
                return null
            }

            adLayout?.removeAllViews()
            adLayout?.visibility = View.VISIBLE

            val adView = AdView(context).apply {
                adUnitId = AdIds.AdmobSplashBannerId()
                setAdSize(getAdaptiveBannerAdSize(context, adLayout!!))
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "Ad Loaded Successfully")
                        listener.onAdLoaded()
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e(TAG, "Ad Failed to Load: ${p0.message}")
                        listener.onAdFailed()
                    }
                }
            }

            try {
                adLayout?.addView(adView)
                adView.loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading banner: ${e.message}")
                listener.onAdFailed()
                return null
            }

            return adView
        }


        fun loadAiBannerAd(
            adLayout: RelativeLayout?,
            context: Context,
            listener: AdmobBannerAdListener,
        ): AdView? {
            if (Prefutils(context).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
                return null
            }

            adLayout?.removeAllViews()
            adLayout?.visibility = View.VISIBLE

            val adView = AdView(context)

            val adRequestBuilder = AdRequest.Builder()
            if (aicollapsable) {
                adView.adUnitId = AdIds.AdmobAiCollaspeBannerId()
                adView.setAdSize(getAdSize(adLayout!!, context))

                val extras = Bundle()
                extras.putString("collapsible", "top")
                adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)

                Log.d(TAG, "Loading Collapsible Banner Ad")
            } else {
                adView.adUnitId = AdIds.AdmobAiCollaspeBannerId()
                adView.setAdSize(getAdaptiveBannerAdSize(context, adLayout!!))

                Log.d(TAG, "Loading Adaptive Banner Ad")
            }

            val adRequest = adRequestBuilder.build()
            adLayout.addView(adView)
            adView.loadAd(adRequest)

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adLayout.visibility = View.VISIBLE
                    Log.d(TAG, "Ad Loaded Successfully")
                    listener.onAdLoaded()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.e(TAG, "Ad Failed to Load: ${p0.message}")
                    listener.onAdFailed()
                }
            }

            return adView
        }

        fun loadHomeBannerAd(
            adLayout: RelativeLayout?,
            context: Context,
            source: String? = null,
            listener: AdmobBannerAdListener,
        ): AdView? {
            if (Prefutils(context).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
                return null
            }

            adLayout?.removeAllViews()
            adLayout?.visibility = View.VISIBLE

            val adView = AdView(context)

            val adRequestBuilder = AdRequest.Builder()
            Log.e(TAG, "loadHomeBannerAd bannerHomeEnabled: $bannerHomeEnabled")
            if (bannerCollapsible) {
                adView.adUnitId = AdIds.AdmobCollaspeBannerId()
                adView.setAdSize(getAdSize(adLayout!!, context))
                Log.e("AdLoader", "loadHomeBannerAd source: $source")
                if (source == "weather" || source == "search") {
                    val extras = Bundle()
                    extras.putString("collapsible", "bottom")
                    adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)

                    Log.d("AdLoader", "Loading Collapsible Banner Ad for source1:$source ")
                } else {
                    val extras = Bundle()
                    extras.putString("collapsible", "top")
                    adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)

                    Log.d("AdLoader", "Loading Collapsible Banner Ad for source2:$source ")
                }

            } else {
                Log.e(TAG, "loadHomeBannerAd: source :$source")
                if (source == "splash" || source == "home") {
                    adView.adUnitId = AdIds.AdmobInlineBannerId()
                } else
                    adView.adUnitId = AdIds.AdmobAdaptiveBannerId()

                adView.setAdSize(getAdaptiveBannerAdSize(context, adLayout!!))

                Log.d("AdLoader", "Loading Adaptive Banner Ad")
            }

            val adRequest = adRequestBuilder.build()
            adLayout.addView(adView)
            adView.loadAd(adRequest)

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adLayout.visibility = View.VISIBLE
                    Log.d("AdLoader", "Ad Loaded Successfully")
                    listener.onAdLoaded()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.e("AdLoader", "Ad Failed to Load: ${p0.message}")
                    listener.onAdFailed()
                }
            }
            return adView
        }

        private fun getAdaptiveBannerAdSize(context: Context, adLayout: RelativeLayout): AdSize {
            // Get the current width of the ad container layout
            val display =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val adWidthPixels = adLayout.width.toFloat().takeIf { it > 0 }
                ?: outMetrics.widthPixels.toFloat()
            val adWidthDp = (adWidthPixels / outMetrics.density).toInt()

            // Return an adaptive ad size based on the width
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
        }

        fun loadInterstitial(context: Context) {
            if (Prefutils(context).getBool(
                    "is_premium",
                    false
                ) || (!interstitialEnabled) || !isEnabled
            ) {
                return
            }
            if (counter >= adcounter) {
                Log.e(TAG, "loadInterstitial counter2: $counter")
                return
            }
            if (mInterstitialAd != null) {
                return
            }

            if (!verifyAppFunction(context)) {
                return
            }
//             if (Prefutils(context).getBool("isFirstTime",true)) {
//
//                 ad = AdIds.admob_Lang_Interstitial()
//                 FirebaseCustomEvents(context).createFirebaseEvents(inter_langload_request_sent, "true")
//                 Log.d(TAG, "loadInterstitial: 1st $ad")
            //   } else {
            ad = AdIds.AdmobInterstitialId()
            FirebaseCustomEvents(context).createFirebaseEvents(
                inter_normalload_request_sent,
                "true"
            )
            Log.d(TAG, "loadInterstitial: 2nd $ad")

            //  }
            InterstitialAd.load(context,
                ad,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        FirebaseCustomEvents(context).createFirebaseEvents(
                            inter_load_success,
                            "true"
                        )
                        mInterstitialAd = ad
                        //      counter++
                        //      retryCount=0
                        Log.e(TAG, "AdManager Interstitial Ad Loaded")
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.e(TAG, "AdManager Interstitial Ad Loaded failed")

                        FirebaseCustomEvents(context).createFirebaseEvents(
                            inter_load_failed,
                            "true"
                        )
                        mInterstitialAd = null
//                        if (retryCount < 1 ) {
//                            retryCount++
//                            Handler(Looper.getMainLooper()).postDelayed({
//                                loadInterstitial(context)
//                            }, 4000)
//                        }
                    }
                })
        }

        fun showInterstitial(
            re: Boolean = false,
            activity: Activity,
            listener: InterstitialAdListener,
            AdSourse: String
        ) {
            reload = re
            if (mInterstitialAd != null) {
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        counter++
                        Log.e(TAG, "AdManager Interstitial Ad On Dismissed Full Screen Content")
                        mInterstitialAd = null
                        // if (!firstkey|| reload)
                        if (reload) loadInterstitial(activity)
                        listener.onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        FirebaseCustomEvents(activity).createFirebaseEvents(
                            "${AdSourse}_Ad_failed_show",
                            "true"
                        )
                        listener.onAdClosed()
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.e("TEST TAG", "Ad Shown preload")
                        FirebaseCustomEvents(activity).createFirebaseEvents(
                            "${AdSourse}_Ad_Showed_sucess",
                            "true"
                        )
                    }
                }
                mInterstitialAd?.show(activity)
            } else {
                listener.onAdClosed()
            }
        }


        @SuppressLint("ResourceType")
        fun loadNative1(
            container: RelativeLayout,
            activity: Activity,
            listener: AdmobBannerAdListener,
            type: NativeAdType
        ) {
            if (Prefutils(activity).getBool(
                    "is_premium",
                    false
                ) || (!nativeEnabled) || !isEnabled
            ) {
                return
            }
            Log.e("TESTTAG", "Native Ad Load Caled")
            var builder: AdLoader.Builder
            var id = AdIds.AdmobNativeId()
            builder = AdLoader.Builder(activity, id)

            //      val layoutId2 = R.layout.native_ad_medium
            val layoutId =
                if (type === NativeAdType.MEDIUM) R.layout.native_ad_medium else if (type === NativeAdType.SMALL) R.layout.native_ad_small else R.layout.native_ad_smallbanner
            val view: View = activity.layoutInflater.inflate(layoutId, null)
            val templateView: TemplateView = view.findViewById(R.id.templateView)
            builder.forNativeAd { nativeAd: NativeAd ->
                if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    nativeAd.destroy()
                    return@forNativeAd
                }
                templateView.setNativeAd(nativeAd)
                container.removeAllViews()
                container.addView(view)
            }
            val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
            val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
            builder.withNativeAdOptions(adOptions)
            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    listener.onAdFailed()
                    Log.e("TESTTAG", "Native Ad failed to load. ${loadAdError}")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    listener.onAdLoaded()
                    Log.e("TESTTAG", "Native Ad Loaded")
                }
            }).build()
            adLoader.loadAd(AdRequest.Builder().build())
        }

        fun loadNative(
            container: RelativeLayout,
            activity: Activity,
            listener: AdmobBannerAdListener,
            type: NativeAdType
        ) {
            if (Prefutils(activity).getBool("is_premium", false) || !nativeEnabled || !isEnabled) {
                return
            }
            Log.e("TESTTAG", "Native Ad Load Called with :$type")

            val layoutId = when (type) {
                NativeAdType.MEDIUM -> R.layout.native_ad_medium
                NativeAdType.SMALL -> R.layout.native_ad_small
                NativeAdType.SMALLBANNER -> R.layout.native_ad_smallbanner
            }

            // Optional: Set container height dynamically
            val height = when (type) {
                NativeAdType.MEDIUM -> activity.resources.getDimensionPixelSize(R.dimen._220sdp)
                NativeAdType.SMALL -> activity.resources.getDimensionPixelSize(R.dimen._100sdp)
                NativeAdType.SMALLBANNER -> activity.resources.getDimensionPixelSize(R.dimen._80sdp)
            }
            val layoutParams = container.layoutParams
            layoutParams.height = height
            container.layoutParams = layoutParams

            val view: View = activity.layoutInflater.inflate(layoutId, null)
            val templateView: TemplateView = view.findViewById(R.id.templateView)

            val builder = AdLoader.Builder(activity, AdIds.AdmobNativeId())
                .forNativeAd { nativeAd ->
                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        nativeAd.destroy()
                        return@forNativeAd
                    }
                    templateView.setNativeAd(nativeAd)
                    container.removeAllViews()
                    container.addView(view)
                }
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                        .build()
                )
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        listener.onAdFailed()
                        Log.e("TESTTAG", "Native Ad failed to load. $loadAdError")
                    }

                    override fun onAdLoaded() {
                        listener.onAdLoaded()
                        Log.e("TESTTAG", "Native Ad Loaded")
                    }
                })
                .build()

            builder.loadAd(AdRequest.Builder().build())
        }

        var counter = 0
        var reload = false
        var mInterstitialAd: InterstitialAd? = null
        private var adsManagerInstance: AdsManager? = null

        private fun verifyAppFunction(context: Context): Boolean {
            return try {
                if (BuildConfig.DEBUG) {
                    true
                } else {
                    val validInstallers: List<String> =
                        ArrayList(listOf("com.android.vending", "com.google.android.feedback"))
                    val installer =
                        context.packageManager.getInstallerPackageName(context.packageName)
                    installer != null && validInstallers.contains(installer)
                }
            } catch (e: Exception) {
                false
            }
        }

    }

    interface InterstitialAdListener {
        fun onAdClosed()
    }

    val instance: AdsManager
        get() {
            if (adsManagerInstance == null) {
                context?.let {
                    adsManagerInstance = AdsManager(it)
                }
            }
            return adsManagerInstance!!
        }

    init {
        if (context == null) {
//            mContext = context
            Handler(Looper.getMainLooper()).post {
                MobileAds.initialize(context) { initializationStatus ->
                    val statusMap = initializationStatus.adapterStatusMap
                    for (adapterClass in statusMap.keys) {
                        val status = statusMap[adapterClass]
                    }
                }
            }
        }
    }

    public interface AdmobBannerAdListener {
        fun onAdLoaded()
        fun onAdFailed()
    }

    enum class NativeAdType {
        MEDIUM, SMALL, SMALLBANNER
    }


}