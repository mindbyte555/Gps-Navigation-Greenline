package com.example.gpstest.subscription

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponseCode.DEVELOPER_ERROR
import com.android.billingclient.api.BillingClient.BillingResponseCode.ERROR
import com.android.billingclient.api.BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_NOT_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_TIMEOUT
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.example.gpstest.utls.Prefutils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GooglePlayBuySubscription {
    companion object {
        val TAG = "GPlayBuySubscription"
        var purchasesInterface: SubscriptionPurchaseInterface? = null
        private var billingClient: BillingClient? = null
        private var mBillingResult: BillingResult? = null
        var mSkuDetailsList: MutableList<SkuDetails>? = null

        var mSharedPrefHelper: SharedPreferencesClass? = null
        var prefUtil: Prefutils? = null

        fun initBillingClient(activity: Activity) {

            mSharedPrefHelper = SharedPreferencesClass(activity)
            prefUtil = Prefutils(activity)
            if (billingClient == null) {
                billingClient = BillingClient.newBuilder(activity)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build()
            }

        }

        fun makeGooglePlayConnectionRequest() {
            billingClient?.startConnection(billingClientStateListener)
        }

        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases: MutableList<Purchase>? ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

                    Log.d(TAG, "purchasing:$purchases")



                    for (purchase in purchases) {
                        acknowledgePurchase(purchase)
                    }


                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {

                    Log.d(TAG, "Cancel:" + billingResult.responseCode)
                    purchasesInterface?.productPurchaseFailed()

                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {

                    Log.d(TAG, "ITEM_ALREADY_OWNED:" + billingResult.responseCode)
                    mSharedPrefHelper!!.setBooleanPreferences(
                        mSharedPrefHelper!!.IS_SUBSCRIBED,
                        true
                    )

                    /*mSharedPrefHelper!!.setBooleanPreferences(
                        mSharedPrefHelper!!.REMOVE_ADS_KEY,
                        true
                    )*/

                    purchasesInterface?.productPurchasedSuccessful()


                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                    Log.d(TAG, "Error:" + billingResult.responseCode)

                    mSharedPrefHelper!!.setBooleanPreferences(
                        mSharedPrefHelper!!.IS_SUBSCRIBED,
                        false
                    )
                    prefUtil?.setBool("is_premium", false)

                } else {

                    Log.d(TAG, "Error:" + billingResult.responseCode)
                    purchasesInterface?.productPurchaseFailed()
                }
            }

        private val billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
//                makeGooglePlayConnectionRequest()
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                Log.d(TAG, "On BillingSetupFinished")

                CoroutineScope(Dispatchers.IO).launch {
                    querySkuDetails()
                }
            }
        }


        private fun handlePurchase(purchaseToken: String) {
            mSharedPrefHelper!!.setBooleanPreferences(mSharedPrefHelper!!.IS_SUBSCRIBED, true)
            prefUtil?.setBool("is_premium", true)

            /*mSharedPrefHelper!!.setBooleanPreferences(
                mSharedPrefHelper!!.REMOVE_ADS_KEY,
                true
            )*/

            mSharedPrefHelper?.setStringPreferences(
                mSharedPrefHelper!!.TOKEN_SUBSCRIPTION,
                purchaseToken
            )
            purchasesInterface!!.productPurchasedSuccessful()
        }


        suspend fun querySkuDetails() {

            checkPurchaseState()

            val skuList = ArrayList<String>()
            skuList.add("01_week")
            skuList.add("01_month")
            skuList.add("01_year")

            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

            // leverage querySkuDetails Kotlin extension function
            val skuDetailsResult = withContext(Dispatchers.IO) {
                billingClient?.querySkuDetails(params.build())
            }


            Log.d(TAG, "Sku Details: ${skuDetailsResult.toString()}")

        }

        fun BillingClient.querySkuDetails(build: SkuDetailsParams) {
            this.querySkuDetailsAsync(build) { billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>? ->
                mBillingResult = billingResult
                mSkuDetailsList = skuDetailsList
            }
        }

        fun startSubscriptionProcess(context: Context, skuDetails: SkuDetails) {
            val TAG = "BILLING PROCESS"

            mSharedPrefHelper = SharedPreferencesClass(context)
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            val responseCode =
                billingClient?.launchBillingFlow(
                    context as Activity,
                    billingFlowParams
                )?.responseCode
            Log.e(TAG, "starts Purchase Process: $responseCode")

            when (responseCode) {
                BILLING_UNAVAILABLE -> {
                    Log.e(TAG, "BILLING_UNAVAILABLE")
                }

                DEVELOPER_ERROR -> {
                    Log.e(TAG, "DEVELOPER_ERROR")
                }

                ERROR -> {
                    Log.e(TAG, "ERROR OCCURE")
                }

                FEATURE_NOT_SUPPORTED -> {
                    Log.e(TAG, "FEATURE_NOT_SUPPORTED")
                }

                ITEM_ALREADY_OWNED -> {
                    Log.e(TAG, "ITEM_ALREADY_OWNED")
                }

                ITEM_NOT_OWNED -> {
                    Log.e(TAG, "ITEM_NOT_OWNED")
                }

                ITEM_UNAVAILABLE -> {
                    Log.e(TAG, "ITEM_UNAVAILABLE")
                }

                OK -> {
                    Log.e(TAG, "OK")

                }

                SERVICE_DISCONNECTED -> {
                    Log.e(TAG, "SERVICE_DISCONNECTED")
                }

                SERVICE_TIMEOUT -> {
                    Log.e(TAG, "SERVICE_TIMEOUT")
                }

                SERVICE_UNAVAILABLE -> {
                    Log.e(TAG, "SERVICE_UNAVAILABLE")
                }

                USER_CANCELED -> {
                    Log.e(TAG, "USER_CANCELED")
                }

            }
            purchasesInterface = context as SubscriptionPurchaseInterface
        }


        private fun acknowledgePurchase(purchase: Purchase) {


            if (mSkuDetailsList == null) {
                if (purchase != null) {
                    acknowledge(purchase.purchaseToken)
                    return
                }
            }


            if (mSkuDetailsList?.isNotEmpty() == true && purchase.purchaseState == Purchase.PurchaseState.PURCHASED && mSkuDetailsList!![0].type == BillingClient.SkuType.SUBS && !purchase.isAcknowledged) {
                acknowledge(purchase.purchaseToken)
            } else {
                if (purchase.isAcknowledged) {
//                    mSharedPrefHelper!!.setIsPurchased(true)
                    mSharedPrefHelper!!.setBooleanPreferences(
                        mSharedPrefHelper!!.IS_SUBSCRIBED,
                        true
                    )
                }
                prefUtil?.setBool("is_premium", true)
            }

        }


        private fun acknowledge(purchaseToken: String) {

            Log.e(TAG, "acknowledgment")
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            billingClient?.acknowledgePurchase(
                acknowledgePurchaseParams
            ) { billingResult ->
                when (billingResult.responseCode) {

                    BillingClient.BillingResponseCode.OK -> {
                        subscriptionOkayAndRemoveAds(purchaseToken)
                    }

                    else -> {
                        Log.d(TAG, "Failed to acknowledge $billingResult")
                        purchasesInterface?.productPurchaseFailed()
                    }
                }
            }

        }

        private fun subscriptionOkayAndRemoveAds(purchaseToken: String) {
            Log.e(TAG, "subscription Done")
            handlePurchase(purchaseToken)
        }

        fun checkPurchaseState() {

            billingClient?.queryPurchasesAsync(BillingClient.SkuType.SUBS) { _: BillingResult, mutableList: MutableList<Purchase> ->
                Log.e(TAG, "checkPurchaseState: " + mutableList)
                if (mutableList.isEmpty())
                    prefUtil?.setBool("is_premium", false)

                for (item in mutableList) {
                    if (item.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!item.isAcknowledged) {
                            acknowledge(item.purchaseToken)
                        } else {
                            handlePurchase(item.purchaseToken)
                        }
                    }
                }
            }
        }
    }

}