package com.example.gpstest.utls

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import com.example.gpstest.R
import kotlin.math.sqrt


object Constants {
    var destinationName: String? = null
    fun getAnimationOptions(context: Context): ActivityOptionsCompat {
        val enterAnimation = R.anim.slide_in_right
        val exitAnimation = R.anim.slide_out_left
//        val enterAnimation = R.anim.slide_inright_2
//        val exitAnimation = R.anim.slideout_left2
        return ActivityOptionsCompat.makeCustomAnimation(context, enterAnimation, exitAnimation)
    }

    var phoneSize = 0.0f
    var textHome = ""
    var featureNameHome = ""
    var pointLngHome = 0.0
    var pointLatHome = 0.0

    var textWork = ""
    var featureNameWork = ""
    var pointLngWork = 0.0
    var pointLatWork = 0.0

    var name = ""

    var chooseImgID = ""
    var isChoose = false

    var isGPSEnable = false

    var giveItName = ""
    var packagePurchasedType = ""
    var isPicInPic = false
    var isPicinPicAllowed = true

    var distance = 0
    var distanceBus = 0
    var distanceBike = 0
    var distanceWalk = 0
    var time = Pair(0, 0)
    var timeBus = Pair(0, 0)
    var timeBike = Pair(0, 0)

    var timeWalk = Pair(0, 0)
    var permissinRational = 0
    var currentAddress = ""
    var myLocationAddress = ""
    var currentLat = 0.0
    var currentLong = 0.0
    fun getDistanceFromShared(context: Context): String {
        val sharedPreferencesDistanceType: SharedPreferences =
            context.getSharedPreferences("distance", Context.MODE_PRIVATE)
        val distanceType = sharedPreferencesDistanceType.getString("selectedOne", "Kilometers")

        return distanceType!!
    }


    fun getScreenSizeInInches(context: Context): Float {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        return sqrt(widthInches * widthInches + heightInches * heightInches)
    }

}