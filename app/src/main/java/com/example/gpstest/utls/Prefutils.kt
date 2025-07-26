package com.example.gpstest.utls

import android.content.Context

class Prefutils(private val context: Context) {
    private val prefsName = "my_prefs"
    fun setInt(key: String?, value: Int) {
        val prefs = context.getSharedPreferences(prefsName, 0)
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String?, defValue: Int): Int {
        val prefs = context.getSharedPreferences(prefsName, 0)
        return prefs.getInt(key, defValue)
    }

    fun setString(key: String?, value: String?) {
        val prefs = context.getSharedPreferences(prefsName, 0)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String?, value: String?): String? {
        val prefs = context.getSharedPreferences(prefsName, 0)
        return prefs.getString(key, value)
    }

    fun getString(key: String?): String? {
        val prefs = context.getSharedPreferences(prefsName, 0)
        return prefs.getString(key, "null")
    }

    fun setBool(key: String?, value: Boolean) {
        val prefs = context.getSharedPreferences(prefsName, 0)
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBool(key: String?): Boolean {
        val prefs = context.getSharedPreferences(prefsName, 0)
        return prefs.getBoolean(key, false)
    }

    fun getBool(key: String?, defaultValue: Boolean): Boolean {
        val prefs = context.getSharedPreferences(prefsName, 0)
        return prefs.getBoolean(key, defaultValue)
    }

}