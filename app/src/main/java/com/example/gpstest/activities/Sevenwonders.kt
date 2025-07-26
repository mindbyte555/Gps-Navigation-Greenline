package com.example.gpstest.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpstest.AdsManager.AdIds.Companion.AdmobRecBannerNewId
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.MyApp.Companion.sevenRecenabled
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivitySevenwondersBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.sevenwonders
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView

class Sevenwonders : BaseActivity() {
    private var navigatingFromSevenWonders=false
    private var adView: AdView? = null
    lateinit var binding: ActivitySevenwondersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=ActivitySevenwondersBinding.inflate(layoutInflater)
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
        if (Prefutils(this@Sevenwonders).getBool("is_premium", false) || !isEnabled || !sevenRecenabled ) {
            binding.adLayout.visibility=View.GONE
        }
        else {

            if (isInternetAvailable(this)) {
                binding.adLayout.visibility=View.VISIBLE
                AdsManager.loadRecBannerAd(binding.adLayout,AdmobRecBannerNewId(),
                    this@Sevenwonders,
                    object : AdsManager.AdmobBannerAdListener {
                        override fun onAdFailed() {
                            binding.adLayout.visibility=View.INVISIBLE
                            Log.e("TEST TAG", "onAdFailed: Banner")
                        }

                        override fun onAdLoaded() {
                            Log.e("TEST TAG", "onAdLoaded: Banner")
                        }
                    })?.let { adView = it }
            }
        }

//        if (Prefutils(this@Sevenwonders).getBool("is_premium", false)|| !nativeEnabled || !isEnabled) {
//            binding.shimmer.visibility=View.GONE
//            binding.shimmer.hideShimmer()
//            binding.shimmer.stopShimmer()
//            binding.shimmer.setBackgroundColor(
//                ContextCompat.getColor(
//                    this@Sevenwonders, R.color.white
//                )
//            )
//        } else {
//            AdsManager.loadNative(
//                binding.nativeAd,
//                this@Sevenwonders,
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
        FirebaseCustomEvents(this).createFirebaseEvents(sevenwonders, "true")
        InfoUtil(this).setSystemBarsColor(R.attr.primarycolor)
        binding.constraintLayout.clickWithDebounce{
            binding.constraintLayout.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.constraintLayout.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(this, StreetViewMaplary::class.java)
                intent.putExtra("name","Great Wall of China")
                intent.putExtra("activity","seven")
                intent.putExtra("address","Great Wall of China, Huairou District, Beijing, China")
                intent.putExtra("url","https://www.mapillary.com/app/?lat=40.429326177147&lng=116.56681656383&z=19.9&pKey=1399852310394321&x=0.105279185064802&y=0.6026584831640263")
                startActivity(intent)
            },200)

        }
        binding.constraintLayout2.clickWithDebounce{
            binding.constraintLayout2.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.constraintLayout2.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(this, StreetViewMaplary::class.java)
                intent.putExtra("name","Chichén Itzá")
                intent.putExtra("activity","seven")
                intent.putExtra("address","Chichén Itzá, Tinum Municipality, Yucatán, Mexico")
                intent.putExtra("url","https://www.mapillary.com/app/?lat=20.6832647&lng=-88.5684728&z=17&pKey=300612364961064")
                startActivity(intent)
            },200)

        }
        binding.icBack.clickWithDebounce {
            finish()
        }
        binding.constraintLayout4.clickWithDebounce{
            binding.constraintLayout4.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.constraintLayout4.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(this, StreetViewMaplary::class.java)
                intent.putExtra("name","Machu Picchu")
                intent.putExtra("activity","seven")
                intent.putExtra("address","Machu Picchu, Urubamba Province, Cusco Region, Peru")
                intent.putExtra("url","https://www.mapillary.com/app/?lat=-13.163401610354&lng=-72.544692813806&z=17&pKey=4062815667132387")
                startActivity(intent)
            },200)

        }
        binding.constraintLayout5.clickWithDebounce{
            binding.constraintLayout5.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.constraintLayout5.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(this, StreetViewMaplary::class.java)
                intent.putExtra("name","Christ the Redeemer")
                intent.putExtra("activity","seven")
                intent.putExtra("address","Christ the Redeemer, Parque Nacional da Tijuca, Rio de Janeiro, Brazil")
                intent.putExtra("url","https://www.mapillary.com/app/?lat=-22.952305&lng=-43.210686944444&z=17&pKey=495070125259667")
                startActivity(intent)
            },200)

        }
        binding.constraintLayout3.clickWithDebounce{
            binding.constraintLayout3.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.constraintLayout3.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(this, StreetViewMaplary::class.java)
                intent.putExtra("name","Petra")
                intent.putExtra("activity","seven")
                intent.putExtra("address","Petra, Wadi Musa, Ma'an Governorate, Jordan")
                intent.putExtra("url","https://www.mapillary.com/app/?lat=30.329031&lng=35.444608972222&z=17&pKey=918756502241314")
                startActivity(intent)
            },200)
        }
        binding.constraintLayout6.clickWithDebounce{
            binding.constraintLayout6.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.constraintLayout6.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(this, StreetViewMaplary::class.java)
                intent.putExtra("name","Colosseum")
                intent.putExtra("activity","seven")
                intent.putExtra("address","Colosseum, Piazza del Colosseo, Rome, Italy")
                intent.putExtra("url","https://www.mapillary.com/app/?lat=41.890335844212&lng=12.49185585831&z=17&pKey=595012914873118")
                startActivity(intent)
            },200)

        }
        binding.constraintLayout66.clickWithDebounce{
            binding.constraintLayout66.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                binding.constraintLayout66.animate().scaleX(1f).scaleY(1f).duration=100
            }
            Handler(Looper.getMainLooper()).postDelayed({
                val intent=Intent(this, StreetViewMaplary::class.java)
                intent.putExtra("name","Taj Mahal")
                intent.putExtra("activity","seven")
                intent.putExtra("address","Taj Mahal, Dharmapuri, Forest Colony, Agra, Uttar Pradesh, India")
                intent.putExtra("url","https://www.mapillary.com/app/?lat=27.1746512&lng=78.0421153&z=17&pKey=466222209557203")
                startActivity(intent)
            },200)

        }
    }

    override fun onResume() {
        super.onResume()
        navigatingFromSevenWonders=false
    }
    override fun onPause() {
        super.onPause()
        if(!navigatingFromSevenWonders)
        {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.sevenwonders_disappear, "true")
        }
    }
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromSevenWonders = true
    }
}