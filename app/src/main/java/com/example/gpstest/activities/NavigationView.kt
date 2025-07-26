package com.example.gpstest.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.CurrentLocation
import com.example.gpstest.CurrentLocation.Companion.city
import com.example.gpstest.CurrentLocation.Companion.fullAddress
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.LocationViewModel
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.clickWithDebounce3
import com.example.gpstest.MyApp.Companion.formattedTemp
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityNavigationViewBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.navigationCancel_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.navigation_btn_clicked
import com.example.gpstest.firebase.customevents.Companion.navigation_view
import com.example.gpstest.gps.DirectionRepository
import com.example.gpstest.gps.DirectionService
import com.example.gpstest.gps.Example
import com.example.gpstest.gps.MessageOnMap
import com.example.gpstest.nearby_places.getTurnDrawable
import com.example.gpstest.utls.Constants.distance
import com.example.gpstest.utls.Constants.distanceBike
import com.example.gpstest.utls.Constants.distanceWalk
import com.example.gpstest.utls.Constants.getDistanceFromShared
import com.example.gpstest.utls.Constants.time
import com.example.gpstest.utls.Constants.timeBike
import com.example.gpstest.utls.Constants.timeWalk
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.weather.WeatherService
import com.example.gpstest.weather.WeatherViewModel
import com.example.gpstest.weather.WeatherViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class NavigationView : BaseActivity(), TextToSpeech.OnInitListener {
    private var apiresponse: Example? = null
    private var isUpdating = false
    var startRoute = false
    private var d = 0
    var navigatingFromnavigation = false
    private var isLayersClicked = false
    private var kmphSpeed: Double = 0.0
    private var preVoice = -1
    private var distnaceToNextPoint = 0.0
    private var lineStringLngDes = 0.0
    private var lineStringLatDes = 0.0
    private var remainingDistance = 0.0
    private var lastLngDes = 0.0
    private var lastLatDes = 0.0
    private var currentSpeed = 0.0
    private var i = 0
    private var ttime = Pair(0, 0)
    private var kkilometers = 0
    private var lastLng = 0.0
    private var lastLat = 0.0
    private var lastPoint = false
    private lateinit var tts: TextToSpeech
    private var isSpeakerOn = true
    private lateinit var mapView: MapView
    private lateinit var currentLocation: CurrentLocation
    private var pointedAddress: String = ""
    private var pointedLatitude: Double = 0.0
    private var pointedLongitude: Double = 0.0
    private var isTracking = false
    private var lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC
    private lateinit var retrofit: Retrofit
    private lateinit var detailsList: MutableList<String>
    lateinit var binding: ActivityNavigationViewBinding
    private lateinit var locationViewModel: LocationViewModel
    private var mFeatureListWalk: ArrayList<Point> = ArrayList()
    private var mFeatureListDriving: ArrayList<Point> = ArrayList()
    private var mFeatureListBike: ArrayList<Point> = ArrayList()
    private lateinit var messageOnMap: MessageOnMap
    private lateinit var repository: DirectionRepository
    private lateinit var serviceDirectionApi: DirectionService
    private val mPointList: ArrayList<Point> = ArrayList()
    private val mManeuver: ArrayList<String> = ArrayList()
    private val mTurnList: ArrayList<String> = ArrayList()
    private val mFeatureList: ArrayList<Point> = ArrayList()
    private var mLoation: ArrayList<Point> = ArrayList()
    private val mBannerInstruction: ArrayList<String> = ArrayList()
    private var selectedLayer = Style.MAPBOX_STREETS
    private var distanceRemainBtwSteps = ArrayList<ArrayList<Double>>()
    private var announcementList = ArrayList<ArrayList<String>>()
    private var listOfArrayLists = ArrayList<ArrayList<Point>>()
    private lateinit var messageOnMapTime: MessageOnMap
    private lateinit var mapboxMap: MapboxMap
    private var mBannerInstructionBike: ArrayList<String> = ArrayList()
    private var mBannerInstructionWalk: ArrayList<String> = ArrayList()
    private var mPointListBike: ArrayList<Point> = ArrayList()
    private var mManeuverBike: ArrayList<String> = ArrayList()
    private var mTurnListBike: ArrayList<String> = ArrayList()
    private var mLoationBike: ArrayList<Point> = ArrayList()
    private var mLoationWalk: ArrayList<Point> = ArrayList()
    private var mTurnListWalk: ArrayList<String> = ArrayList()
    private var mPointListWalk: ArrayList<Point> = ArrayList()
    private var mManeuverWalk: ArrayList<String> = ArrayList()
    private var listOfArrayListsBike = ArrayList<ArrayList<Point>>()
    private var listOfArrayListsWalk = ArrayList<ArrayList<Point>>()
    private var mLoationDriving: ArrayList<Point> = ArrayList()
    private var mBannerInstructionDriving: ArrayList<String> = ArrayList()
    private var mTurnListDriving: ArrayList<String> = ArrayList()
    private var mPointListDriving: ArrayList<Point> = ArrayList()
    private var mManeuverDriving: ArrayList<String> = ArrayList()
    private var listOfArrayListsDriving = ArrayList<ArrayList<Point>>()
    private lateinit var weatherViewModel: WeatherViewModel
    private val apiKey = "a6450a304b61929119a1967808ae10a0"
    private val progressDialog: AlertDialog by lazy {
        showLoadingDialog()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        InfoUtil(this).setSystemBarsColor(R.attr.backgroundColor)
        val weatherService = Retrofit.Builder().baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(WeatherService::class.java)
        val factory = WeatherViewModelFactory(weatherService)
        weatherViewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]
        if (formattedTemp == 0) {
            weatherViewModel.fetchWeather(city, apiKey)
            weatherViewModel.weatherData.observe(this) { weatherData ->
                formattedTemp = weatherData.main.temp.toInt()
                binding.temp.text = "${formattedTemp}°C"
            }
        } else {
            binding.temp.text = "${formattedTemp}°C"
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.navigate.text == resources.getString(R.string.cancel_navigation) && !progressDialog.isShowing) {
                    showExitRouteDialog()
                } else {
                    finish()

                }
            }
        })
        FirebaseCustomEvents(this).createFirebaseEvents(navigation_view, "true")

        binding.sourse.isSelected = true
        binding.destination.isSelected = true
        binding.destinationPoint.isSelected = true
        binding.instruction.isSelected = true
        messageOnMap = MessageOnMap(this, binding.mapView)
        messageOnMapTime = MessageOnMap(this, binding.mapView)
        detailsList = mutableListOf()
        mapView = findViewById(R.id.mapView)
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        currentLocation = CurrentLocation(this, this, locationViewModel, 2)
        binding.mapView.getMapboxMap().also { mapboxMap ->
            this.mapboxMap = mapboxMap
        }
        if (intent != null) {
            pointedLatitude = intent.getDoubleExtra("destinationLatitude", 0.0)
            pointedLongitude = intent.getDoubleExtra("destinationLongitude", 0.0)

            Handler(Looper.getMainLooper()).postDelayed({
                pointedAddress =
                    currentLocation.getAddressFromLocation(pointedLatitude, pointedLongitude)

                binding.destination.text = pointedAddress
                // binding.destinationPoint.text=pointedAddress
            }, 1000)
        }
        // retrofit initialization
        val okHttpClient = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()
        retrofit = Retrofit.Builder().baseUrl("https://api.mapbox.com/").client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        serviceDirectionApi = retrofit.create(DirectionService::class.java)
        repository = DirectionRepository(serviceDirectionApi)

        //currentLocation.checkLocationPermission()
        // Trigger the route search when the button is clicked
        Handler(Looper.getMainLooper()).postDelayed({
            if (fullAddress == "") {
                currentLocation.checkLocationPermission()
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.sourse.text = fullAddress
                    focusOnPoints(longitude, latitude, pointedLongitude, pointedLatitude)
                }, 2000)


            } else {
//                val CENTER = Point.fromLngLat(longitude, latitude)
//                zoomMap(CENTER, true)
                binding.sourse.text = fullAddress
                focusOnPoints(longitude, latitude, pointedLongitude, pointedLatitude)
            }
        }, 2000)

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            val originalBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.destination_point)
            val resizedBitmap = resizeBitmap(originalBitmap) // Resize to 100x100 pixels
            it.addImage("destination-icon", resizedBitmap)
            //it.addImage("destination-icon", BitmapFactory.decodeResource(resources, R.drawable.destination_point) )
            //observeCurrentLocation()
            Log.d("mapbox", "Style loaded: ")
        }
        val directions = "${longitude},${latitude};${pointedLongitude},${pointedLatitude}"
        val endPoint = Point.fromLngLat(pointedLongitude, pointedLatitude)
        addDestinationMarker(endPoint)
        movingIconOnMap()
        if (!isInternetAvailable(this@NavigationView)) {
            Toast.makeText(
                this, "internet_not_available", Toast.LENGTH_SHORT
            ).show()
        } else {

            Handler(Looper.getMainLooper()).postDelayed({
                directionApiCal(lastSelectedDirectionsProfile, directions)
            }, 2000)
            if (!progressDialog.isShowing) progressDialog.show()
        }
        binding.icBack.clickWithDebounce {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.myloc.setOnClickListener {
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder().center(Point.fromLngLat(longitude, latitude)).zoom(16.0)
                    .build()
            )
        }
        binding.llDrivingNew.clickWithDebounce3 {
            if (!isInternetAvailable(this@NavigationView)) {
                Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show()
                return@clickWithDebounce3
            }
            if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_DRIVING_TRAFFIC && !isLayersClicked) {
                return@clickWithDebounce3
            }
            lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC

            movingIconOnMap()
            if (mFeatureListDriving.isEmpty()) {
                if (!progressDialog.isShowing) progressDialog.show()
                Log.d("list_", "onCreate: mFeatureListDriving =empty ")
                directionApiCal(
                    lastSelectedDirectionsProfile,
                    "${longitude},${latitude};${pointedLongitude},${pointedLatitude}"
                )

            } else {
                lineDraw(mFeatureListDriving)
            }

            focusOnPoints(longitude, latitude, pointedLongitude, pointedLatitude)

            profile("llDriving")
        }
        binding.llBikeNew.clickWithDebounce3 {
            if (!isInternetAvailable(this@NavigationView)) {
                Toast.makeText(this, "internet not available", Toast.LENGTH_SHORT).show()
                return@clickWithDebounce3
            }
            if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_CYCLING && !isLayersClicked) {
                return@clickWithDebounce3
            }

            lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_CYCLING
            val totalDistance = distance / 1000
            Log.e(TAG, "bottomSheetData bike total distance $totalDistance: ")
            if (totalDistance > 2500) {
                lifecycleScope.launch {
                    showSnackBar(binding.llBikeNew, "route not available")
                    delay(2000)
                    binding.llDrivingNew.performClick()
                }
                Log.e(TAG, "bottomSheetData: bike route is more than 2500 km ,$totalDistance ")
                return@clickWithDebounce3
            } else {
                Log.e(TAG, "bottomSheetData: bike route is less  than 3000 km , $totalDistance")
                movingIconOnMap()
                if (mFeatureListBike.isEmpty()) {
                    if (!progressDialog.isShowing) progressDialog.show()
                    //showLoadingDialog()
                    Log.d("list_", "onCreate: mFeatureListBike =empty ")
                    directionApiCal(
                        lastSelectedDirectionsProfile,
                        "${longitude},${latitude};${pointedLongitude},${pointedLatitude}"
                    )
                } else {
                    lineDraw(mFeatureListBike)
                }
                focusOnPoints(longitude, latitude, pointedLongitude, pointedLatitude)
                profile("llBike")
            }

        }
        binding.llWalkNew.clickWithDebounce3 {
            if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_WALKING && !isLayersClicked) {
                return@clickWithDebounce3
            }
            if (!isInternetAvailable(this@NavigationView)) {
                Toast.makeText(this, "internet not available", Toast.LENGTH_SHORT).show()
                return@clickWithDebounce3
            }

            lastSelectedDirectionsProfile = DirectionsCriteria.PROFILE_WALKING
            movingIconOnMap()
            if (mFeatureListWalk.isEmpty()) {

                if (distance > 1000000) {
                    lifecycleScope.launch {
                        showSnackBar(binding.llWalkNew, "route not available")
                        delay(2000)
                        binding.llDrivingNew.performClick()
                    }
                    return@clickWithDebounce3
                }
                if (!progressDialog.isShowing) progressDialog.show()
                directionApiCal(
                    lastSelectedDirectionsProfile,
                    "${longitude},${latitude};${pointedLongitude},${pointedLatitude}"
                )
            } else {
                lineDraw(mFeatureListWalk)
            }

            focusOnPoints(longitude, latitude, pointedLongitude, pointedLatitude)

            profile("llWalk")
        }
        binding.navigate.clickWithDebounce {

            if (binding.navigate.text == resources.getString(R.string.cancel_navigation)) {
                FirebaseCustomEvents(this).createFirebaseEvents(
                    navigationCancel_btn_clicked, "true"
                )
                showExitRouteDialog()
            } else {
                FirebaseCustomEvents(this).createFirebaseEvents(navigation_btn_clicked, "true")

                if (pointedAddress == "") {
                    Toast.makeText(this, "selecct destination", Toast.LENGTH_SHORT).show()
                } else {
                    if (!isInternetAvailable(this@NavigationView)) {
                        Toast.makeText(
                            this, "internet_not_available", Toast.LENGTH_SHORT
                        ).show()
                        return@clickWithDebounce
                    } else {
                        //
                        binding.layers.visibility = View.GONE
                        binding.navigate.setBackgroundResource(R.drawable.regbg)
                        startRoute = true
                        binding.floating.visibility = View.VISIBLE
                        binding.constraintLayout7.visibility = View.GONE
                        binding.llNavBtns.visibility = View.GONE
                        binding.navigate.text = resources.getString(R.string.cancel_navigation)
                        observeCurrentLocation()

                        //

                    }

                }
            }


        }
        binding.layers.clickWithDebounce {
            val context = this@NavigationView
            val anchorView = binding.layers
            val popup = PopupMenu(context, anchorView, Gravity.END, 0, R.style.MyPopupMenuStyle)
            val textColorBlack = ContextCompat.getColor(context, android.R.color.black)
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.layers_menu_items, popup.menu)

            val popupMenuStyle = ContextThemeWrapper(context, R.style.MyPopupMenuItemStyle)
            val menu = popup.menu

            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                val spannableString = SpannableString(menuItem.title)
                spannableString.setSpan(
                    TextAppearanceSpan(popupMenuStyle, 0),
                    0,
                    spannableString.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                spannableString.setSpan(
                    ForegroundColorSpan(textColorBlack),
                    0,
                    spannableString.length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                menuItem.title = spannableString
            }
            try {
                val fields = popup.javaClass.getDeclaredFields()
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popup)
                        menuPopupHelper?.javaClass?.getDeclaredMethod(
                            "setForceShowIcon", Boolean::class.java
                        )?.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popup.setOnMenuItemClickListener { item ->
                val mapView = binding.mapView?.getMapboxMap()
                val currentStyle = mapView?.getStyle()?.styleURI

                when (item.itemId) {
                    R.id.mapbox_street -> {
                        selectedLayer = Style.MAPBOX_STREETS
                        if (currentStyle != selectedLayer) {
                            //    mapView?.loadStyleUri(selectedLayer)
                            onLayersClick(
                                binding.llDrivingNew, binding.llBikeNew, binding.llWalkNew
                            )
                        }

                        true
                    }

                    R.id.traffic_day -> {
                        selectedLayer = Style.TRAFFIC_DAY
                        if (currentStyle != selectedLayer) {
                            //     mapView?.loadStyleUri(selectedLayer)
                            onLayersClick(
                                binding.llDrivingNew, binding.llBikeNew, binding.llWalkNew
                            )
                        }

                        true
                    }

                    R.id.TRAFFIC_NIGHT -> {
                        selectedLayer = Style.TRAFFIC_NIGHT
                        if (currentStyle != selectedLayer) {
                            //      mapView?.loadStyleUri(selectedLayer)
                            onLayersClick(
                                binding.llDrivingNew, binding.llBikeNew, binding.llWalkNew
                            )
                        }

                        true
                    }

                    R.id.SATELLITE -> {
                        selectedLayer = Style.SATELLITE
                        if (currentStyle != selectedLayer) {
                            //   mapView?.loadStyleUri(selectedLayer)
                            onLayersClick(
                                binding.llDrivingNew, binding.llBikeNew, binding.llWalkNew
                            )
                        }

                        true
                    }

                    R.id.DARK -> {
                        selectedLayer = Style.DARK
                        if (currentStyle != selectedLayer) {
                            //   mapView?.loadStyleUri(selectedLayer)
                            onLayersClick(
                                binding.llDrivingNew, binding.llBikeNew, binding.llWalkNew
                            )
                        }

                        true
                    }

                    R.id.LIGHT -> {
                        selectedLayer = Style.LIGHT
                        if (currentStyle != selectedLayer) {
                            //   mapView?.loadStyleUri(selectedLayer)
                            onLayersClick(
                                binding.llDrivingNew, binding.llBikeNew, binding.llWalkNew
                            )
                        }

                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }
    }

    private fun onLayersClick(llDriving: View, llBike: View, llWalk: View) {

        isLayersClicked = true
        when (lastSelectedDirectionsProfile) {

            DirectionsCriteria.PROFILE_DRIVING_TRAFFIC -> {
                llDriving.performClick()
            }

            DirectionsCriteria.PROFILE_CYCLING -> {
                llBike.performClick()
            }

            DirectionsCriteria.PROFILE_WALKING -> {
                llWalk.performClick()
            }
        }
        isLayersClicked = false

    }

    private fun showLoadingDialog(): AlertDialog {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.laoding_dialog, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return alertDialog
    }

    private fun getScaleExpression(): String {
        return interpolate {
            linear()
            zoom()
            stop {
                literal(10.0)
                literal(0.5)
            }
            20.0.let {
                stop {
                    literal(it)
                    literal(1.0)
                }
            }
        }.toJson()
    }

    private fun movingIconOnMap() {
        lifecycleScope.launch(Dispatchers.IO) {

            // Handle different profiles using a single function
            val (locationPuckDrawable, scaleExpression) = when (lastSelectedDirectionsProfile) {
                DirectionsCriteria.PROFILE_DRIVING_TRAFFIC -> R.drawable.yellowcar to getScaleExpression()

                DirectionsCriteria.PROFILE_CYCLING -> R.drawable.bikepng to getScaleExpression()

                DirectionsCriteria.PROFILE_WALKING -> R.drawable.man2 to getScaleExpression()

                else -> null to null
            }
            withContext(Dispatchers.Main) {
                try {
                    binding.mapView.location.let {
                        it.updateSettings {
                            if (locationPuckDrawable != null) {
                                this.locationPuck = LocationPuck2D(
                                    bearingImage = AppCompatResources.getDrawable(
                                        this@NavigationView, locationPuckDrawable
                                    ), scaleExpression = scaleExpression
                                )
                            }
                            this.enabled = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RouteDrawActivity", "Error in updateSettings: ${e.message}")
                }
            }

        }
    }


    private fun focusOnPoints(
        originLng: Double, originLat: Double, destinationLng: Double, destinationLat: Double
    ) {
        mapboxMap.easeTo(mapboxMap.cameraForCoordinateBounds(
            CoordinateBounds(
                Point.fromLngLat(originLng, originLat),
                Point.fromLngLat(destinationLng, destinationLat),
                false
            ), EdgeInsets(200.0, 50.0, 200.0, 50.0), null, null
        ), MapAnimationOptions.mapAnimationOptions {
            duration(1000L)
        })
    }

    // to observe live location
    private fun showExitRouteDialog() {

        val dialog = Dialog(this, R.style.AlertDialogDayNight)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.exit_route_dialog)
        //Animation
        val dialogView = dialog.window?.decorView
        dialogView?.scaleX = 0f
        dialogView?.scaleY = 0f
        val scaleXAnimator = ObjectAnimator.ofFloat(dialogView, "scaleX", 0f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(dialogView, "scaleY", 0f, 1f)
        scaleXAnimator.duration = 300
        scaleYAnimator.duration = 300
        scaleXAnimator.interpolator = DecelerateInterpolator()
        scaleYAnimator.interpolator = DecelerateInterpolator()
        scaleXAnimator.start()
        scaleYAnimator.start()

        dialog.show()

        fun dismissDialogWithAnimation() {
            val scaleXDismiss = ObjectAnimator.ofFloat(dialogView, "scaleX", 1f, 0f)
            val scaleYDismiss = ObjectAnimator.ofFloat(dialogView, "scaleY", 1f, 0f)
            scaleXDismiss.duration = 100
            scaleYDismiss.duration = 100
            scaleXDismiss.interpolator = DecelerateInterpolator()
            scaleYDismiss.interpolator = DecelerateInterpolator()

            // Start the dismiss animation and dismiss the dialog after it's done
            scaleXDismiss.start()
            scaleYDismiss.start()

            // Add a listener to dismiss the dialog after the animation
            scaleXDismiss.addListener(object : Animator.AnimatorListener {

                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    dialog.dismiss()
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
        }

        dialog.show()
        val cancelButton = dialogView!!.findViewById<TextView>(R.id.btn_no_unfav)
        val yesButton = dialogView.findViewById<TextView>(R.id.btn_yes_unfav)
        cancelButton.clickWithDebounce {
            dismissDialogWithAnimation()
        }
        yesButton.clickWithDebounce {
            stopTrackingUser()
            //binding.navigate.text = "Navigate"
            //binding.constraintLayout7.visibility = View.VISIBLE
            //binding.llNavBtns.visibility = View.VISIBLE
            //binding.floating.visibility = View.GONE
            startRoute = false
            dialog.dismiss()
//             startActivity(Intent(this@Navigation_View, Start_Activity::class.java))
            AdsManager.showInterstitial(
                true, this@NavigationView, object : AdsManager.InterstitialAdListener {
                    override fun onAdClosed() {
                        Log.e(
                            "TEST TAG",
                            "onAdClosed DIALOG YES BUTTON     mInterstitialAd: $mInterstitialAd",
                        )
                        finish()
                    }
                }, "navigation_activity"
            )
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 100, 100, false)
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ))
    }

    private fun directionApiCal(profile: String, coordinates: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make the API call in IO thread
                val response = repository.getDrivingDirections(
                    profile, coordinates, getString(R.string.mapbox_access_token)
                )
                Log.d(
                    TAG,
                    "directionApiCal: https://api.mapbox.com/directions/v5/mapbox/$profile,$coordinates,${
                        getString(R.string.mapbox_access_token)
                    }"
                )
                // Once the data is ready, move to Main thread to handle UI operations
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res != null && res.routes.isNotEmpty()) {
                            // Draw routes if data is available
                            Log.d("api", "directionApiCal:${res} ")
                            apiresponse = res
                            drawRoutes(res)
                        } else {

                            if (lastSelectedDirectionsProfile != DirectionsCriteria.PROFILE_DRIVING_TRAFFIC) {
                                showSnackBar(
                                    binding.root, "no route Available"
                                )
                                binding.llDrivingNew.performClick()
                            } else {
                                if (progressDialog.isShowing) {
                                    progressDialog.dismiss()
                                }
                                showRouteNotAvailableDialog()
                            }

                            Log.e(TAG, "directionApiCal: ")
                        }
                    } else {
                        Log.e(TAG, "directionApiCal: route not avialable response is empty  ")

                        if (lastSelectedDirectionsProfile != DirectionsCriteria.PROFILE_DRIVING_TRAFFIC) {
                            showSnackBar(binding.root, "route_not_available")
                            binding.llDrivingNew.performClick()
                        } else {
                            if (progressDialog.isShowing) {
                                progressDialog.dismiss()
                            }
                            showRouteNotAvailableDialog()

                        }


                    }
                }
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Exception Error: ${e.message}")
                    if (progressDialog.isShowing && !isFinishing) {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun showRouteNotAvailableDialog() {
        val dialog = AlertDialog.Builder(this).setTitle("Route Not Available")
            .setMessage("This route is not available, try another destination.")
            .setCancelable(false) // Prevents dismissing by clicking outside or pressing back
            .setPositiveButton("OK") { _, _ ->
                finish() // Close the activity
            }.create()

        dialog.show()
    }

    private fun announcemnetAtDistance(
        res: Example,
        distanceRemainToNextStep: ArrayList<ArrayList<Double>>,
        announcetList: ArrayList<ArrayList<String>>
    ) {
        for (i in 0 until res.routes[0].legs?.get(0)?.steps!!.size) {
            val voiceList = ArrayList<String>()
            val distanceList = ArrayList<Double>()
            for (n in 0 until res.routes[0].legs?.get(0)?.steps?.get(i)?.voiceInstructions!!.size) {
                res.routes[0].legs?.get(0)?.steps?.get(i)?.voiceInstructions?.get(n)?.let {
                    distanceList.add(
                        it.distanceAlongGeometry
                    )
                }
                res.routes[0].legs?.get(0)?.steps?.get(i)?.voiceInstructions?.get(n)
                    ?.let { voiceList.add(it.announcement) }
            }
            distanceRemainToNextStep.add(distanceList)
            announcetList.add(voiceList)
        }
    }

    private fun lineStringList(res: Example, mListOfArray: ArrayList<ArrayList<Point>>) {
        for (i in 0 until res.routes[0].legs!![0].steps!!.size) {
            val lineStringList = ArrayList<Point>() // Create a new list for each iteration

            for (n in 0 until res.routes[0].legs?.get(0)?.steps?.get(i)?.geometry!!.coordinates.size) {
                lineStringList.add(
                    Point.fromLngLat(
                        res.routes[0].legs?.get(0)?.steps?.get(i)?.geometry!!.coordinates[n][0],
                        res.routes[0].legs?.get(0)?.steps?.get(i)?.geometry!!.coordinates[n][1]
                    )
                )
            }

            mListOfArray.add(lineStringList)
        }
    }

//++++++++++++++latest
//    private fun routeArrays(
//        res: Example,
//        mArrayFeature: ArrayList<Point>,
//        mArrayLocation: ArrayList<Point>,
//        mBannerIns: ArrayList<String>,
//        mTurnList: ArrayList<String>,
//        mManeuver: ArrayList<String>,
//        mPoint: ArrayList<Point>
//    ) {
//        val route = res.routes.getOrNull(0) ?: return  // Ensure route exists
//        val geometry = route.geometry
//        val legs = route.legs?.getOrNull(0)
//        val steps = legs?.steps ?: emptyList()
//
//        // Populate mArrayFeature safely
//        geometry?.coordinates?.forEach { coordinate ->
//            val lng = coordinate.getOrNull(0) ?: 0.0
//            val lat = coordinate.getOrNull(1) ?: 0.0
//            mArrayFeature.add(Point.fromLngLat(lng, lat))
//        }
//
//        for (i in steps.indices) {
//            val step = steps[i]
//            val maneuver = step.maneuver
//            val bannerInstructions = step.bannerInstructions ?: emptyList()
//
//            // Add maneuver locations safely
//            val maneuverLocation = maneuver.location
//            if (maneuverLocation?.size == 2) {
//                mArrayLocation.add(Point.fromLngLat(maneuverLocation[0], maneuverLocation[1]))
//            }
//
//            // Add banner instructions safely
//            val bannerText = bannerInstructions.getOrNull(0)?.primary?.text ?: "Unknown Destination"
//            val turnType = bannerInstructions.getOrNull(0)?.primary?.modifier ?: "straight"
//
//            mBannerIns.add(bannerText)
//            mTurnList.add(turnType)
//
//            // Add maneuver instruction
//            val instruction = maneuver.instruction ?: "No Instructions Available"
//            mManeuver.add(instruction)
//
//            // Add last step instruction if it's the last step
//            if (i == steps.lastIndex) {
//                mBannerIns.add(instruction)
//            }
//        }
//
//        // Populate mPoint safely
//        for (point in mFeatureList) {
//            mPoint.add(Point.fromLngLat(point.longitude(), point.latitude()))
//        }
//    }


    private fun routeArrays(
        res: Example,
        mArrayFeature: ArrayList<Point>,
        mArrayLocation: ArrayList<Point>,
        mBannerIns: ArrayList<String>,
        mTurnList: ArrayList<String>,
        mManeuver: ArrayList<String>,
        mPoint: ArrayList<Point>
    ) {
        for (i in 0 until res.routes[0].geometry!!.coordinates.size) {
            mArrayFeature.add(
                Point.fromLngLat(
                    (res.routes[0].geometry!!.coordinates[i][0]),
                    res.routes[0].geometry!!.coordinates[i][1]
                )
            )
        }
        for (i in 0 until res.routes[0].legs?.get(0)?.steps!!.size) {
            res.routes[0].legs?.get(0)?.steps?.get(i)?.maneuver?.location?.get(0)?.let {
                Point.fromLngLat(
                    it, res.routes[0].legs?.get(0)?.steps?.get(i)?.maneuver!!.location[1]
                )
            }?.let {
                mArrayLocation.add(
                    it
                )
            }

            if (res.routes[0].legs?.get(0)?.steps?.get(i)?.bannerInstructions!!.isNotEmpty()) {
                mBannerIns.add(
                    res.routes[0].legs?.get(0)?.steps?.get(i)?.bannerInstructions!![0].primary.text
                )
            }
            if (res.routes[0].legs?.get(0)?.steps?.get(i)?.bannerInstructions!!.isNotEmpty()) {
                mTurnList.add(res.routes[0].legs?.get(0)?.steps?.get(i)!!.bannerInstructions[0].primary.modifier)
            }
            if (i == res.routes[0].legs?.get(0)?.steps!!.size - 1) {
                mBannerIns.add(res.routes[0].legs?.get(0)?.steps?.get(i)?.maneuver!!.instruction)
            }
            mManeuver.add(res.routes[0].legs?.get(0)?.steps?.get(i)?.maneuver!!.instruction)
        }
        for (point in mFeatureList) {
            mPoint.add(Point.fromLngLat(point.longitude(), point.latitude()))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun profile(name: String) {
        val grey = ContextCompat.getColor(this, R.color.grey)
        val blue = ContextCompat.getColor(this, R.color.mBlue)
        when (name) {
            "llDriving" -> {
                binding.ivDrivingNew.setColorFilter(blue, PorterDuff.Mode.SRC_ATOP)
                binding.ivBikeNew.setColorFilter(grey, PorterDuff.Mode.SRC_ATOP)
                binding.ivWalkNew.setColorFilter(grey, PorterDuff.Mode.SRC_ATOP)


                val timeText = if (time.first > 0) {
                    "${time.first} hr ${time.second} min"
                } else {
                    "${time.second} min"
                }
                val distanceText = if (getDistanceFromShared(this) == "Kilometers") {
                    "${distance / 1000} km"
                } else {
                    "${(distance * 0.000621371).toInt()} mi"
                }

                binding.currentAddress.text = "$timeText ($distanceText)"
            }

            "llBike" -> {
                binding.ivDrivingNew.setColorFilter(grey, PorterDuff.Mode.SRC_ATOP)
                binding.ivBikeNew.setColorFilter(blue, PorterDuff.Mode.SRC_ATOP)
                binding.ivWalkNew.setColorFilter(grey, PorterDuff.Mode.SRC_ATOP)

                val timeText = if (timeBike.first > 0) {
                    "${timeBike.first} hr ${timeBike.second} min"
                } else {
                    "${timeBike.second} min"
                }

                val distanceText = if (getDistanceFromShared(this) == "Kilometers") {
                    "${distanceBike / 1000} km"
                } else {
                    "${(distanceBike * 0.000621371).toInt()} mi"
                }

                binding.currentAddress.text = "$timeText ($distanceText)"
            }

            "llWalk" -> {
                binding.ivDrivingNew.setColorFilter(grey, PorterDuff.Mode.SRC_ATOP)
                binding.ivBikeNew.setColorFilter(grey, PorterDuff.Mode.SRC_ATOP)
                binding.ivWalkNew.setColorFilter(blue, PorterDuff.Mode.SRC_ATOP)
                val timeText = if (timeWalk.first > 0) {
                    "${timeWalk.first} hr ${timeWalk.second} min"
                } else {
                    "${timeWalk.second} min"
                }
                val distanceText = if (getDistanceFromShared(this) == "Kilometers") {
                    "${distanceWalk / 1000} km"
                } else {
                    "${(distanceWalk * 0.000621371).toInt()} mi"
                }

                binding.currentAddress.text = "$timeText ($distanceText)"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun drawRoutes(res: Example) {

        mTurnList.clear()
        mLoation.clear()
        mFeatureList.clear()
        mManeuver.clear()
        mPointList.clear()
        mBannerInstruction.clear()
        binding.mapView.getMapboxMap().loadStyleUri(
            selectedLayer
        ) {
            if (res.routes.isEmpty()) {
                return@loadStyleUri
            }
            announcemnetAtDistance(res, distanceRemainBtwSteps, announcementList)
            lineStringList(res, listOfArrayLists)
            routeArrays(
                res, mFeatureList, mLoation, mBannerInstruction, mTurnList, mManeuver, mPointList
            )
            lineDraw(mFeatureList)
//            val totalDistance = res.routes?.get(0)?.distance ?: 0.0 // In meters
//            val totalDuration = res.routes?.get(0)?.duration ?: 0.0 // In seconds
//            val minutes = (totalDuration / 60).toInt()
//            val seconds = (totalDuration % 60).toInt()
//            val durationTime = if (minutes > 0) {
//                "$minutes minutes, $seconds seconds"
//            } else {
//                "$seconds seconds"
//            }
//            val formattedDistance = if (totalDistance < 1000) {
//                // If the distance is less than 1 kilometer, display in meters
//                String.format("%.0f", totalDistance) + " m"
//            } else {
//                // Otherwise, convert to kilometers
//                String.format("%.2f", totalDistance / 1000) + " km"
//            }
//            // Log or display the results
//            Log.e("mapbox", "Total Distance:${formattedDistance}")
//            Log.e("mapbox", "Total Time: ${totalDuration / 60} minutes")
//
//            // Example: Displaying results on UI
//            binding.currentAddress.text = "$durationTime ($formattedDistance)"

            kkilometers = res.routes[0].legs?.get(0)?.distance!!.toInt() / 1000
            ttime = secondsToHoursMinutes(res.routes[0].duration.toInt())

            if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_DRIVING_TRAFFIC) {
                routeArrays(
                    res,
                    mFeatureListDriving,
                    mLoationDriving,
                    mBannerInstructionDriving,
                    mTurnListDriving,
                    mManeuverDriving,
                    mPointListDriving
                )
                lineStringList(res, listOfArrayListsDriving)
                // binding.tvDrivingNew.text = time.first.toString() + " hr " + time.second.toString() + " min"
                val kilometers = res.routes[0].legs?.get(0)?.distance!!.toInt()
                distance = kilometers
                ttime = secondsToHoursMinutes(res.routes[0].duration.toInt())
                time = ttime
                val time = if (time.first > 0) {
                    "${time.first} hr ${time.second} min"
                } else {
                    "${time.second} min"
                }
                if (getDistanceFromShared(this) == "Kilometers") {
                    binding.currentAddress.text = "$time (${distance / 1000} km)"
                } else {
                    binding.currentAddress.text = "$time (${(distance * 0.000621371).toInt()}) mi"
                }
                Log.d(TAG, "drawRoutes: message")
                //  showMapMessageIcon()
            }
            //other profiles

            else if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_CYCLING) {
                routeArrays(
                    res,
                    mFeatureListBike,
                    mLoationBike,
                    mBannerInstructionBike,
                    mTurnListBike,
                    mManeuverBike,
                    mPointListBike
                )
                lineStringList(res, listOfArrayListsBike)

                val kilometers = res.routes[0].legs?.get(0)?.distance!!.toInt()
                distanceBike = kilometers

                ttime = secondsToHoursMinutes(
                    res.routes[0].duration.toInt()
                )
                timeBike = ttime

                val time = timeBike.first.toString() + " hr " + timeBike.second.toString() + " min"
                if (getDistanceFromShared(this) == "Kilometers") {
                    binding.currentAddress.text = "$time (${distanceBike / 1000} km)"
                } else {
                    binding.currentAddress.text =
                        "$time (${(distanceBike * 0.000621371).toInt()}) mi"
                }
            } else {
                routeArrays(
                    res,
                    mFeatureListWalk,
                    mLoationWalk,
                    mBannerInstructionWalk,
                    mTurnListWalk,
                    mManeuverWalk,
                    mPointListWalk
                )
                lineStringList(res, listOfArrayListsWalk)

                val kilometers = res.routes[0].legs?.get(0)?.distance!!.toInt()
                distanceWalk = kilometers

                ttime = secondsToHoursMinutes(
                    res.routes[0].duration.toInt()
                )
                timeWalk = ttime
                val time = timeWalk.first.toString() + " hr " + timeWalk.second.toString() + " min"

                if (getDistanceFromShared(this) == "Kilometers") {
                    binding.currentAddress.text = "$time (${distanceWalk / 1000} km)"
                } else {
                    binding.currentAddress.text =
                        "$time (${(distanceWalk * 0.000621371).toInt()}) mi"
                }
            }

        }
        if (progressDialog.isShowing) progressDialog.dismiss()
    }

    private fun secondsToHoursMinutes(seconds: Int): Pair<Int, Int> {
        val hours = seconds / 3600
        val remainingSeconds = seconds % 3600
        val minutes = remainingSeconds / 60
        return Pair(hours, minutes)
    }

    private var previousLatitude: Double? = null
    private var previousLongitude: Double? = null

    private fun observeCurrentLocation() {
        binding.myloc.performClick()
        tts = TextToSpeech(this, this)
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true // Pulsing animation for the current location
        }

        if (!isTracking) {
            isTracking = true
            mapView.location.addOnIndicatorPositionChangedListener { point ->
                val newLatitude = point.latitude()
                val newLongitude = point.longitude()

                // Check if the location has changed significantly
                if (previousLatitude == null || previousLongitude == null || previousLatitude != newLatitude || previousLongitude != newLongitude) {

                    previousLatitude = newLatitude
                    previousLongitude = newLongitude

                    Log.d("testing", "Location updated: $newLatitude, $newLongitude")
                    latitude = newLatitude
                    longitude = newLongitude

                    if (!isUpdating) {
                        if (startRoute) {
                            isUpdating = true
                            when (lastSelectedDirectionsProfile) {
                                DirectionsCriteria.PROFILE_DRIVING_TRAFFIC -> {
                                    updateBannerText(
                                        mLoationDriving,
                                        mBannerInstructionDriving,
                                        mManeuverDriving,
                                        mTurnListDriving,
                                        mPointListDriving,
                                        listOfArrayListsDriving,
                                        distanceRemainBtwSteps,
                                        announcementList
                                    )
                                }

                                DirectionsCriteria.PROFILE_CYCLING -> {
                                    updateBannerText(
                                        mLoationBike,
                                        mBannerInstructionBike,
                                        mManeuverBike,
                                        mTurnListBike,
                                        mPointListBike,
                                        listOfArrayListsBike,
                                        distanceRemainBtwSteps,
                                        announcementList
                                    )
                                }

                                else -> {
                                    updateBannerText(
                                        mLoationWalk,
                                        mBannerInstructionWalk,
                                        mManeuverWalk,
                                        mTurnListWalk,
                                        mPointListWalk,
                                        listOfArrayListsWalk,
                                        distanceRemainBtwSteps,
                                        announcementList
                                    )
                                }
                            }
                        }
                    }

                    // Update camera only if location changed
//                    mapView.getMapboxMap().setCamera(
//                        CameraOptions.Builder()
//                            .center(point)
//                            .zoom(16.0) // Adjust zoom level if needed
//                            .build()
//                    )
                }
            }
        }
    }
//    private fun observeCurrentLocation() {
//
//        tts = TextToSpeech(this, this)
//        mapView.location.updateSettings {
//            enabled = true
//            pulsingEnabled = true // Pulsing animation for the current location
//        }
//        if (!isTracking) {
//            isTracking = true
//            mapView.location.addOnIndicatorPositionChangedListener { point ->
//
//                Log.d("testing", "1 observeCurrentLocation: listern true")
//                latitude = point.latitude()
//                longitude = point.longitude()
//                if (!isUpdating) {
//                    Log.d("testing", "2 isUpdating: isUpdating true")
//                    if (startRoute) {
//                        Log.d("testing", "3 startRoute: startRoute true")
//                        if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_DRIVING_TRAFFIC) {
//                            Log.d(
//                                "testing",
//                                "4 lastSelectedDirectionsProfile: lastSelectedDirectionsProfile true"
//                            )
//                            isUpdating = true
//                            updateBannerText(
//                                mLoation,
//                                mBannerInstruction,
//                                mManeuver,
//                                mTurnList,
//                                mPointList,
//                                listOfArrayLists,
//                                distanceRemainBtwSteps,
//                                announcementList
//                            )
//                        } else if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_CYCLING) {
//                            Log.d(
//                                "testing",
//                                "4 lastSelectedDirectionsProfile cycling: lastSelectedDirectionsProfile true"
//                            )
//                            isUpdating = true
//                            updateBannerText(
//                                mLoationBike,
//                                mBannerInstructionBike,
//                                mManeuverBike,
//                                mTurnListBike,
//                                mPointListBike,
//                                listOfArrayListsBike,
//                                distanceRemainBtwSteps,
//                                announcementList
//                            )
//                        } else {
//                            Log.d(
//                                "testing",
//                                "4 lastSelectedDirectionsProfile walking: lastSelectedDirectionsProfile true"
//                            )
//                            isUpdating = true
//                            updateBannerText(
//                                mLoationWalk,
//                                mBannerInstructionWalk,
//                                mManeuverWalk,
//                                mTurnListWalk,
//                                mPointListWalk,
//                                listOfArrayListsWalk,
//                                distanceRemainBtwSteps,
//                                announcementList
//                            )
//                        }
//
//                    }
//                }
//                // Update map's camera to follow the user's location dynamically
//
////                mapView.getMapboxMap().setCamera(
////                    CameraOptions.Builder()
////                        .center(point)
////                        .zoom(16.0)
////                        .build()
////                )
//            }
//            // Add bearing listener to rotate map based on user direction
////            binding.mapView.location.addOnIndicatorBearingChangedListener { bearing ->
////                binding.mapView.getMapboxMap().setCamera(
////                    CameraOptions.Builder()
////                        .bearing(bearing)  // Rotate map to match user direction
////                        .build()
////                )
////            }
//        }
//    }

    @SuppressLint("SetTextI18n")
    private fun updateBannerText(
        listLocation: ArrayList<Point>,
        listBanner: ArrayList<String>,
        listManeuver: ArrayList<String>,
        listTurnType: ArrayList<String>,
        listPoints: ArrayList<Point>,
        listOfLineStringArray: ArrayList<ArrayList<Point>>,
        distanceRemainBtwSteps: ArrayList<ArrayList<Double>>,
        announcementList: ArrayList<ArrayList<String>>
    ) {
        try {
            Log.d("testing", "4 bannertext:  true")
            for (pointOfList in listLocation) {
                Log.d("testing", "5 for:  true")
                val longitude1 = pointOfList.longitude()
                val latitude1 = pointOfList.latitude()

                val threshold = 0.00010 // 73.085625,
                // Calculate the distance between the current location and the upcoming turn point


                if (binding.destinationPoint.text.isEmpty()) {
                    Log.d("testing", "6 textempty:  true")
                    if (listTurnType.isNotEmpty()) {
                        val turnType = listTurnType.getOrNull(0)
                        when (turnType) {
                            "left", "right", "slight right", "straight", "slight left", "sharp right", "sharp left", "uturn" -> {
                                Glide.with(this).load(getTurnDrawable(turnType))
                                    .into(binding.indication)
                            }
                        }
                    }
                    if (listBanner.isNotEmpty()) binding.destinationPoint.text = listBanner[0]
                    if (listManeuver.isNotEmpty()) {
                        binding.instruction.text = listManeuver[0]
                        if (isSpeakerOn) speakOut(listManeuver[0])
                    }

                }

                if (abs(longitude1 - longitude) < threshold && abs(latitude1 - latitude) < threshold) {
                    Log.d("testing", "7 if:  true")
                    val index = listLocation.indexOf(pointOfList)
                    // Show your text or perform any other action here
                    if (lastLng != longitude1 && lastLat != latitude1) {
                        Log.d("testing", "8 if:  true")
                        binding.destinationPoint.text = listBanner[index]
                        binding.instruction.text = listManeuver[index]
                        if (isSpeakerOn) {
                            speakOut(listManeuver[index])
                        }
                        if (index < listTurnType.size) {
                            when (listTurnType[index]) {
                                "left", "right", "slight right", "straight", "slight left", "sharp right", "sharp left", "uturn" -> {
                                    Glide.with(this).load(getTurnDrawable(listTurnType[index]))
                                        .into(binding.indication)
                                }
                            }
                            if (listBanner.size - 1 == index) {
                                showDestinationDialog()
                            }
                        }
                        i = index
                        if (listLocation.size - 1 == index) {
                            lastPoint = true
                            showDestinationDialog()
                        }
                        println("voice ")
                    }
                    lastLng = longitude1
                    lastLat = latitude1
                    break
                }

            }
            for (point in listPoints) {
                Log.d("testing", "9 for point:  true")
                val longitude1 = point.longitude()
                val latitude1 = point.latitude()
                val threshold = 0.00010
                if (abs(longitude1 - longitude) < threshold && abs(latitude1 - latitude) < threshold) {
                    Log.d("testing", "10 if point:  true")

                    val index = listPoints.indexOf(point)
                    // Show your text or perform any other action here
                    if (lastLngDes != longitude1 && lastLatDes != latitude1) {
                        calculateDistance(d)
                    }
                    d = index
                    println("voice ")
                    lastLngDes = longitude1
                    lastLatDes = latitude1
                    break
                }
            }
            if (i in listOfLineStringArray.indices) {
                for (point in listOfLineStringArray[i]) {
                    Log.d("testing", "11 for line:  true")

                    val longitude1 = point.longitude()
                    val latitude1 = point.latitude()
                    val threshold = 0.0001
                    if (abs(longitude1 - longitude) < threshold && abs(latitude1 - latitude) < threshold) {


                        val index = listOfLineStringArray[i].indexOf(point)

                        // Show your text or perform any other action here
                        if (lineStringLngDes != longitude1 && lineStringLatDes != latitude1) {
                            calculateDistanceBetweenTwoInstructions(
                                index
                            )

                            //d = index
                            println("voice ")
                        }


                        for (v in distanceRemainBtwSteps[i]) {
                            var voice: String
                            var distance: Double

                            for (k in 0 until distanceRemainBtwSteps[i].size - 1) {
                                distance = distanceRemainBtwSteps[i][k]
                                if (k < distanceRemainBtwSteps[i].size) {
                                    val distanc2 = distanceRemainBtwSteps[i][k + 1]
                                    if (distnaceToNextPoint < distance && distnaceToNextPoint > distanc2) {
                                        if (preVoice != k) {
                                            voice = announcementList[i][k]
                                            if (isSpeakerOn) {
                                                speakOut(voice)
                                            }
                                            preVoice = k

                                        }
                                    }
                                } else {
                                    if (distnaceToNextPoint < distance) {
                                        if (preVoice != k) {
                                            voice = announcementList[i][k]
                                            if (isSpeakerOn) {
                                                speakOut(voice)
                                            }
                                            preVoice = k
                                        }
                                    }
                                }
                            }
                        }

                        lineStringLngDes = longitude1
                        lineStringLatDes = latitude1
                        break
                    }

                }

            }

            if (kmphSpeed < 10 && !lastPoint) {
                when (lastSelectedDirectionsProfile) {

                    DirectionsCriteria.PROFILE_DRIVING_TRAFFIC -> {
                        remainTextView(binding.currentAddress, distance, time)
                    }

                }

            } else if (!lastPoint) {
                if (getDistanceFromShared(this) == "Kilometers") {
                    // Log.e(TAG, "Distance is:$remainingDistance")
                    binding.currentAddress.text = "${
                        convertDoubleToTime(
                            calculateTimeToDestination(
                                remainingDistance, currentSpeed
                            )
                        )
                    } " + "(${remainingDistance.toInt() / 1000}) km"
                }
            } else {

                binding.currentAddress.text = "you reached your destination"
            }
            if (binding.currentAddress.text.toString() == "00 hr 00 min") {
                binding.currentAddress.text = "less then 1 min"
            }

        } finally {
            isUpdating = false
        }
    }

    private fun calculateTimeToDestination(distance: Double, speed: Double): Double {
        return distance / speed
    }

    @SuppressLint("DefaultLocale")
    private fun convertDoubleToTime(durationInSeconds: Double): String {
        val hours = TimeUnit.SECONDS.toHours(durationInSeconds.toLong())
        val minutes =
            TimeUnit.SECONDS.toMinutes(durationInSeconds.toLong()) - TimeUnit.HOURS.toMinutes(hours)
        return String.format("%02d hr %02d min", hours, minutes)
    }

    @SuppressLint("SetTextI18n")
    private fun remainTextView(tvRemainingTime: TextView, distance: Int, time: Pair<Int, Int>) {
        if (getDistanceFromShared(this) == "Kilometers") {
            tvRemainingTime.text =
                time.first.toString() + " hr " + time.second.toString() + " min" + " (${distance / 1000} km)"

        } else {
            tvRemainingTime.text =
                time.first.toString() + " hr " + time.second.toString() + " min" + " (${(distance * 0.000621371).toInt()} mi)"
        }
    }

    private fun calculateDistanceBetweenTwoInstructions(f: Int) {
        distnaceToNextPoint = 0.0
        for (k in f until listOfArrayLists[i].size - 1) {
            val from = listOfArrayLists[i][k]
            val to = listOfArrayLists[i][k + 1]
            val line = LineString.fromLngLats(listOf(from, to))
            val segmentDistance = TurfMeasurement.length(line, TurfConstants.UNIT_METERS)
            distnaceToNextPoint += segmentDistance
        }
    }

    private fun calculateDistance(d: Int) {
        if (lastSelectedDirectionsProfile == DirectionsCriteria.PROFILE_DRIVING_TRAFFIC) {
            remainingDistance = 0.0
            for (i in d until mFeatureListDriving.size - 1) {
                val from = mFeatureListDriving[i]
                val to = mFeatureListDriving[i + 1]
                val line = LineString.fromLngLats(listOf(from, to))
                val segmentDistance = TurfMeasurement.length(line, TurfConstants.UNIT_METERS)
                remainingDistance += segmentDistance
                //binding.tvDistance.text = (remainingDistance / 1000).toInt().toString() + " km"
            }
        }
    }

    private fun showDestinationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.destination_dialog_layout, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCancelable(false)
        val yesButton = dialogView.findViewById<TextView>(R.id.btn_yes)
        yesButton.clickWithDebounce {
            alertDialog.dismiss()
            AdsManager.showInterstitial(
                true, this@NavigationView, object : AdsManager.InterstitialAdListener {
                    override fun onAdClosed() {
                        startRoute = false
                        startActivity(
                            Intent(
                                this@NavigationView, MainActivity::class.java
                            ).putExtra("review", true)
                        )
                        finish()
                    }
                }, "navigation_activity"
            )

        }
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun speakOut(text: String) {
        if (tts.isSpeaking) {
            Handler(Looper.getMainLooper()).postDelayed({
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")
            }, 1000) // Delay of 1000 milliseconds (1 second)
        } else {
            // If TTS is not speaking, speak immediately
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    private fun stopTrackingUser() {
        if (isTracking) {
            isTracking = false
            binding.mapView.location.removeOnIndicatorPositionChangedListener {}
            binding.mapView.location.removeOnIndicatorBearingChangedListener {}
        }
    }

    private fun showSnackBar(view: View, messageText: String) {
        val snackBar = Snackbar.make(view, messageText, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    private fun lineDraw(mFeatureList: ArrayList<Point>) {

        binding.mapView.getMapboxMap().loadStyleUri(
            selectedLayer
        ) {

            mFeatureList.add(0, Point.fromLngLat(longitude, latitude))

            val featureList = mFeatureList
            val lineString = LineString.fromLngLats(featureList)
            val feature = com.mapbox.geojson.Feature.fromGeometry(lineString)
            val featureCollection = FeatureCollection.fromFeatures(listOf(feature))

            val geoJsonSource =
                GeoJsonSource.Builder("source-id").featureCollection(featureCollection).build()

            it.addSource(geoJsonSource)

            val lineLayer = LineLayer("layer-id", "source-id").lineWidth(8.0).lineCap(LineCap.ROUND)
                .lineJoin(LineJoin.ROUND)
                .lineColor(ContextCompat.getColor(applicationContext, R.color.mBlue))
            if (selectedLayer == Style.MAPBOX_STREETS) {
                it.addLayerBelow(lineLayer, "road-label")
            } else {
                it.addLayer(lineLayer)
            }

        }
    }

    override fun onResume() {
        navigatingFromnavigation = false
        super.onResume()

    }

    private fun addDestinationMarker(point: Point) {
        mapView.annotations.createPointAnnotationManager().apply {
            val annotationOptions =
                PointAnnotationOptions().withPoint(point).withIconImage("destination-icon")
                    .withIconSize(0.5) // Ensure this image is added to the style
            create(annotationOptions)
        }
    }

    override fun onInit(p0: Int) {
    }

    override fun onPause() {
        super.onPause()
        if (!navigatingFromnavigation) {
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.navigation_disappear, "true"
            )
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromnavigation = true
    }

}
