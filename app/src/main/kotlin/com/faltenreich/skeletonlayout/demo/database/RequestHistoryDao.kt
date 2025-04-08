package com.faltenreich.skeletonlayout.demo.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faltenreich.skeletonlayout.demo.model.RequestHistory

/**
 * Data Access Object for RequestHistory entities
 */
@Dao
interface RequestHistoryDao {
    
    /**
     * Insert a new request history item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(requestHistory: RequestHistory): Long
    
    /**
     * Get all request history items, ordered by timestamp descending
     */
    @Query("SELECT * FROM request_history ORDER BY timestamp DESC")
    fun getAllRequestHistory(): LiveData<List<RequestHistory>>
    
    /**
     * Get the last 10 request history items, ordered by timestamp descending
     */
    @Query("SELECT * FROM request_history ORDER BY timestamp DESC LIMIT 10")
    fun getLastTenRequestHistory(): LiveData<List<RequestHistory>>
    
    /**
     * Delete all request history items
     */
    @Query("DELETE FROM request_history")
    fun deleteAll()
}
