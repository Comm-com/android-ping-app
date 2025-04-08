package com.faltenreich.skeletonlayout.demo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.faltenreich.skeletonlayout.demo.service.PingService
import com.faltenreich.skeletonlayout.demo.util.PreferencesManager

/**
 * Broadcast receiver for handling device boot events
 * Restarts the ping service if it was running before the device was restarted
 */
class BootReceiver : BroadcastReceiver() {
    
    private val TAG = "BootReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, checking if service should be started")
            
            val preferencesManager = PreferencesManager(context)
            val wasServiceRunning = preferencesManager.isServiceRunning()
            val phoneNumbers = preferencesManager.getPhoneNumbers()
            
            if (wasServiceRunning && phoneNumbers.isNotEmpty()) {
                Log.d(TAG, "Starting ping service after boot")
                PingService.start(context)
            } else {
                Log.d(TAG, "Service was not running before reboot or no phone numbers configured")
            }
        }
    }
}
