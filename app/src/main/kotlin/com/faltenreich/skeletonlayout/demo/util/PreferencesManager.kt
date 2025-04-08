package com.faltenreich.skeletonlayout.demo.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for handling shared preferences
 */
class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * Save phone numbers as a comma-separated string
     */
    fun savePhoneNumbers(phoneNumbers: String) {
        sharedPreferences.edit().putString(KEY_PHONE_NUMBERS, phoneNumbers).apply()
    }
    
    /**
     * Get saved phone numbers
     */
    fun getPhoneNumbers(): String {
        return sharedPreferences.getString(KEY_PHONE_NUMBERS, "") ?: ""
    }
    
    /**
     * Save service running state
     */
    fun saveServiceRunning(isRunning: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SERVICE_RUNNING, isRunning).apply()
    }
    
    /**
     * Get service running state
     */
    fun isServiceRunning(): Boolean {
        return sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
    }
    
    companion object {
        private const val PREF_NAME = "app_preferences"
        private const val KEY_PHONE_NUMBERS = "phone_numbers"
        private const val KEY_SERVICE_RUNNING = "service_running"
    }
}
