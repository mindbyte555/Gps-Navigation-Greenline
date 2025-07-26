package com.example.gpstest.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetailsParams
import com.example.gpstest.MyApp.Companion.clickWithDebounce
import com.example.gpstest.R
import com.example.gpstest.ViewPagerAdapter
import com.example.gpstest.databinding.ActivityInAppPurchaseScreenBinding
import com.example.gpstest.firebase.FirebaseCustomEvents
import com.example.gpstest.firebase.customevents
import com.example.gpstest.subscription.GooglePlayBuySubscription
import com.example.gpstest.subscription.SubscriptionPurchaseInterface
import com.example.gpstest.utls.GradientAnimator
import com.example.gpstest.utls.InfoUtil
import com.example.gpstest.utls.Prefutils
import com.example.gpstest.utls.ViewPagerItem
import com.google.common.collect.ImmutableList

class InApp_Purchase_Screen : BaseActivity(), SubscriptionPurchaseInterface {
    lateinit var binding: ActivityInAppPurchaseScreenBinding

    //   private lateinit var leftRightAnimation: ObjectAnimator
    private var navigatingFromPremium = false
    lateinit var items: List<ViewPagerItem>
    private var startA = false
    private var check: String = ""
    private var oneMonth: String = "01_month"
    var yearly: String = "01_year"
    var weekly: String = "01_week"
    private var billingClient: BillingClient? = null
    private var productDetailsList: ArrayList<ProductDetails>? = ArrayList()
    var retryCount = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInAppPurchaseScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        GradientAnimator.start(binding.main, this)
        if (Prefutils(this).getBool("is_premium", true)) {
            binding.textView8.visibility = View.VISIBLE
            binding.cancelTv.visibility = View.VISIBLE
        }
        binding.termsTv.clickWithDebounce {
            Log.d("btn", "clicked: ")
            InfoUtil(this@InApp_Purchase_Screen).openGooglePrivacy()
        }
        binding.cancelTv.clickWithDebounce {
            Log.d("btn", "clicked: ")
            InfoUtil(this@InApp_Purchase_Screen).openSubscriptionPage()
        }
        binding.privacyTv.clickWithDebounce {
            Log.d("btn", "clicked: ")
            InfoUtil(this@InApp_Purchase_Screen).openPrivacy()
        }
//        if (Prefutils(this).getBool("isFirstTime",true)) {
//            AdsManager.loadInterstitial(this)
//        }
        // Apply system window insets listener to adjust padding dynamically
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
        FirebaseCustomEvents(this).createFirebaseEvents(customevents.premmium_launched, "true")
        if (intent != null) {
            startA = intent.getBooleanExtra("startActivity", false)
        }

        items = listOf(
            ViewPagerItem(
                R.drawable.route_finder11,
            ),
            ViewPagerItem(
                R.drawable.nearby_places11,

                ),
            ViewPagerItem(
                R.drawable.compass11,

                ),
            ViewPagerItem(
                R.drawable.my_location11,

                ),
            ViewPagerItem(
                R.drawable.satellite_map11,
            ),

            ViewPagerItem(
                R.drawable.street_view11,
            ),
            ViewPagerItem(
                R.drawable.voice_navigation11,
            ),
            ViewPagerItem(
                R.drawable.seven_wonders11,

                )
        )
//        leftRightAnimation = ObjectAnimator.ofPropertyValuesHolder(
//            binding.btnBuy,
//            PropertyValuesHolder.ofFloat("scaleX", 0.85f, 1.02f),
//            PropertyValuesHolder.ofFloat("scaleY", 0.85f, 1.02f)
//        ).apply {
//            duration = 400
//            repeatCount = ObjectAnimator.INFINITE
//            repeatMode = ObjectAnimator.REVERSE
//            interpolator = DecelerateInterpolator() // Smooth transition
//        }
//        leftRightAnimation.start()

//        bounceanimation = ObjectAnimator.ofPropertyValuesHolder(
//            binding.cancelbtn,
//            PropertyValuesHolder.ofFloat("scaleX", 0.95f, 1.08f),
//            PropertyValuesHolder.ofFloat("scaleY", 0.95f, 1.08f)
//        ).apply {
//            duration = 400
//            repeatCount = ObjectAnimator.INFINITE
//            repeatMode = ObjectAnimator.REVERSE
//            interpolator = DecelerateInterpolator() // Smooth transition
//        }
//        bounceanimation.start()

        binding.viewpager.adapter = ViewPagerAdapter(items)
        setupViewPager()
        startAutoSlide()

        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        verifySubPurchase(purchase)
                    }
                }
            }.build()
        establishConnection()
        initialization()
        GooglePlayBuySubscription.purchasesInterface = this
        if (!GooglePlayBuySubscription.mSkuDetailsList.isNullOrEmpty()) {
            GooglePlayBuySubscription.mSkuDetailsList?.let {
                if (it.size > 0) {
                    val priceString = it[0].price.convertArabicToEnglishDigits()
                    val numericPrice = priceString.replace(Regex("[^\\d.]"), "")
//                    binding.textCurrency.text = "${it[0].priceCurrencyCode}/Month"
                    binding.OneMonthPrice.text = "${it[0].priceCurrencyCode} ${numericPrice}"
                }
                if (it.size > 1) {
                    val priceString = it[1].price.convertArabicToEnglishDigits()
                    val numericPrice = priceString.replace(Regex("[^\\d.]"), "")
//                    binding.textCurrency.text = "${it[0].priceCurrencyCode}/Month"
                    binding.weekPrice.text = "${it[1].priceCurrencyCode} ${numericPrice}"
                }
                if (it.size > 2) {
                    try {
                        val priceString = it[2].price.convertArabicToEnglishDigits()
                        //   val priceString = "Rs ٢٠٥٠٫٠٠".convertArabicToEnglishDigits()
                        Log.d("TAGEE", "onCreate:$priceString ")
                        val stringPrice = priceString.replace(Regex("[^\\d.]"), "")
                        val cleanPrice = stringPrice.substringBefore('.')

                        val numericPrice = cleanPrice.toInt()
                        val weeklyPrice = (numericPrice / 52)

                        binding.yearlyPrice.text = "${it[2].priceCurrencyCode} $stringPrice"
                        binding.weektxtyear.text = "${it[2].priceCurrencyCode} $weeklyPrice / Week"
                    } catch (_: Exception) {

                    }
                }

            }
        }

    }

    fun String.convertArabicToEnglishDigits(): String {
        return this.flatMap {
            when (it) {
                '٠' -> listOf('0')
                '١' -> listOf('1')
                '٢' -> listOf('2')
                '٣' -> listOf('3')
                '٤' -> listOf('4')
                '٥' -> listOf('5')
                '٦' -> listOf('6')
                '٧' -> listOf('7')
                '٨' -> listOf('8')
                '٩' -> listOf('9')
                '٫' -> listOf('.')  // Arabic decimal to dot
                ',' -> emptyList()  // Remove commas entirely
                else -> listOf(it)
            }
        }.joinToString("")
    }

    override fun onDestroy() {
        super.onDestroy()
        //  leftRightAnimation.cancel()
    }

    private fun startAutoSlide() {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                val nextItem = binding.viewpager.currentItem + 1

                // Pause for 1 second before moving
                handler.postDelayed({
                    if (nextItem < items.size) {
                        smoothScrollToItem(nextItem, 500) // Move slowly over 1 seconds
                    } else {
                        // When reaching the last item, smoothly reset to the first
                        smoothScrollToItem(0, 40)
                    }
                }, 100) // Wait 1 second

                handler.postDelayed(this, 1100) // Total delay: 1s wait + ~1s transition
            }
        }

        handler.postDelayed(runnable, 500) // Initial delay before starting
    }

    // Custom method to control the speed of scrolling
    private fun smoothScrollToItem(position: Int, duration: Int) {
        try {
            val recyclerView = binding.viewpager.getChildAt(0) as RecyclerView
            val smoothScroller = object : LinearSmoothScroller(binding.viewpager.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return duration.toFloat() / displayMetrics.densityDpi // Slower scroll
                }
            }
            smoothScroller.targetPosition = position
            recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
        } catch (e: Exception) {
            binding.viewpager.setCurrentItem(position, true) // Fallback to default smooth scroll
        }
    }

    private fun setupViewPager() {
        binding.viewpager.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3 // Load extra pages

            // Calculate padding dynamically (10% of screen width)
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val padding = (screenWidth * 0.28).toInt() // 10% of screen width

            setPadding(padding, 0, padding, 0) // Apply calculated padding

            setPageTransformer { page, position ->
                val minScale = 0.8f
                val maxScale = 1.0f
                val scaleFactor = minScale + (1 - Math.abs(position)) * (maxScale - minScale)

                // Apply scaling smoothly
                page.scaleX = scaleFactor
                page.scaleY = scaleFactor
            }
        }
    }
//


    override fun productPurchasedSuccessful() {
        Prefutils(this).setBool("is_premium", true)
//        if (newUi) {
        startActivity(Intent(applicationContext, StartTwo::class.java).putExtra("review", true))
        finish()
//        } else {
//            startActivity(
//                Intent(applicationContext, StartActivity::class.java).putExtra(
//                    "review",
//                    true
//                )
//            )
//            finish()
//        }

    }

    override fun productPurchaseFailed() {
    }

    private fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    getProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retryCount <= 3) establishConnection()
                retryCount++
            }
        })
    }

    private fun verifySubPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams =
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient!!.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                Prefutils(this).setBool("is_premium", true)

            }
        }
    }

    private fun getProducts() {
        //for getting the pricing from console
        val skuList: MutableList<String> = java.util.ArrayList()
        skuList.add("01_week")
        skuList.add("01_month")
        skuList.add("01_year")


        val param2 = SkuDetailsParams.newBuilder()
        param2.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        billingClient!!.querySkuDetailsAsync(
            param2.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.e("TESTTAG", "CALLED SIZE ${skuDetailsList}")
//                if (skuDetailsList != null && skuDetailsList.size > 0) {
//                    binding.textCurrency.text = "${skuDetailsList[0].priceCurrencyCode}/Month"
//                    binding.textPrice.text = "${skuDetailsList[0].price}"
//                }
//                if (skuDetailsList != null && skuDetailsList.size > 1) {
//                    binding.textPriceSixMonth.text = skuDetailsList[1].price
//                    binding.textSixCurrency.text = "${skuDetailsList[1].priceCurrencyCode}/6 Month"
//                }
            } else {
                Toast.makeText(this, " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        }


        val productList = ImmutableList.of(
            //Product 1
            QueryProductDetailsParams.Product.newBuilder().setProductId("01_week")
                .setProductType(BillingClient.ProductType.SUBS).build(),
            //Product 2
            QueryProductDetailsParams.Product.newBuilder().setProductId("01_month")
                .setProductType(BillingClient.ProductType.SUBS).build(),
            // Product 3
            QueryProductDetailsParams.Product.newBuilder().setProductId("01_year")
                .setProductType(BillingClient.ProductType.SUBS).build()

//            ,
//

//            QueryProductDetailsParams.Product.newBuilder()
//                .setProductId("")
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient!!.queryProductDetailsAsync(params) { billingResult: BillingResult?, prodDetailsList: List<ProductDetails>? ->
            prodDetailsList?.forEach {
                productDetailsList?.add(it)
            }
//            prodDetailsList?.let { productDetailsList?.addAll(it) }
        }
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        assert(productDetails.subscriptionOfferDetails != null)
        val productDetailsParamsList = ImmutableList.of(
            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken).build()
        )
        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                .build()
        val billingResult =
            billingClient!!.launchBillingFlow(this@InApp_Purchase_Screen!!, billingFlowParams)
    }

    override fun onResume() {
        super.onResume()
        billingClient!!.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        verifySubPurchase(purchase)
                    }
                }
            }
        }
    }

    private fun initialization() {
        check = yearly
        binding.btnconsmonth.setOnClickListener {
            binding.btnconsmonth.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.btnconsmonth.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            check = oneMonth

            binding.btnconsyear.setBackgroundDrawable(this.resources.getDrawable(R.drawable.unsel_perm))
            binding.btnconsweek.setBackgroundDrawable(this.resources.getDrawable(R.drawable.unsel_perm))
            binding.btnconsmonth.setBackgroundDrawable(this.resources.getDrawable(R.drawable.sel_perm))
//            binding.monthtxt.setTextColor(Color.WHITE)
//            binding.yeartxt.setTextColor(Color.BLACK)
//            binding.OneMonthPrice.setTextColor(Color.WHITE)
//            binding.yearlyPrice.setTextColor(Color.BLACK)
//            binding.weekPrice.setTextColor(Color.BLACK)
//            binding.weektxt.setTextColor(Color.BLACK)
            //   binding.btnContinueWithAdsTv.text = resources.getString(R.string.continue_with_monthly)


        }

        binding.btnconsyear.setOnClickListener {
            binding.btnconsyear.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.btnconsyear.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            binding.besttag.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.besttag.animate().scaleX(1f).scaleY(1f).duration = 100
                }
            check = yearly

            binding.btnconsyear.setBackgroundDrawable(this.resources.getDrawable(R.drawable.sel_perm))
            binding.btnconsmonth.setBackgroundDrawable(this.resources.getDrawable(R.drawable.unsel_perm))
            binding.btnconsweek.setBackgroundDrawable(this.resources.getDrawable(R.drawable.unsel_perm))

            //     binding.btnContinueWithAdsTv.text = resources.getString(R.string.continue_with_yearly)
//            binding.OneMonthPrice.setTextColor(Color.BLACK)
//            binding.yearlyPrice.setTextColor(Color.WHITE)
//            binding.monthtxt.setTextColor(Color.BLACK)
//            binding.yeartxt.setTextColor(Color.WHITE)
//            binding.weekPrice.setTextColor(Color.BLACK)
//            binding.weektxt.setTextColor(Color.BLACK)
        }
        binding.btnconsweek.setOnClickListener {
            binding.btnconsweek.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    binding.btnconsweek.animate().scaleX(1f).scaleY(1f).duration = 100
                }

            check = weekly

            binding.btnconsweek.setBackgroundDrawable(this.resources.getDrawable(R.drawable.sel_perm))
            binding.btnconsmonth.setBackgroundDrawable(this.resources.getDrawable(R.drawable.unsel_perm))
            binding.btnconsyear.setBackgroundDrawable(this.resources.getDrawable(R.drawable.unsel_perm))

            //   binding.btnContinueWithAdsTv.text = resources.getString(R.string.continue_with_weekly)
//            binding.OneMonthPrice.setTextColor(Color.BLACK)
//            binding.weekPrice.setTextColor(Color.WHITE)
//            binding.weektxt.setTextColor(Color.WHITE)
//            binding.yearlyPrice.setTextColor(Color.BLACK)
//            binding.monthtxt.setTextColor(Color.BLACK)
//            binding.yeartxt.setTextColor(Color.BLACK)
        }


        binding.btnBuy.setOnClickListener {
            if (productDetailsList?.size!! > 0) {
                if (check == oneMonth) {
                    Toast.makeText(this, " 1 Month plan selected", Toast.LENGTH_SHORT).show()
                    launchPurchaseFlow(productDetailsList!![0])
                } else if (check == yearly) {
                    Toast.makeText(this, " Yearly plan selected", Toast.LENGTH_SHORT).show()
                    launchPurchaseFlow(productDetailsList!![2])
                } else {
                    Toast.makeText(this, " Weekly plan selected", Toast.LENGTH_SHORT).show()
                    launchPurchaseFlow(productDetailsList!![1])
                }
            }
        }
//            skip btn
//        Handler(Looper.getMainLooper()).postDelayed({
//            binding.cancelbtn.visibility= View.VISIBLE
//        },3000)
        binding.cancelbtn.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (startA) {
            finish()
            super.onBackPressed()

        } else {
//            if (newUi) {
            val intent = Intent(this@InApp_Purchase_Screen, StartTwo::class.java)
            startActivity(intent)
            finish()
//            } else {
//                val intent = Intent(this@InApp_Purchase_Screen, StartActivity::class.java)
//                startActivity(intent)
//                finish()
//            }

        }

    }

    override fun onPause() {
        super.onPause()
        if (!navigatingFromPremium) {
            FirebaseCustomEvents(this).createFirebaseEvents(customevents.premimum_disappear, "true")
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        navigatingFromPremium = true
    }
}