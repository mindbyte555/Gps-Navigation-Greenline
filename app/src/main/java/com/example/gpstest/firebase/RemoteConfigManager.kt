package com.example.gpstest.firebase

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings


class RemoteConfigManager() {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun fetchRemoteConfig(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }
    fun addConfigUpdateListener(keysToWatch: Set<String>, onUpdate: (updatedKeys: Set<String>) -> Unit, onError: (Exception) -> Unit) {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Log.d(TAG, "Updated keys: ${configUpdate.updatedKeys}")
                if (configUpdate.updatedKeys.any { keysToWatch.contains(it) }) {
                    remoteConfig.activate().addOnCompleteListener {
                        onUpdate(configUpdate.updatedKeys)
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update error: ${error.code}", error)
                onError(error)
            }
        })
    }
    fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }
}