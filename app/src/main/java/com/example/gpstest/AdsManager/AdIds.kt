package com.example.gpstest.AdsManager

import com.example.gpstest.BuildConfig


class AdIds {
    companion object {
        var Admob_Banner: String = "ca-app-pub-3624833649786834/5435080531"
        var Admob_Inline_Banner: String = "ca-app-pub-3624833649786834/6448978336"
        var Admob_Banneradaptive: String = "ca-app-pub-3624833649786834/5435080531"
        var splashBanner: String = "ca-app-pub-3624833649786834/5131459989"
        var Admob_Interstitial: String = "ca-app-pub-3624833649786834/8955523145"
        var Admob_Native: String = "ca-app-pub-3624833649786834/4616822881"
        var Admob_AppOpen: String = "ca-app-pub-3624833649786834/6329359803"
        var AppOpen_resume: String = "ca-app-pub-3624833649786834/8661036687"
        var Admob_RecBanner: String = "ca-app-pub-3624833649786834/9978600821"
        var Admob_RecNew: String = "ca-app-pub-3624833649786834/4441103610"
        var aiadunit_id: String = "ca-app-pub-3624833649786834/1157565772"
        var Admob_Lang_Inter: String = "ca-app-pub-3624833649786834/4304182875"

        val Admob_Banner_Test: String = "ca-app-pub-3940256099942544/2014213617"
        val Admob_Banneradaptive_Test: String = "ca-app-pub-3940256099942544/6300978111"
        val Admob_Interstitial_Test: String = "/6499/example/interstitial"
        val Admob_Native_Test: String = "ca-app-pub-3940256099942544/1044960115"
        val Admob_AppOpen_test: String = "ca-app-pub-3940256099942544/9257395921"

        fun AdmobSplashBannerId(): String {
            return if (BuildConfig.DEBUG) {
                return Admob_Banner_Test
            } else {
                return splashBanner
            }
        }

        fun AdmobRecBannerId(): String {
            return if (BuildConfig.DEBUG) {
                return Admob_Banneradaptive_Test
            } else {
                return Admob_RecBanner
            }
        }

        fun AdmobInlineBannerId(): String {
            return if (BuildConfig.DEBUG) {
                return Admob_Banneradaptive_Test
            } else {
                return Admob_Inline_Banner
            }
        }

        fun AdmobRecBannerNewId(): String {
            return if (BuildConfig.DEBUG) {
                return Admob_Banneradaptive_Test
            } else {
                return Admob_RecNew
            }
        }

        fun AdmobAdaptiveBannerId(): String {
            return if (BuildConfig.DEBUG) {
                Admob_Banneradaptive_Test
            } else {
                Admob_Banneradaptive
            }
        }

        fun AdmobCollaspeBannerId(): String {
            return if (BuildConfig.DEBUG) {
                Admob_Banner_Test
            } else {
                Admob_Banner
            }
        }

        fun AdmobAiCollaspeBannerId(): String {
            return if (BuildConfig.DEBUG) {
                Admob_Banner_Test
            } else {
                aiadunit_id
            }
        }

        fun getAppOpenId(): String {
            return if (BuildConfig.DEBUG) {
                Admob_AppOpen_test
            } else {
                Admob_AppOpen
            }
        }

        fun getAppOpenResumeId(): String {
            return if (BuildConfig.DEBUG) {
                Admob_AppOpen_test
            } else {
                AppOpen_resume
            }
        }


        fun AdmobInterstitialId(): String {
            return if (BuildConfig.DEBUG) {
                Admob_Interstitial_Test
            } else {
                Admob_Interstitial
            }

        }

        fun AdmobSmallNativeId(): String {
            return if (BuildConfig.DEBUG) {
                return Admob_Native_Test
            } else {
                return Admob_Native
            }
        }

        fun AdmobNativeId(): String {
            return if (BuildConfig.DEBUG) {
                Admob_Native_Test
            } else {
                Admob_Native
            }
        }


        fun admob_Lang_Interstitial(): String {
            return if (BuildConfig.DEBUG) {
                Admob_Interstitial_Test
            } else {
                Admob_Lang_Inter
            }

        }
    }
}