package com.faltenreich.skeletonlayout.demo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.faltenreich.skeletonlayout.demo.database.AppDatabase
import com.faltenreich.skeletonlayout.demo.model.RequestHistory
import com.faltenreich.skeletonlayout.demo.repository.RequestHistoryRepository
import com.faltenreich.skeletonlayout.demo.util.PreferencesManager

/**
 * ViewModel for the MainActivity
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: RequestHistoryRepository
    private val preferencesManager: PreferencesManager
    
    // LiveData for request history
    val requestHistory: LiveData<List<RequestHistory>>
    
    init {
        // Initialize repository
        val requestHistoryDao = AppDatabase.getDatabase(application).requestHistoryDao()
        repository = RequestHistoryRepository(requestHistoryDao)
        requestHistory = repository.lastTenRequests
        
        // Initialize preferences manager
        preferencesManager = PreferencesManager(application)
    }
    
    /**
     * Save phone numbers to preferences
     */
    fun savePhoneNumbers(phoneNumbers: String) {
        preferencesManager.savePhoneNumbers(phoneNumbers)
    }
    
    /**
     * Get phone numbers from preferences
     */
    fun getPhoneNumbers(): String {
        return preferencesManager.getPhoneNumbers()
    }
    
    /**
     * Save service running state to preferences
     */
    fun saveServiceRunning(isRunning: Boolean) {
        preferencesManager.saveServiceRunning(isRunning)
    }
    
    /**
     * Check if service is running from preferences
     */
    fun isServiceRunning(): Boolean {
        return preferencesManager.isServiceRunning()
    }
}
