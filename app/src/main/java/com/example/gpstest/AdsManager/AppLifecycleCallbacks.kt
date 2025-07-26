package com.example.gpstest.AdsManager

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.example.gpstest.AdsManager.AdIds.Companion.getAppOpenResumeId
import com.example.gpstest.MyApp.Companion.appOpen_resume_enabled
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AppOpenAdManager {
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var loadTime: Long = 0

    fun loadAd(context: Context) {
        Log.e("AppOpenAdResume", "onAppForegrounded:loadAd called")

        if (Prefutils(context).getBool(
                "is_premium",
                false
            ) || (!appOpen_resume_enabled) || !isEnabled
        ) {
            return
        }
        if (appOpenAd != null)
            return
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            getAppOpenResumeId(), // Replace with your Ad Unit ID
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = System.currentTimeMillis()
                    Log.e("AppOpenAdResume", "onAppForegrounded:onAdLoaded $appOpenAd ")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d("AppOpenAdResume", "Failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showAdIfAvailable2(context: Context) {
        Log.e("ForGround", "showAdIfAvailable called $appOpenAd: ")
        if (isShowingAd) return
        if (appOpenAd != null) {
            val activity = context as? Activity ?: return
            appOpenAd?.show(activity)
            isShowingAd = true
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowingAd = false
                    appOpenAd = null
                    loadAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    isShowingAd = false
                    appOpenAd = null
                    loadAd(context)
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    Log.e("ForGround", "onAdShowedFullScreenContent  ")

                }
            }
        } else {
            loadAd(context)
        }
    }

    fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd) {
            Log.e("AppOpenAdResume", "Ad already showing")
            return
        }

        if (appOpenAd == null) {
            Log.e("AppOpenAdResume", "Ad not loaded, loading new one...")
            loadAd(activity.applicationContext)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.e("AppOpenAdResume", "Ad dismissed")
                isShowingAd = false
                appOpenAd = null
                loadAd(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("AppOpenAdResume", "Ad failed to show: ${adError.message}")
                isShowingAd = false
                appOpenAd = null
                loadAd(activity.applicationContext)
            }

            override fun onAdShowedFullScreenContent() {
                Log.e("AppOpenAdResume", "Ad showed successfully")
                isShowingAd = true
            }
        }

        Log.e("AppOpenAdResume", "Attempting to show ad...")
        appOpenAd?.show(activity)
    }

//    private fun isAdAvailable(): Boolean {
//        return appOpenAd != null && (System.currentTimeMillis() - loadTime) < 4 * 60 * 60 * 1000 // 4 hours
//    }
}
