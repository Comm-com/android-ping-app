package com.faltenreich.skeletonlayout.demo.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for the application
 */
object Utils {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Format a date to a readable string
     */
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
    
    /**
     * Validate phone numbers format
     * Returns true if the format is valid (comma-separated numbers)
     */
    fun validatePhoneNumbers(phoneNumbers: String): Boolean {
        if (phoneNumbers.isBlank()) return false
        
        // Simple validation - check if it contains only digits and commas
        val validChars = setOf(',', '+', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ')
        return phoneNumbers.all { it in validChars }
    }
    
    /**
     * Clean phone numbers string by removing spaces
     */
    fun cleanPhoneNumbers(phoneNumbers: String): String {
        return phoneNumbers.replace(" ", "")
    }
}
