package com.example.gpstest.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.gpstest.LocaleHelper
import com.example.gpstest.utls.Prefutils

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            val selectedLanguage = Prefutils(it).getString("selectedLanguage", "en") ?: "en"
            super.attachBaseContext(LocaleHelper.wrapContext(it, selectedLanguage))
        } ?: super.attachBaseContext(newBase)
    }
}
