package com.example.gpstest.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityCompassBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.comapss
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView
import kotlin.math.abs
import kotlin.math.roundToInt

class CompassActivity : BaseActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
     lateinit var binding: ActivityCompassBinding
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private lateinit var locationViewModel: LocationViewModel
    private lateinit  var currentLocation: CurrentLocation
    private var azimuth: Float = 0f
    var navigatingFromcompass=false
    private lateinit var locationHelper: LocationPermissionHelper
    private var adView: AdView? = null
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCompassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseCustomEvents(this).createFirebaseEvents(comapss, "true")
        InfoUtil(this).setSystemBarsColor(R.attr.primarycolor)
        locationHelper = LocationPermissionHelper(this)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        currentLocation= CurrentLocation(this,this,locationViewModel,0)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        checkPermissions()
        setupCardClickListeners()
        if (isCompassAvailable(this)) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        } else {
            Toast.makeText(this, "Compass not supported on this device", Toast.LENGTH_SHORT).show()
        }
        binding.icBack.clickWithDebounce{
            finish()
        }
binding.grantpermission.clickWithDebounce{
    locationHelper.requestLocationPermission(this)
}

    }
    private fun checkPermissions() {
        when {
            !locationHelper.isLocationPermissionGranted() -> {
                binding.permissionLayout.visibility = View.VISIBLE
                binding.groupcontent.visibility = View.GONE
            }
            else -> {
                updateui()
                binding.permissionLayout.visibility = View.GONE
                binding.groupcontent.visibility = View.VISIBLE

            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateui()
            }
            else {

                locationHelper.openAppSettings(this)
            }
        }
    }
    private fun updateui()
    {
        if (Prefutils(this@CompassActivity).getBool("is_premium", false)||!bannerEnabled || !isEnabled) {

            binding.adLayout.visibility=View.GONE

        } else {
            binding.adLayout.visibility=View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadBannerAd(binding.adLayout,
                    this@CompassActivity,
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            Log.e("TESTTAG", "onAdFailed: Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TESTTAG", "onAdLoaded: Banner")
                        }
                    })?.let { adView = it }
            }
        }
        currentLocation.getCurrentLocation()
        Handler(Looper.getMainLooper()).postDelayed({
            binding.textView8.text= longitude.toString()
            binding.textView9.text= latitude.toString()
           // bindind.address.text=fullAddress
        },2000)
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            }
        }

        val rotationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            azimuth = (azimuth + 360) % 360

            updateCompassUI(azimuth)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
    override fun onResume() {
        super.onResume()
        navigatingFromcompass=false
        checkPermissions()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }
    private var lastAzimuth: Float = 0f
    private val azimuthThreshold = 3f // Only update if change > 2 degrees
    @SuppressLint("SetTextI18n")
    private fun updateCompassUI(azimuth: Float) {
        // Only update if the azimuth change exceeds the threshold
        if (abs(azimuth - lastAzimuth) > azimuthThreshold) {
            lastAzimuth = azimuth

            // Smooth rotation using ObjectAnimator
            ObjectAnimator.ofFloat(binding.compass, "rotation", -azimuth).apply {
                duration=500
                interpolator = LinearInterpolator()
                start()
            }

            val direction = when (azimuth.roundToInt()) {
                in 0..22 -> "N"
                in 23..67 -> "NE"
                in 68..112 -> "E"
                in 113..157 -> "SE"
                in 158..202 -> "S"
                in 203..247 -> "SW"
                in 248..292 -> "W"
                in 293..337 -> "NW"
                else -> "N"
            }

            binding.directionText.text = "${azimuth.toInt()}° $direction"
        }
    }
    override fun onPause() {
        super.onPause()
        if(!navigatingFromcompass)
        {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.compass_disappear, "true")
        }
        sensorManager.unregisterListener(this)
    }
    private fun isCompassAvailable(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        return compassSensor != null
    }
    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupCardClickListeners() {
        val defaultStrokeColor = R.drawable.unselcardbg
        val selectedStrokeColor = R.drawable.cardbg2
        fun selectCard(selectedCard: ConstraintLayout) {
            binding.card.setBackgroundResource(if (selectedCard == binding.card) selectedStrokeColor else defaultStrokeColor)
            binding.card2.setBackgroundResource(if (selectedCard == binding.card2) selectedStrokeColor else defaultStrokeColor)
            binding.card3.setBackgroundResource( if (selectedCard == binding.card3) selectedStrokeColor else defaultStrokeColor)

        }

        fun setSelectedImage(imageView: ImageView) {
            val fadeOut = ObjectAnimator.ofFloat(binding.compass, "alpha", 1f, 0f)
            fadeOut.duration = 250
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.compass.setImageDrawable(imageView.drawable)

                    val fadeIn = ObjectAnimator.ofFloat(binding.compass, "alpha", 0f, 1f)
                    fadeIn.duration = 250

                    val scaleX = ObjectAnimator.ofFloat(binding.compass, "scaleX", 0.8f, 1f)
                    val scaleY = ObjectAnimator.ofFloat(binding.compass, "scaleY", 0.8f, 1f)
                    scaleX.duration = 300
                    scaleY.duration = 300

                    AnimatorSet().apply {
                        playTogether(fadeIn, scaleX, scaleY)
                        start()
                    }
                }
            })
            fadeOut.start()
        }
        binding.card.setOnClickListener {
            binding.card.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.card.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                selectCard(binding.card)
                setSelectedImage(binding.imageSlider)
            },200)

        }
        binding.card2.setOnClickListener {
            binding.card2.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.card2.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                selectCard(binding.card2)
                setSelectedImage(binding.imageSlider2)
            },200)
        }
        binding.card3.setOnClickListener {
            binding.card3.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.card3.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                selectCard(binding.card3)
                setSelectedImage(binding.imageSlider3)
            },200)
        }

    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromcompass = true
    }
}