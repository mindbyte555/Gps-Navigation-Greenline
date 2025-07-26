package com.example.gpstest.firebase


import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseCustomEvents(val context: Context) {

    var FirebBaseEvent: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun createFirebaseEvents(key: String, value: String) {
        val params = Bundle()
        params.putString(key, value)
        FirebBaseEvent.logEvent(key+"21", params)

        Log.e("TAG", "createFirebaseEvents: $key and $value")
    }


}