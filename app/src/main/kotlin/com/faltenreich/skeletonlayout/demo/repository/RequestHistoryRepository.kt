package com.faltenreich.skeletonlayout.demo.repository

import androidx.lifecycle.LiveData
import com.faltenreich.skeletonlayout.demo.database.RequestHistoryDao
import com.faltenreich.skeletonlayout.demo.model.RequestHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repository for handling request history data operations
 */
class RequestHistoryRepository(private val requestHistoryDao: RequestHistoryDao) {
    
    /**
     * Get the last 10 request history items
     */
    val lastTenRequests: LiveData<List<RequestHistory>> = requestHistoryDao.getLastTenRequestHistory()
    
    /**
     * Insert a new request history item
     */
    fun insert(requestHistory: RequestHistory) {
        CoroutineScope(Dispatchers.IO).launch {
            requestHistoryDao.insert(requestHistory)
        }
    }
    
    /**
     * Delete all request history items
     */
    fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            requestHistoryDao.deleteAll()
        }
    }
}
