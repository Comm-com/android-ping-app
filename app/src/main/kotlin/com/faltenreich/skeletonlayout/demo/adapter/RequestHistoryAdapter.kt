package com.faltenreich.skeletonlayout.demo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faltenreich.skeletonlayout.demo.databinding.ItemRequestHistoryBinding
import com.faltenreich.skeletonlayout.demo.model.RequestHistory
import com.faltenreich.skeletonlayout.demo.model.RequestType
import com.faltenreich.skeletonlayout.demo.util.Utils
import com.faltenreich.skeletonlayout.demo.api.ApiService

/**
 * Adapter for displaying request history items
 */
class RequestHistoryAdapter : ListAdapter<RequestHistory, RequestHistoryAdapter.ViewHolder>(RequestHistoryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRequestHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(private val binding: ItemRequestHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: RequestHistory) {
            binding.apply {
                // Set request type
                requestTypeTextView.text = item.type.name
                
                // Set background color based on request type
                requestTypeTextView.setBackgroundColor(
                    if (item.type == RequestType.PING) {
                        android.graphics.Color.parseColor("#2196F3") // Blue
                    } else {
                        android.graphics.Color.parseColor("#4CAF50") // Green
                    }
                )
                
                // Set timestamp
                timestampTextView.text = Utils.formatDate(item.timestamp)
                
                // Set details
                requestDetailsTextView.text = buildString {
                    append("Phone: ${item.phoneNumber}")
                    
                    // Add URL if it exists
                    if (item.url.isNotEmpty()) {
                        append("\nURL: ${item.url}")
                    } else if (item.type == RequestType.PING || item.type == RequestType.SMS) {
                        // If URL is not explicitly set but we know it's an API request
                        append("\nURL: ${ApiService.BASE_URL}")
                    }
                    
                    // Add payload if it exists
                    if (item.payload.isNotEmpty()) {
                        append("\nPayload: ${item.payload}")
                    }
                    
                    append("\n")
                    append(item.details)
                    
                    if (!item.success) {
                        append("\n")
                        append("❌ Failed")
                    }
                }
            }
        }
    }
    
    /**
     * DiffUtil callback for efficient RecyclerView updates
     */
    class RequestHistoryDiffCallback : DiffUtil.ItemCallback<RequestHistory>() {
        override fun areItemsTheSame(oldItem: RequestHistory, newItem: RequestHistory): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: RequestHistory, newItem: RequestHistory): Boolean {
            return oldItem == newItem
        }
    }
}
