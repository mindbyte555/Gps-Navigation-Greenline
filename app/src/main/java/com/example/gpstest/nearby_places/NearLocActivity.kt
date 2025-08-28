package com.example.gpstest.nearby_places

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.activities.BaseActivity
import com.example.gpstest.R
import com.example.gpstest.activities.MyLoc
import com.example.gpstest.databinding.ActivityNearLocBinding
import com.example.gpstest.gps.LocationPermissionHelper
import com.example.gpstest.activities.Navigate
import com.example.gpstest.searchPlacesApiResponse.Feature
import com.example.gpstest.searchPlacesApiResponse.Places
import com.example.gpstest.utls.InfoUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NearLocActivity : BaseActivity() {
    lateinit var binding: ActivityNearLocBinding
    val TAG = javaClass.simpleName
    var flag =false
    var firststart=true
    private lateinit var mapboxMap: MapboxMap
    private var currentLng = 0.0
    private var currentLat = 0.0
    lateinit var tvEmptyLocation: TextView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    lateinit var persistentBottomSheet: LinearLayout
    private lateinit var locationHelper: LocationPermissionHelper

    // private lateinit var mapMarkersManager: MapMarkersManager
    lateinit var service: NearByPlacesService
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearLocBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30+
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        locationHelper = LocationPermissionHelper(this)
        persistentBottomSheet = findViewById(R.id.bottom_sheet_nearby_p)
        tvEmptyLocation = persistentBottomSheet.findViewById(R.id.tv_empty_location)
        InfoUtil(this).setSystemBarsColor(R.attr.backgroundColor)
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.grantpermission.setOnClickListener {
            locationHelper.requestLocationPermission(this)
        }

        binding.enablegps.setOnClickListener {
            locationHelper.openLocationSettings(this)
        }
        checkPermissions()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mInterstitialAd != null) {
                    AdsManager.showInterstitial(
                        true, this@NearLocActivity, object : AdsManager.InterstitialAdListener {
                            override fun onAdClosed() {
                               finish()
                            }
                        }, "NearbyPlaces_activity_Back"
                    )
                } else {
                    if (interstitialAd != null) {
                        showAvailableInterstitial(this@NearLocActivity) {
                            finish()
                        }
                    } else {
                        InterstitialClass.requestInterstitial(this@NearLocActivity,
                            this@NearLocActivity,
                            "NearbyPlaces_activity_Back",
                            object : ActionOnAdClosedListener {
                                override fun ActionAfterAd() {
                                   finish()
                                }
                            })
                    }
                }

            }
        })

    }
    private fun checkPermissions() {
        when {
            !locationHelper.isLocationPermissionGranted() -> {
                binding.permissionLayout.visibility = View.VISIBLE
                binding.settinglayout.visibility = View.GONE
                binding.groupcontent.visibility = View.GONE
            }
            !locationHelper.isLocationEnabled() -> {
                binding.permissionLayout.visibility = View.GONE
                binding.settinglayout.visibility = View.VISIBLE
                binding.groupcontent.visibility = View.GONE
            }
            else -> {
                checkInternet()
                binding.permissionLayout.visibility = View.GONE
                binding.settinglayout.visibility = View.GONE
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
                checkPermissions()
            }
            else {

                locationHelper.openAppSettings(this)
            }
        }
    }

    private fun checkInternet() {
        if (!isInternetAvailable(this@NearLocActivity)) {
            tvEmptyLocation.visibility = View.VISIBLE
            tvEmptyLocation.text=resources.getString(R.string.no_internet_connections)
           // Handler().postDelayed({finish()},2000)
        } else {
            Log.e(ContentValues.TAG, "checkInternet: internet is running ")
                if (!flag){
                    loading()
                    initData()
                }
        }
    }

    override fun onResume() {
        checkPermissions()
        super.onResume()
    }
    private fun showLocationAlert() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Your Location Settings is set to 'Off'.\nPlease Enable Location to use this app.")
            .setCancelable(false)
            .setPositiveButton("Location Settings") { dialog, _ ->
                firststart=false
                dialog.dismiss() // Dismiss the dialog before starting the intent
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("cancel"){dialog,_ ->
                dialog.dismiss()
                finish()
            }
            .create()
        alertDialog.show()
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    @SuppressLint("ServiceCast")
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
    private fun loading() {
        if (this@NearLocActivity.isFinishing || this@NearLocActivity.isDestroyed) {
            return
        }
        Log.d("TAG", "loading: true ")
        val dialogView = LayoutInflater.from(this).inflate(R.layout.laoding_dialog, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        alertDialog = builder.create()
        alertDialog?.show()
        alertDialog?.setCancelable(false)
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    private fun initData() {

        flag=true
        Log.d("TAG", "initData: true and flaf :$flag")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.mapbox.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(NearByPlacesService::class.java)
        //      mapMarkersManager = MapMarkersManager(binding.mapViewNearLocations, applicationContext)

        val name = intent.getStringExtra("type")
        val qury = intent.getStringExtra("name")

        binding.tvNavText.text = name


        val mapView = binding.mapViewNearLocations
        mapView!!.getMapboxMap().also { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                mapView!!.location.updateSettings {
                    enabled = true
                }

                mapView!!.location.addOnIndicatorPositionChangedListener(object :
                    OnIndicatorPositionChangedListener {
                    override fun onIndicatorPositionChanged(point: Point) {

                        currentLat = point.latitude()
                        currentLng = point.longitude()
                        mapView!!.getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                .center(point)
                                .zoom(14.0)
                                .build()
                        )
                        mapView!!.location.removeOnIndicatorPositionChangedListener(this)
                        searchForNearBy(
                            qury.toString(), "${point.longitude()},${point.latitude()}",
                            "${point.longitude() - 0.27},${point.latitude() - 0.27},${point.longitude() + 0.27},${point.latitude() + 0.27}"
                        )
                    }
                })
            }
        }
        bottomSheetBehavior = BottomSheetBehavior.from(persistentBottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, state: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        bottomSheetBehavior.isDraggable = false
        val tvCategory = persistentBottomSheet.findViewById<TextView>(R.id.tv_category_np)

        tvCategory.text = name
    }

    fun searchForNearBy(query: String, proximity: String, bbox: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val rv = persistentBottomSheet.findViewById<RecyclerView>(R.id.rv_bt_nearby_places)
            val call = service.getPlaces(
                query, proximity, bbox,10, true,
                true, true, "poi", getString(R.string.mapbox_access_token)
            )

            call.enqueue(object : Callback<Places> {
                override fun onResponse(call: Call<Places>, response: Response<Places>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val placesList: List<Feature> = it.features as List<Feature>
                            for (i in it.features) {
                                // Log.e(TAG, "onResponse: ${i.properties.name}")
                            }

                            //mapMarkersManager.showResults(placesList)


                            val placesListForRv: ArrayList<Feature> =
                                it.features as ArrayList<Feature>
                            val adapter =
                                NearbyPlacesAdapter2(placesListForRv, object : OnPlaceClickNP {
                                    override fun click(pos: Int) {
                                        //   setEvent(FirebaseEventConstants.choose_from_suggested_nearby_place)
                                        val intent = Intent(
                                            this@NearLocActivity,
                                            Navigate::class.java
                                        )
                                        intent.putExtra(
                                            "text",
                                            placesListForRv[pos].placeName
                                        )
                                        intent.putExtra(
                                            "address",
                                            placesListForRv[pos].placeName + " " + placesListForRv[pos].properties.address
                                        )
                                        intent.putExtra(
                                            "destinationLongitude",
                                            placesListForRv[pos].geometry.coordinates[0]
                                        )
                                        intent.putExtra(
                                            "destinationLatitude",
                                            placesListForRv[pos].geometry.coordinates[1]
                                        )
                                        startActivity(intent)
                                    }

                                })
                            rv.isNestedScrollingEnabled = true

                            rv.layoutManager = LinearLayoutManager(this@NearLocActivity)
                            rv.adapter = adapter

                            if (!isFinishing && !isDestroyed && alertDialog?.isShowing == true) {
                                alertDialog?.dismiss()
                            }
                            if (placesListForRv.isEmpty()) {
                                tvEmptyLocation.visibility = View.VISIBLE
                              //  Handler().postDelayed({finish()},2000)
                            } else {
                                tvEmptyLocation.visibility = View.GONE
                            }
                        }

                    }
                    else {
                        if (!isFinishing && !isDestroyed && alertDialog?.isShowing == true) {
                            alertDialog?.dismiss()
                        }
                        tvEmptyLocation.visibility = View.VISIBLE
                        tvEmptyLocation.text=resources.getString(R.string.Unexpected_error)
                        //Handler().postDelayed({finish()},2000)
                        Log.e("TAG", "onResponse: fail")
                    }
                }

                override fun onFailure(call: Call<Places>, t: Throwable) {
                    if (!isFinishing && !isDestroyed && alertDialog?.isShowing == true) {
                        alertDialog?.dismiss()
                    }
                    tvEmptyLocation.visibility = View.VISIBLE
                    tvEmptyLocation.text=resources.getString(R.string.Unexpected_error)
                   // Handler().postDelayed({finish()},2000)
                    Log.e("TAG", "onResponse: error ${t.message}")
                }
            })
        }

    }


}
    class NearbyPlacesAdapter2 (val list: ArrayList<Feature>, val onPlaceClickNP: OnPlaceClickNP) :
        RecyclerView.Adapter<NearbyPlacesAdapter2.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_nearby_places_item, parent, false)
            return ViewHolder(view)

        }

        override fun getItemCount(): Int {

            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val arrayLsit = list[position]
            holder.tvName.isSelected=true
            holder.tvName.text = arrayLsit.placeName
           // holder.tvAddress.text = arrayLsit.properties.address
            holder.itemView.setOnClickListener {
                holder.itemView.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                    holder.itemView.animate().scaleX(1f).scaleY(1f).duration=100
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    onPlaceClickNP.click(position)
                },200)

            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val tvName = view.findViewById<TextView>(R.id.tv_name)
            //val tvAddress = view.findViewById<TextView>(R.id.tv_adress)
        }
    }
