package com.example.gpstest.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import com.example.gpstest.AdsManager.AdsManager
import com.example.gpstest.MyApp.Companion.clickWithDebounce2
import com.example.gpstest.MyApp.Companion.isEnabled
import com.example.gpstest.MyApp.Companion.lan_inter_enable
import com.example.gpstest.MyApp.Companion.nativeEnabled
import com.example.gpstest.MyApp.Companion.templateStr
import com.example.gpstest.R
import com.example.gpstest.databinding.ActivityOnbBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.firebase.customevents.Companion.AI_Bot_sendbtn
import com.example.gpstest.firebase.customevents.Companion.backpress_from_boarding
import com.example.gpstest.firebase.customevents.Companion.letsgo_boarding_clicked
import com.example.gpstest.firebase.customevents.Companion.onb_next_clicked
import com.example.gpstest.firebase.customevents.Companion.onboarding
import com.example.gpstest.pagerAdapter
import com.example.gpstest.utls.Constants.getScreenSizeInInches
import com.example.gpstest.utls.Constants.phoneSize
import com.example.gpstest.utls.Prefutils

class
OnbActivity : BaseActivity() {
    private var navigatingFromOnboarding = false
    lateinit var binding: ActivityOnbBinding
    private lateinit var dots: Array<ImageView?>
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val nextItem = (binding.viewpager.currentItem + 1) % binding.viewpager.adapter!!.count
            binding.viewpager.setCurrentItem(nextItem, true)
            autoScrollHandler.postDelayed(this, 2000) // Change page every 3 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnbBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val statusBarInsets =
                insets.getInsets(WindowInsetsCompat.Type.statusBars()) // Status Bar Insets
            val navigationBarInsets =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()) // Navigation Bar Insets

            // ✅ Apply bottom padding only if running on Android 14+
            val bottomPadding = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                navigationBarInsets.bottom // Proper bottom padding for Android 14+
            } else {
                0 // No bottom padding for older versions
            }

            v.setPadding(
                statusBarInsets.left,
                statusBarInsets.top,
                statusBarInsets.right,
                bottomPadding
            )
            insets
        }
        phoneSize = getScreenSizeInInches(this)
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
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                FirebaseCustomEvents(this@OnbActivity).createFirebaseEvents(
                    backpress_from_boarding,
                    "true"
                )
                startActivity(Intent(this@OnbActivity, LanguageScreen::class.java))
                finish()
            }
        })
        if (Prefutils(this@OnbActivity).getBool(
                "is_premium",
                false
            ) || !nativeEnabled || !isEnabled
        ) {
            binding.shimmer.visibility = View.GONE
            binding.shimmer.hideShimmer()
            binding.shimmer.stopShimmer()
            binding.shimmer.setBackgroundColor(
                ContextCompat.getColor(
                    this@OnbActivity, R.color.white
                )
            )
        } else {
//            val nativeType = try {
//                AdsManager.NativeAdType.valueOf(templateStr)
//            } catch (e: Exception) {
//                AdsManager.NativeAdType.MEDIUM
//            }
            if (phoneSize <= 5.5) {
                Log.e("ONB", "onCreate phoneSize if: $phoneSize")
                AdsManager.loadNative(
                    binding.nativeAd,
                    this@OnbActivity,
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
                Log.e("ONB", "onCreate phoneSize else: $phoneSize")
                AdsManager.loadNative(
                    binding.nativeAd,
                    this@OnbActivity,
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
                    }, AdsManager.NativeAdType.MEDIUM
                )
            }
        }
        FirebaseCustomEvents(this).createFirebaseEvents(onboarding, "true")

        binding.viewpager.adapter = pagerAdapter(supportFragmentManager)
        initializeDots()
        updateIndicator(0)
        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

                when (binding.viewpager.currentItem) {
                    0 -> {
                        updateIndicator(0)
                    }

                    1 -> {
                        updateIndicator(1)
                    }

                    2 -> {
                        updateIndicator(2)
                    }
                }
            }

            override fun onPageSelected(p0: Int) {}
            override fun onPageScrollStateChanged(p0: Int) {}
        })
//        binding.buttonSkip.clickWithDebounce {
//            startActivity(Intent(this, language_screen::class.java))
//            finish()
//        }
        binding.buttonNext.clickWithDebounce2 {

            binding.buttonNext.animate()
                .scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                    binding.buttonNext.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            Log.e("clicked", "1")
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LanguageScreen::class.java))
                finish()
            }, 200)

        }

    }

    private fun initializeDots() {
        dots = arrayOfNulls(3)
        for (i in dots.indices) {
            dots[i] = ImageView(this)
            val widthHeight = resources.getDimensionPixelSize(R.dimen.dotsize)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams(widthHeight, widthHeight))
            params.setMargins(
                resources.getDimensionPixelSize(R.dimen.dotmargin),
                0,
                resources.getDimensionPixelSize(R.dimen.dotmargin),
                0
            )
            dots[i]?.layoutParams = params
            dots[i]?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_dot))
            binding.indicatorLayout.addView(dots[i])
        }
    }

    private fun updateIndicator(position: Int) {
        for (i in dots.indices) {
            if (i == position) {
                dots[i]?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.active_indicator
                    )
                )
            } else {
                dots[i]?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_dot))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!navigatingFromOnboarding) {
            FirebaseCustomEvents(this).createFirebaseEvents(
                customevents.onboarding_disappear,
                "true"
            )
        }
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onResume() {
        super.onResume()
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromOnboarding = true
    }
}