package com.example.gpstest.AdsManager

import android.util.Log
import java.util.Timer
import java.util.TimerTask


object AdTimerClass {
    var counter: Int = 0
    var isFirstTimeClicked: Boolean = true
    var myTimer: TimerTask? = null

    val isEligibleForAd: Boolean
        get() {
            if (isFirstTimeClicked) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        //your method
                        myTimer = this
                        if (counter >= 1) {
                            cancelTimer()
                        } else {
                            isFirstTimeClicked = false
                        }
                        counter++
                        Log.d("Ads_", ": Counter second: " + counter)
                    }
                }, 0, 1000)
            }

            if (isFirstTimeClicked) {
                return true
            }
            return false
        }

    fun cancelTimer() {
        counter = -1
        isFirstTimeClicked = true
        myTimer!!.cancel()
    }
}