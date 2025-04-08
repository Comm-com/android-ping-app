package com.faltenreich.skeletonlayout.demo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.faltenreich.skeletonlayout.demo.api.ApiService
import com.faltenreich.skeletonlayout.demo.api.SmsRequest
import com.faltenreich.skeletonlayout.demo.database.AppDatabase
import com.faltenreich.skeletonlayout.demo.model.RequestHistory
import com.faltenreich.skeletonlayout.demo.model.RequestType
import com.faltenreich.skeletonlayout.demo.repository.RequestHistoryRepository
import com.faltenreich.skeletonlayout.demo.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Broadcast receiver for handling incoming SMS messages
 */
class SmsReceiver : BroadcastReceiver() {
    
    private val TAG = "SmsReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(TAG, "Received intent with action ${intent.action}, ignoring")
            return
        }
        
        Log.d(TAG, "SMS received, processing...")
        
        // Get phone numbers from preferences
        val preferencesManager = PreferencesManager(context)
        val phoneNumbers = preferencesManager.getPhoneNumbers()
        
        if (phoneNumbers.isEmpty()) {
            Log.w(TAG, "No phone numbers configured, ignoring SMS")
            return
        }
        
        // Process SMS messages
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        
        if (messages.isEmpty()) {
            Log.w(TAG, "No SMS messages found in intent")
            return
        }
        
        Log.d(TAG, "Processing ${messages.size} SMS messages")
        
        // Process each message
        for (sms in messages) {
            processSms(context, sms, phoneNumbers)
        }
    }
    
    private fun processSms(context: Context, sms: android.telephony.SmsMessage, phoneNumbers: String) {
        // Launch coroutine to handle network request
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sender = sms.originatingAddress ?: "Unknown"
                val body = sms.messageBody ?: ""
                val pdu = sms.pdu?.joinToString(",") { it.toString() } ?: ""
                
                Log.d(TAG, "Processing SMS from $sender: ${body.take(20)}${if (body.length > 20) "..." else ""}")
                
                // Create request
                val request = SmsRequest(
                    sender = sender,
                    recipient = "Unknown", // We don't have this information
                    body = body,
                    pdu = pdu,
                    phoneNumber = phoneNumbers
                )
                
                // Send to server
                val apiService = ApiService.create()
                val response = apiService.sendSms(request)
                
                val success = response.isSuccessful
                val details = if (success) {
                    "From: $sender\nMessage: ${body.take(50)}${if (body.length > 50) "..." else ""}"
                } else {
                    "Failed: ${response.code()}\nFrom: $sender"
                }
                
                Log.d(TAG, "SMS forwarding response: ${if (success) "Success" else "Failed: ${response.code()}"}")
                
                // Create payload JSON string
                val payload = """
                    {
                        "type": "sms",
                        "sender": "$sender",
                        "recipient": "Unknown",
                        "body": "${body.replace("\"", "\\\"").take(100)}${if (body.length > 100) "..." else ""}",
                        "phoneNumber": "$phoneNumbers"
                    }
                """.trimIndent()
                
                // Save to database
                val requestHistory = RequestHistory(
                    type = RequestType.SMS,
                    timestamp = Date(),
                    phoneNumber = phoneNumbers,
                    details = details,
                    success = success,
                    url = ApiService.BASE_URL,
                    payload = payload
                )
                
                // Get database instance
                val database = AppDatabase.getDatabase(context)
                val repository = RequestHistoryRepository(database.requestHistoryDao())
                repository.insert(requestHistory)
                
                Log.d(TAG, "Saved SMS request to history")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS", e)
                
                // Save failed request to database
                try {
                    val sender = sms.originatingAddress ?: "Unknown"
                    val body = sms.messageBody ?: ""
                    
                    // Create payload JSON string for error case
                    val payload = """
                        {
                            "type": "sms",
                            "sender": "$sender",
                            "recipient": "Unknown",
                            "body": "${body.replace("\"", "\\\"").take(100)}${if (body.length > 100) "..." else ""}",
                            "phoneNumber": "$phoneNumbers"
                        }
                    """.trimIndent()
                    
                    val requestHistory = RequestHistory(
                        type = RequestType.SMS,
                        timestamp = Date(),
                        phoneNumber = phoneNumbers,
                        details = "Error: ${e.message}\nFrom: ${sms.originatingAddress ?: "Unknown"}",
                        success = false,
                        url = ApiService.BASE_URL,
                        payload = payload
                    )
                    
                    // Get database instance
                    val database = AppDatabase.getDatabase(context)
                    val repository = RequestHistoryRepository(database.requestHistoryDao())
                    repository.insert(requestHistory)
                } catch (dbError: Exception) {
                    Log.e(TAG, "Error saving failed SMS request to database", dbError)
                }
            }
        }
    }
}
