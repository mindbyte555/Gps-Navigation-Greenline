package com.example.gpstest.activities

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.CurrentLocation.Companion.latitude
import com.example.gpstest.CurrentLocation.Companion.longitude
import com.example.gpstest.MyApp
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityStreetviewMaplaryBinding
import com.example.gpstest.utls.InfoUtil

class StreetViewMaplary : BaseActivity() {
    lateinit var binding: ActivityStreetviewMaplaryBinding
    private var alertDialog: AlertDialog? = null
    val tag: String = javaClass.simpleName
    var name=" Street view"
    private var url = "https://www.mapillary.com/app/?lat=$latitude&lng=$longitude&z=18.5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreetviewMaplaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(!MyApp.interstitialMappilaryEnabled){
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        finish()
                    }
                    return
                }
                if (mInterstitialAd != null) {
                    AdsManager.showInterstitial(
                        true, this@StreetViewMaplary, object : AdsManager.InterstitialAdListener {
                            override fun onAdClosed() {
                                if (binding.webView.canGoBack()) {
                                    binding.webView.goBack()
                                } else {
                                    finish()
                                }
                            }
                        }, "NearbyPlaces_activity_Back"
                    )
                } else {
                    if (interstitialAd != null) {
                        showAvailableInterstitial(this@StreetViewMaplary) {
                            if (binding.webView.canGoBack()) {
                                binding.webView.goBack()
                            } else {
                                finish()
                            }
                        }
                    } else {
                        InterstitialClass.requestInterstitial(this@StreetViewMaplary,
                            this@StreetViewMaplary,
                            "NearbyPlaces_activity_Back",
                            object : ActionOnAdClosedListener {
                                override fun ActionAfterAd() {
                                    if (binding.webView.canGoBack()) {
                                        binding.webView.goBack()
                                    } else {
                                        finish()
                                    }
                                }
                            })
                    }
                }

            }
        })

        InfoUtil(this).setSystemBarsColor(R.attr.primarycolor)
        if (intent != null) {
            name = intent.getStringExtra("name").toString()
            binding.title.text = name
            url =intent.getStringExtra("url").toString()
        }
        //showDialog()
        showStreets(url)
        clickListeners()
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun showStreets(url: String) {
        val webView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.zoomIn()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
            //to hide location button and icon and notification
            override fun onPageFinished(view: WebView, url: String) {
                webView.evaluateJavascript(
                    """
                (function() {   
                      var locationButtonParent = document.querySelector('.LocationButton');
                    if (locationButtonParent) {
                        locationButtonParent.style.display = "none";
                    }
                      var attributionButton = document.querySelector('.IconContainer[dropup-control][for="attribution"]');
                    if (attributionButton) {
                        attributionButton.style.display = "none";
                    }
                      var appPromo = document.querySelector('div.fixed.bottom-0.left-0.right-0.flex.items-center.p1.h5.bg-white');
                 if (appPromo) {
               appPromo.style.display = 'none';
                }
       
                })();
                """.trimIndent()
                ) { }


                if (alertDialog != null && alertDialog!!.isShowing) {
                    alertDialog?.dismiss()
                }
            }
        }
        if (!isInternetAvailable(this@StreetViewMaplary)) {
            Toast.makeText(this, "no internet", Toast.LENGTH_SHORT).show()
        }
        webView.loadUrl(url)
    }
    @SuppressLint("ServiceCast")
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
    private fun clickListeners() {
        binding.icBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}