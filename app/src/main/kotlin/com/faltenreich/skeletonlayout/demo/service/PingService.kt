package com.faltenreich.skeletonlayout.demo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.faltenreich.skeletonlayout.demo.MainActivity
import com.faltenreich.skeletonlayout.demo.R
import com.faltenreich.skeletonlayout.demo.api.ApiService
import com.faltenreich.skeletonlayout.demo.api.PingRequest
import com.faltenreich.skeletonlayout.demo.database.AppDatabase
import com.faltenreich.skeletonlayout.demo.model.RequestHistory
import com.faltenreich.skeletonlayout.demo.model.RequestType
import com.faltenreich.skeletonlayout.demo.repository.RequestHistoryRepository
import com.faltenreich.skeletonlayout.demo.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Foreground service for sending ping requests every 10 seconds
 */
class PingService : Service() {
    
    private val TAG = "PingService"
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "ping_service_channel"
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: RequestHistoryRepository
    private lateinit var apiService: ApiService
    
    private var pingJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            Log.d(TAG, "Initializing PingService")
            preferencesManager = PreferencesManager(this)
            val dao = AppDatabase.getDatabase(this).requestHistoryDao()
            repository = RequestHistoryRepository(dao)
            apiService = ApiService.create()
            
            // Create notification channel
            createNotificationChannel()
            
            Log.d(TAG, "PingService initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PingService", e)
            stopSelf()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d(TAG, "Starting PingService in foreground")
            
            // Start as a foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification())
            
            preferencesManager.saveServiceRunning(true)
            startPingJob()
            
            Log.d(TAG, "PingService started successfully")
            return START_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "Error starting PingService", e)
            stopSelf()
            return START_NOT_STICKY
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        pingJob?.cancel()
        preferencesManager.saveServiceRunning(false)
        Log.d(TAG, "Ping service destroyed")
    }
    
    /**
     * Start the job to send ping requests every 10 seconds
     */
    private fun startPingJob() {
        pingJob?.cancel()
        
        pingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    sendPingRequest()
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending ping", e)
                }
                
                // Wait for 10 seconds before the next ping
                delay(10_000)
            }
        }
    }
    
    private suspend fun sendPingRequest() {
        try {
            val phoneNumbers = preferencesManager.getPhoneNumbers()
            if (phoneNumbers.isEmpty()) {
                Log.w(TAG, "No phone numbers configured, skipping ping")
                return
            }
            
            Log.d(TAG, "Sending ping request for numbers: $phoneNumbers")
            
            val request = PingRequest(phoneNumber = phoneNumbers)
            val response = apiService.sendPing(request)
            
            val success = response.isSuccessful
            val details = if (success) "Success" else "Failed: ${response.code()}"
            
            Log.d(TAG, "Ping response: $details")
            
            // Save to database
            val requestHistory = RequestHistory(
                type = RequestType.PING,
                timestamp = Date(),
                phoneNumber = phoneNumbers,
                details = details,
                success = success,
                url = ApiService.BASE_URL,
                payload = "{ \"type\": \"ping\", \"phoneNumber\": \"$phoneNumbers\" }"
            )
            
            repository.insert(requestHistory)
            Log.d(TAG, "Saved ping request to history")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending ping request", e)
            
            // Save failed request to database
            val requestHistory = RequestHistory(
                type = RequestType.PING,
                timestamp = Date(),
                phoneNumber = preferencesManager.getPhoneNumbers(),
                details = "Error: ${e.message}",
                success = false,
                url = ApiService.BASE_URL,
                payload = "{ \"type\": \"ping\", \"phoneNumber\": \"${preferencesManager.getPhoneNumbers()}\" }"
            )
            
            repository.insert(requestHistory)
        }
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create a notification for the foreground service
     */
    private fun createNotification(): Notification {
        // Create an intent for the activity
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        
        // Build the notification
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_channel_name))
            .setContentText(getString(R.string.notification_channel_description))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    companion object {
        /**
         * Start the ping service
         */
        fun start(context: Context) {
            val intent = Intent(context, PingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Stop the ping service
         */
        fun stop(context: Context) {
            val intent = Intent(context, PingService::class.java)
            context.stopService(intent)
        }
    }
}
