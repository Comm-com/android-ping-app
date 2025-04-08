package com.faltenreich.skeletonlayout.demo

import android.app.Application
import android.util.Log
import com.faltenreich.skeletonlayout.demo.database.AppDatabase

/**
 * Application class for initializing app-wide components
 */
class SmsMonitoringApp : Application() {
    
    companion object {
        private const val TAG = "SmsMonitoringApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize the database
            AppDatabase.getDatabase(this)
            Log.d(TAG, "Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing application", e)
        }
    }
}
