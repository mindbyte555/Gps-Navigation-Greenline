package com.example.gpstest.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.example.gpstest.CurrentLocation.Companion.cityCountry2
import com.example.gpstest.CurrentLocation.Companion.fullAddress
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.bannerEnabled
import com.example.gpstest.MyApp.Companion.formattedTemp
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityMyLocBinding
import com.example.gpstest.databinding.ActivityWeatherBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.myloc
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.showToast
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.example.gpstest.weather.WeatherData
import com.example.gpstest.weather.WeatherService
import com.example.gpstest.weather.WeatherViewModel
import com.example.gpstest.weather.WeatherViewModelFactory
import com.google.android.gms.ads.AdView
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.expressions.dsl.generated.color
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.location
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class weather : BaseActivity() {
    lateinit var binding: ActivityWeatherBinding
    private var loactionflag = false
    private lateinit var weatherViewModel: WeatherViewModel
    private val apiKey = "a6450a304b61929119a1967808ae10a0"
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var currentLocation: CurrentLocation
    private var adView: AdView? = null
    private lateinit var locationHelper: LocationPermissionHelper
    private var flag = false
    var formattedTime = ""
    private var navigatingFromweather = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        FirebaseCustomEvents(this).createFirebaseEvents("weather_launch", "true")
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
        val currentTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy | hh:mm a", Locale.getDefault())
        formattedTime = formatter.format(currentTime)
        checkPermissions()

        binding.icBack.setOnClickListener {
            finish()
        }


        binding.grantpermission.setOnClickListener {
            locationHelper.requestLocationPermission(this)
        }

        binding.enablegps.setOnClickListener {
            locationHelper.openLocationSettings(this)
        }
    }

    private fun load() {
        if (Prefutils(this@weather).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {

            binding.adLayout.visibility = View.GONE
        } else {
            binding.adLayout.visibility = View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadHomeBannerAd(binding.adLayout,
                    this@weather,"weather",
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
    }

    override fun onPause() {
        super.onPause()
        if (!navigatingFromweather) {
            FirebaseCustomEvents(this).createFirebaseEvents("weather_disappear", "true")
        }
    }

    private fun checkPermissions() {
        Handler(Looper.getMainLooper()).postDelayed({
            flag = true
        }, 3000)

        when {
            !locationHelper.isLocationPermissionGranted() -> {
                binding.permissionLayout.visibility = View.VISIBLE
                binding.settinglayout.visibility = View.GONE
                binding.groupcontent.visibility = View.GONE
                binding.icBack.setImageResource(R.drawable.iconback)
                binding.title.setTextColor(getResources().getColor(R.color.black))
                binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.white))


            }

            !locationHelper.isLocationEnabled() -> {
                binding.permissionLayout.visibility = View.GONE
                binding.settinglayout.visibility = View.VISIBLE
                binding.groupcontent.visibility = View.GONE
                binding.icBack.setImageResource(R.drawable.iconback)
                binding.title.setTextColor(getResources().getColor(R.color.black))
                binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            }

            else -> {
                handleLocationAccess()
                binding.permissionLayout.visibility = View.GONE
                binding.settinglayout.visibility = View.GONE
                binding.groupcontent.visibility = View.VISIBLE
                binding.icBack.setImageResource(R.drawable.iconbackwhite)
                binding.title.setTextColor(getResources().getColor(R.color.white))
                binding.main.setBackgroundResource(R.drawable.day_weather)
                load()

            }
        }
    }

    private fun handleLocationAccess() {

        currentLocation.getCurrentLocation()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isInternetAvailable(this@weather)) {
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

    private fun weatherdata() {
        binding.time.text = formattedTime
        Log.d("weaether city", "$city ")
        weatherViewModel.fetchWeather(city, apiKey)

        weatherViewModel.weatherData.observe(this) { weatherData ->
            updateUI(weatherData)
        }
        // Observe weather data
        // Observe errors
        weatherViewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Log.d("TAGE", "weatherdata: $errorMessage")
                Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show()
                loactionflag = false
                //  Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun convertUnixTime(time: Long): String {
        val date = Date(time * 1000) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date).lowercase() // returns like "06:45 am"
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(weatherData: WeatherData) {
        binding.textViewCountry.text = cityCountry2
        binding.progressbar.visibility = View.GONE
        Log.d("TAG", "updateUI: icon${weatherData.weather[0].icon}")

        formattedTemp = weatherData.main.temp.toInt()
        binding.textViewTemp.text = "$formattedTemp°C"
        binding.maxmin.text =
            "Max: ${weatherData.main.temp_max.toInt()}°C | Min: ${weatherData.main.temp_min.toInt()}°C"
        binding.descriptionTextView.text = weatherData.weather[0].description
        binding.tvSunriseValue.text = convertUnixTime(weatherData.sys.sunrise)
        binding.tvSunsetValue.text = convertUnixTime(weatherData.sys.sunset)
        binding.tvHumidityValue.text = "${weatherData.main.humidity}%"
        binding.tvPressureValue.text = "${weatherData.main.pressure} hPa"
        binding.tvWindValue.text = "${weatherData.wind.speed} m/s"
        binding.tvVisibilityValue.text = "${weatherData.visibility / 1000} km"
        var weatherIcon = 1
        when (weatherData.weather[0].icon) {
            "01d" -> {

                binding.main.setBackgroundResource(R.drawable.day_weather)
                weatherIcon = 1
            }

            "01n" -> {
                binding.main.setBackgroundResource(R.drawable.night_weather)
                weatherIcon = 2
            }

            "02d" -> {
                binding.main.setBackgroundResource(R.drawable.day_weather)
                weatherIcon = 1
            }

            "02n" -> {
                binding.main.setBackgroundResource(R.drawable.night_weather)
                weatherIcon = 2
            }

            "03d" -> {
                binding.main.setBackgroundResource(R.drawable.day_weather)
                weatherIcon = 3
            }

            "03n" -> {
                binding.main.setBackgroundResource(R.drawable.night_weather)
                weatherIcon = 3
            }

            "04d" -> {
                binding.main.setBackgroundResource(R.drawable.day_weather)
                weatherIcon = 3
            }

            "04n" -> {
                binding.main.setBackgroundResource(R.drawable.night_weather)
                weatherIcon = 3
            }

            "09d" -> {
                binding.main.setBackgroundResource(R.drawable.day_weather)
                weatherIcon = 3
            }

            "09n" -> {
                binding.main.setBackgroundResource(R.drawable.night_weather)
                weatherIcon = 3
            }

            "10d" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }

            "10n" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }

            "11d" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }

            "11n" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }

            "13d" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }

            "13n" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }

            "50d" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }

            "50n" -> {
                binding.main.setBackgroundResource(R.drawable.rain_weather)
                weatherIcon = 4
            }


        }
        when (weatherIcon) {
            1 -> binding.weatherImp.setAnimation("sun.json")

            2 -> binding.weatherImp.setAnimation("night.json")

            3 -> binding.weatherImp.setAnimation("cloud.json")

            4 -> binding.weatherImp.setAnimation("strom.json")
        }
        binding.weatherImp.playAnimation()
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
        }
    }

    override fun onResume() {
        super.onResume()
        navigatingFromweather = false
        if (Prefutils(this).getBool("is_premium", false) || !bannerEnabled || !isEnabled) {
            binding.adLayout.visibility = View.GONE
        }
        if (flag) {
            Log.d("TAG", "onResume: handle called")

            if (!loactionflag) {
                checkPermissions()
            }

        }


    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromweather = true
    }

}