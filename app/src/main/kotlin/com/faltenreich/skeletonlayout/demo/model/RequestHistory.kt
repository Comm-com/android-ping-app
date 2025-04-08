package com.faltenreich.skeletonlayout.demo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a request history item
 */
@Entity(tableName = "request_history")
data class RequestHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: RequestType,
    val phoneNumber: String,
    val details: String,
    val timestamp: Date = Date(),
    val success: Boolean = true,
    val url: String = "",
    val payload: String = ""
)

/**
 * Enum representing the type of request
 */
enum class RequestType {
    PING,
    SMS
}
