package com.faltenreich.skeletonlayout.demo.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Data classes for API requests
 */
data class PingRequest(
    val type: String = "ping",
    val phoneNumber: String
)

data class SmsRequest(
    val type: String = "sms",
    val sender: String,
    val recipient: String,
    val body: String,
    val pdu: String,
    val phoneNumber: String
)

/**
 * Retrofit API service interface
 */
interface ApiService {
    
    @POST("/api/v1/webhooks/live-phone/")
    suspend fun sendPing(@Body request: PingRequest): Response<Unit>
    
    @POST("/api/v1/webhooks/live-phone/")
    suspend fun sendSms(@Body request: SmsRequest): Response<Unit>
    
    companion object {
        private const val TAG = "ApiService"
        const val BASE_URL = "https://api.comm.com"
        
        @Volatile
        private var INSTANCE: ApiService? = null
        
        fun create(): ApiService {
            return INSTANCE ?: synchronized(this) {
                try {
                    Log.d(TAG, "Creating ApiService instance")
                    
                    val logger = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    
                    val client = OkHttpClient.Builder()
                        .addInterceptor(logger)
                        .addInterceptor { chain ->
                            try {
                                chain.proceed(chain.request())
                            } catch (e: Exception) {
                                Log.e(TAG, "Network error", e)
                                throw e
                            }
                        }
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build()
                    
                    val instance = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(ApiService::class.java)
                    
                    INSTANCE = instance
                    Log.d(TAG, "ApiService instance created successfully")
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating ApiService instance", e)
                    throw e
                }
            }
        }
    }
}
