package com.example.gpstest.AdsManager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.gpstest.AdsManager.AdsManager.Companion.ad
import com.example.gpstest.AdsManager.AdsManager.Companion.counter
import com.example.gpstest.MyApp.Companion.adcounter
import com.example.gpstest.MyApp.Companion.firstkey
import com.example.gpstest.MyApp.Companion.interstitialCounter
import com.example.gpstest.MyApp.Companion.interstitialEnabled
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.lan_inter_enable
import com.example.gpstest.R
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents.Companion.inter_langload_request_sent
import com.example.gpstest.firebase.customevents.Companion.inter_normalload_request_sent
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


@SuppressLint("StaticFieldLeak")
object InterstitialClass {
    var interstitialAd: InterstitialAd? = null
    var mContext: Context? = null
    var mActivity: Activity? = null
    var mInterstitialID: String? = null
    var logTag: String = "TEST TAG"
    var mActionOnAdClosedListener: ActionOnAdClosedListener? = null
    var isAdDecided: Boolean = false
    var DELAY_TIME: Int = 0
    var isInterstitalIsShowing: Boolean = false
    var isProcessing: Boolean = false
    var stopInterstitial: Boolean = false
    var timerCalled: Boolean = false
    var progressDialog: ProgressDialog? = null
    var dialog: Dialog? = null
    var loader: LottieAnimationView? = null

    fun requestInterstitial(
        context: Context?,
        activity: Activity?,
        showSuccessEvent: String = null.toString(),
        actionOnAdClosedListenersm: ActionOnAdClosedListener?,
    ) {
//        if (!PrefUtil(context).getBool("playstore")) return
        if (Prefutils(context!!).getBool("is_premium") || !isEnabled || !interstitialEnabled) {
            mActionOnAdClosedListener = actionOnAdClosedListenersm
            performAction()
            return
        }
        if (counter >= adcounter) {
            mActionOnAdClosedListener = actionOnAdClosedListenersm
            performAction()
            return
        }
        if (Prefutils(context).getBool("isFirstTime", true) && !lan_inter_enable) {
            mActionOnAdClosedListener = actionOnAdClosedListenersm
            performAction()
            return
        }

//        if (showSuccessEvent =="language"&& !lan_inter_enable)
//        {
//            mActionOnAdClosedListener = actionOnAdClosedListenersm
//            performAction()
//            return
//        }
        if (isProcessing) {
            Log.e(logTag, "request_interstitial: Dialog is showing..")
            return
        }
        isProcessing = true
        mContext = context
        mActivity = activity
        if (Prefutils(context).getBool("isFirstTime", true)) {
            mInterstitialID = AdIds.admob_Lang_Interstitial()
            FirebaseCustomEvents(context).createFirebaseEvents(inter_langload_request_sent, "true")
            Log.d(logTag, " dialog loadInterstitial: 1st $mInterstitialID")

        } else {
            mInterstitialID = AdIds.AdmobInterstitialId()
            FirebaseCustomEvents(context).createFirebaseEvents(
                inter_normalload_request_sent,
                "true"
            )
            Log.d(logTag, "dialog loadInterstitial: 2nd $mInterstitialID")

        }
        if (showSuccessEvent == "weather") {
            mInterstitialID = AdIds.admob_Lang_Interstitial()
        }
        mActionOnAdClosedListener = actionOnAdClosedListenersm
        isAdDecided = false

        if (AdTimerClass.isEligibleForAd) {
            if (Prefutils(context).getBool("is_premium") || !interstitialEnabled || !isEnabled || !isInternetAvailable(
                    mContext!!
                )
            ) {
                mActionOnAdClosedListener = actionOnAdClosedListenersm
                performAction()
            } else {
                loadInterstitialruntime(context, showSuccessEvent)
            }
        } else {
            performAction()
        }
    }

    fun loadInterstitialruntime(
        context: Context,
        showSuccessEvent: String = null.toString()
    ) {
        if (interstitialAd == null) {
            Log.e(logTag, "Main Insterstitial Request Send.")
            mContext?.let {

            }
            showAdDialog()
            stopInterstitial = false
            timerCalled = false
            val adRequest_interstitial = AdRequest.Builder().build()
            InterstitialAd.load(
                mContext!!,
                mInterstitialID!!,
                adRequest_interstitial,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        counter++
                        interstitialAd = interAd
                        isAdDecided = true
                        if (!timerCalled) {
                            closeAdDialog()
                            show_interstitial(context, showSuccessEvent)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isAdDecided = true
                        if (!timerCalled) {
                            closeAdDialog()
                            performAction()
                        }
                    }
                })
            timerAdDecided(context, showSuccessEvent)
        } else {
            Log.e(logTag, " Main Ad was already loaded.: ")
            stopInterstitial = false
            showAdDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                closeAdDialog()
                show_interstitial(context, showSuccessEvent)
            }, 2000)
        }
    }

    fun timerAdDecided(
        context: Context,
        showSuccessEvent: String = null.toString(),
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdDecided) {
                stopInterstitial = true
                timerCalled = true
                Log.e(logTag, "Handler Cancel.")
                AdTimerClass.cancelTimer()
                closeAdDialog()
                show_interstitial(context, showSuccessEvent)
            }
        }, 8000)
    }

    @SuppressLint("SetTextI18n")
    fun showAdDialog() {
        if (mActivity != null && !mActivity!!.isFinishing) {
            isInterstitalIsShowing = true
            dialog = Dialog(mActivity!!)
            dialog?.setContentView(R.layout.dialog_custom)
            dialog?.setCancelable(false)
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            // val tvTitle = dialog?.findViewById<TextView>(R.id.tvTitle)
            // val tvMessage = dialog?.findViewById<TextView>(R.id.tvMessage)
            // tvTitle?.text = "Please Wait."
            // tvMessage?.text = "Full Screen Ad is expected to Show."
            try {
                dialog?.show()
            } catch (e: Exception) {
                Log.e("AdDialog", "Error showing custom dialog: ${e.message}")
            }
            dialog = dialog
        }
    }

    fun closeAdDialog() {
        isInterstitalIsShowing = false
        try {
            if (mActivity != null && !mActivity!!.isFinishing) {
                if (dialog != null && dialog!!.isShowing) {
                    loader?.pauseAnimation()
                    dialog!!.dismiss()
                }
//                    if (progressDialog != null && progressDialog!!.isShowing) {
//                        progressDialog!!.dismiss()
//                    }
            }
        } catch (e: Exception) {
            Log.e(logTag, "closeAdDialog: Exception")
        }
    }


    fun show_interstitial(
        context: Context,
        showSuccessEvent: String = null.toString(),
    ) {
        if (interstitialAd != null && stopInterstitial == false) {
            isInterstitalIsShowing = true
            interstitialAd!!.show(mActivity!!)
            interstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {

                    super.onAdFailedToShowFullScreenContent(adError)
                    Log.e(logTag, "Main Insterstitial Failed to Show ${adError.message}")
                    interstitialAd = null
                    isInterstitalIsShowing = false
                    performAction()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    Log.e(logTag, "Main Insterstitial Shown.")
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    Log.e(logTag, "Main Insterstitial Dismissed.")
                    interstitialAd = null
                    isInterstitalIsShowing = false
//                  if (!firstkey || showSuccessEvent == "routeFinder_activity" )
                    if (showSuccessEvent != "language") AdsManager.loadInterstitial(context)
                    performAction()
                }
            }
        } else {
            performAction()
        }
    }

    fun showAvailableInterstitial(
        activity: Activity,
        re: Boolean = false,
        onAdDismissed: () -> Unit // Callback when the ad is dismissed
    ) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.e("test tag", "Ad Dismissed")
                interstitialAd = null // reset it
//               if (!firstkey||re)
                AdsManager.loadInterstitial(activity)
                onAdDismissed() // Invoke the callback after ad is dismissed
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("Interstitial", "Ad Failed to Show: ${adError.message}")
                interstitialAd = null // reset it
                onAdDismissed() // Invoke the callback in case of failure to show
            }

            override fun onAdShowedFullScreenContent() {
                Log.e("TEST TAG", "Ad Shown preload dialog")
            }
        }

        interstitialAd?.show(activity)
    }


    fun performAction() {
        Log.e(logTag, "performAction: Moving next")
        isInterstitalIsShowing = false
        mActionOnAdClosedListener?.ActionAfterAd()
        Handler(Looper.getMainLooper()).postDelayed({ isProcessing = false }, 1000)
    }
}

