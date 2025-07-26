package com.example.gpstest.nearby_places

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.gpstest.AdsManager.AdIds.Companion.AdmobRecBannerNewId
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.activities.BaseActivity
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.nearRecEnabled
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityNearByplacesBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.nearby
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView

class NearByplacesActivity : BaseActivity() {
    private var adView: AdView? = null
    lateinit var binding: ActivityNearByplacesBinding
    var placesList: ArrayList<NearbyPlacesModel> = ArrayList()
    var navigatingFromnearby = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearByplacesBinding.inflate(layoutInflater)
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
        InfoUtil(this).setSystemBarsColor(R.attr.backgroundColor)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        if (Prefutils(this@NearByplacesActivity).getBool("is_premium", false)|| !isEnabled || !nearRecEnabled) {
            binding.adLayout.visibility=View.GONE
            Log.e("TEST TAG", "onCreate: in if")
        }
        else {
            binding.adLayout.visibility=View.VISIBLE
            if (isInternetAvailable(this)) {
                AdsManager.loadRecBannerAd(binding.adLayout,AdmobRecBannerNewId(),
                    this@NearByplacesActivity,
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
//        if (Prefutils(this@NearByplacesActivity).getBool("is_premium", false)|| !nativeEnabled || !isEnabled) {
//            binding.shimmer.visibility= View.GONE
//            binding.shimmer.hideShimmer()
//            binding.shimmer.stopShimmer()
//            binding.shimmer.setBackgroundColor(
//                ContextCompat.getColor(
//                    this@NearByplacesActivity, R.color.white
//                )
//            )
//        } else {
//            AdsManager.loadNative(
//                binding.nativeAd,
//                this@NearByplacesActivity,
//                object : AdsManager.AdmobBannerAdListener {
//                    override fun onAdLoaded() {
//                        binding.shimmer.hideShimmer()
//                        binding.shimmer.stopShimmer()
//                        binding.shimmer.setBackgroundResource(R.color.white)
//                    }
//
//                    override fun onAdFailed() {
//                        binding.shimmer.hideShimmer()
//                        binding.shimmer.stopShimmer()
//                        binding.shimmer.setBackgroundResource(R.drawable.rounded_with_gray_light)
//                    }
//                }, AdsManager.NativeAdType.MEDIUM )
//        }
        FirebaseCustomEvents(this).createFirebaseEvents(nearby, "true")
        InfoUtil(this).setSystemBarsColor(R.attr.primarycolor)
        initData()
        setAdapter()
        clickListeners()


    }

    private fun clickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setAdapter() {
        val adapter = NearbyPlacesAdapter(this, placesList, object : OnItemClick {
            override fun onClick(name: Int) {
                //setEvent(FirebaseEventConstants.select_nearby_place)
                val intent = Intent(this@NearByplacesActivity, NearLocActivity::class.java)
                intent.putExtra("name", placesList[name].type)
                intent.putExtra("type", placesList[name].name)
                startActivity(intent)
            }
        })

        binding.rvNearbyPlacesF.layoutManager = GridLayoutManager(this,2)
        binding.rvNearbyPlacesF.adapter = adapter
    }

    private fun initData() {
        placesList = getNearByPlaces()
    }

    override fun onResume() {
        super.onResume()
        navigatingFromnearby=false
    }
    override fun onPause() {
        super.onPause()
        if(!navigatingFromnearby)
        {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.nearby_disappear, "true")
        }
    }
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromnearby = true
    }

}