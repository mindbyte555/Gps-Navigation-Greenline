package com.example.gpstest.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpstest.AdsManager.ActionOnAdClosedListener
import com.example.gpstest.AdsManager.AdIds
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.AdsManager.AdsManager.Companion.mInterstitialAd
import com.example.gpstest.AdsManager.InterstitialClass
import com.example.gpstest.AdsManager.InterstitialClass.interstitialAd
import com.example.gpstest.AdsManager.InterstitialClass.showAvailableInterstitial
import com.example.gpstest.MyApp.Companion.bannerRecEnabled
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.isInternetAvailable
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityLanguageScreenBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.lang_apply
import com.example.gpstest.utls.Constants.phoneSize
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.google.android.gms.ads.AdView
import java.util.Locale

class LanguageScreen : BaseActivity() {
    private var navigatingFromLanguage = false
    lateinit var prefUtil: Prefutils
    private var adView: AdView? = null
    private var alreadySelected = ""
    private var selectedLanguage: String = "en"
    lateinit var binding: ActivityLanguageScreenBinding
    private lateinit var languageButtons: List<View>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageScreenBinding.inflate(layoutInflater)
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
        FirebaseCustomEvents(this).createFirebaseEvents(customevents.language_launch, "true")
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!prefUtil.getBool("isFirstTime", true)) {
                    finish()
                }
            }
        })
        prefUtil = Prefutils(this)
        // if (!prefUtil.getBool("isFirstTime", true)){
        selectedLanguage = prefUtil.getString("selectedLanguage", "en") ?: "en"
        //}
        alreadySelected = selectedLanguage
        when (selectedLanguage) {
            "en" -> {

                binding.english.setBackgroundResource(R.drawable.selected_lang)
            }

            "ar" -> {

                binding.arbic.setBackgroundResource(R.drawable.selected_lang)
            }

            "fr" -> {

                binding.french.setBackgroundResource(R.drawable.selected_lang)
            }

            "it" -> {

                binding.italian.setBackgroundResource(R.drawable.selected_lang)
            }

            "ru" -> {

                binding.russian.setBackgroundResource(R.drawable.selected_lang)
            }

            "zh" -> {

                binding.china.setBackgroundResource(R.drawable.selected_lang)
            }

            "hi" -> {

                binding.hindi.setBackgroundResource(R.drawable.selected_lang)
            }

            "ur" -> {

                binding.urdu.setBackgroundResource(R.drawable.selected_lang)
            }


            "es" -> {

                binding.spanish.setBackgroundResource(R.drawable.selected_lang)
            }

            "tr" -> {

                binding.turkish.setBackgroundResource(R.drawable.selected_lang)
            }

            "pt" -> {

                binding.portegu.setBackgroundResource(R.drawable.selected_lang)
            }

            "th" -> {
                binding.thai.setBackgroundResource(R.drawable.selected_lang)
            }

            "vi" -> {
                binding.vietnamese.setBackgroundResource(R.drawable.selected_lang)
            }

            "uk" -> {
                binding.ukraine.setBackgroundResource(R.drawable.selected_lang)
            }

            "in" -> {
                binding.indonesia.setBackgroundResource(R.drawable.selected_lang)
            }

            "fa" -> {
                binding.persian.setBackgroundResource(R.drawable.selected_lang)
            }

            "ja" -> {
                binding.japan.setBackgroundResource(R.drawable.selected_lang)
            }

            "ko" -> {
                binding.korean.setBackgroundResource(R.drawable.selected_lang)
            }

            "de" -> {
                binding.german.setBackgroundResource(R.drawable.selected_lang)
            }

            "af" -> {
                binding.african.setBackgroundResource(R.drawable.selected_lang)
            }
        }
        if (Prefutils(this@LanguageScreen).getBool(
                "is_premium",
                false
            ) || !bannerRecEnabled || !isEnabled
        ) {
            binding.adLayout.visibility = View.GONE
            binding.adNativeLayout.visibility = View.GONE
        } else {
            if (isInternetAvailable(this)) {
                if (phoneSize <= 5.5) {
                    binding.adLayout.visibility = View.GONE
                    binding.adNativeLayout.visibility = View.VISIBLE
                    AdsManager.loadNative(
                        binding.adNativeLayout,
                        this@LanguageScreen,
                        object : AdsManager.AdmobBannerAdListener {
                            override fun onAdLoaded() {
                                binding.shimmer.hideShimmer()
                                binding.shimmer.stopShimmer()
                                binding.shimmer.setBackgroundResource(R.color.white)
                            }

                            override fun onAdFailed() {
                                binding.shimmer.hideShimmer()
                                binding.shimmer.stopShimmer()
                                binding.shimmer.setBackgroundResource(R.drawable.rounded_with_gray_light)
                            }
                        }, AdsManager.NativeAdType.SMALL
                    )
                } else {
                    binding.adLayout.visibility = View.VISIBLE
                    binding.adNativeLayout.visibility = View.GONE
                    AdsManager.loadRecBannerAd(binding.adLayout, AdIds.AdmobRecBannerId(),
                        this@LanguageScreen,
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

        languageButtons = listOf(
            binding.english,
            binding.arbic,
            binding.china,
            binding.french,
            binding.hindi,
            binding.italian,
            binding.portegu,
            binding.russian,
            binding.spanish,
            binding.turkish,
            binding.urdu,
            binding.thai,
            binding.vietnamese,
            binding.ukraine,
            binding.indonesia,
            binding.persian,
            binding.japan,
            binding.korean,
            binding.german,
            binding.african
        )
        binding.english.setOnClickListener { selectLanguage(binding.english, "en") }
        binding.arbic.setOnClickListener { selectLanguage(binding.arbic, "ar") }
        binding.china.setOnClickListener { selectLanguage(binding.china, "zh") }
        binding.french.setOnClickListener { selectLanguage(binding.french, "fr") }
        binding.hindi.setOnClickListener { selectLanguage(binding.hindi, "hi") }
        binding.italian.setOnClickListener { selectLanguage(binding.italian, "it") }
        binding.portegu.setOnClickListener { selectLanguage(binding.portegu, "pt") }
        binding.russian.setOnClickListener { selectLanguage(binding.russian, "ru") }
        binding.spanish.setOnClickListener { selectLanguage(binding.spanish, "es") }
        binding.turkish.setOnClickListener { selectLanguage(binding.turkish, "tr") }
        binding.urdu.setOnClickListener { selectLanguage(binding.urdu, "ur") }
        binding.thai.setOnClickListener { selectLanguage(binding.thai, "th") }
        binding.vietnamese.setOnClickListener { selectLanguage(binding.vietnamese, "vi") }
        binding.ukraine.setOnClickListener { selectLanguage(binding.ukraine, "uk") }
        binding.indonesia.setOnClickListener { selectLanguage(binding.indonesia, "in") }
        binding.persian.setOnClickListener { selectLanguage(binding.persian, "fa") }
        binding.japan.setOnClickListener { selectLanguage(binding.japan, "ja") }
        binding.korean.setOnClickListener { selectLanguage(binding.korean, "ko") }
        binding.german.setOnClickListener { selectLanguage(binding.german, "de") }
        binding.african.setOnClickListener { selectLanguage(binding.african, "af") }




        binding.btnSave.setOnClickListener {
            FirebaseCustomEvents(this).createFirebaseEvents(lang_apply, "true")
            prefUtil.setString("selectedLanguage", selectedLanguage)
            if (prefUtil.getBool("isFirstTime", true)) {
                InterstitialClass.requestInterstitial(
                    this@LanguageScreen,
                    this@LanguageScreen,
                    "language",
                    object : ActionOnAdClosedListener {
                        override fun ActionAfterAd() {
                            val intent =
                                Intent(this@LanguageScreen, InApp_Purchase_Screen::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                )
            } else {
                if (alreadySelected == selectedLanguage) {
                    finish()
                } else {
                    if (mInterstitialAd != null) {
                        AdsManager.showInterstitial(
                            false,
                            this@LanguageScreen,
                            object : AdsManager.InterstitialAdListener {
                                override fun onAdClosed() {
                                    val intent = Intent(
                                        this@LanguageScreen,
                                        InApp_Purchase_Screen::class.java
                                    ).apply {
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        putExtra("refresh", true)
                                        putExtra("language", selectedLanguage)
                                    }
                                    startActivity(intent)
                                    finish()
                                }

                            }, "language"
                        )
                    } else {
                        if (interstitialAd != null) {
                            showAvailableInterstitial(this@LanguageScreen) {
                                val intent = Intent(
                                    this@LanguageScreen,
                                    InApp_Purchase_Screen::class.java
                                ).apply {
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    putExtra("refresh", true)
                                    putExtra("language", selectedLanguage)
                                }
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            InterstitialClass.requestInterstitial(
                                this@LanguageScreen,
                                this@LanguageScreen,
                                "lang",
                                object : ActionOnAdClosedListener {
                                    override fun ActionAfterAd() {
                                        val intent = Intent(
                                            this@LanguageScreen,
                                            InApp_Purchase_Screen::class.java
                                        ).apply {
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                            putExtra("refresh", true)
                                            putExtra("language", selectedLanguage)
                                        }
                                        startActivity(intent)
                                        finish()
                                    }
                                }

                            )
                        }
                    }
                }
            }
        }


    }

    private fun selectLanguage(selectedButton: View, langCode: String) {
        languageButtons.forEach { it.setBackgroundResource(R.drawable.unsel_language) }
        selectedButton.setBackgroundResource(R.drawable.selected_lang)
        selectedLanguage = langCode

        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        val newContext = createConfigurationContext(config)

        // Apply translated text from the new context
        binding.btnSave.text = newContext.resources.getString(R.string.apply_language)
    }


    override fun onPause() {
        super.onPause()
        if (!navigatingFromLanguage) {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.language_disappear, "true")
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromLanguage = true
    }
}
