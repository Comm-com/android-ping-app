package com.faltenreich.skeletonlayout.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.faltenreich.skeletonlayout.demo.adapter.RequestHistoryAdapter
import com.faltenreich.skeletonlayout.demo.databinding.ActivityMainBinding
import com.faltenreich.skeletonlayout.demo.service.PingService
import com.faltenreich.skeletonlayout.demo.util.Utils
import com.faltenreich.skeletonlayout.demo.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var requestHistoryAdapter: RequestHistoryAdapter

    // Permission launcher for SMS permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel and binding first, before any other operations
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        try {
            setSupportActionBar(binding.toolbar)

            // Set up RecyclerView
            setupRecyclerView()

            // Set up UI listeners
            setupListeners()

            // Load saved phone numbers
            loadPhoneNumbers()

            // Request permissions
            requestPermissions()

            // Observe request history
            observeRequestHistory()

            // Update service status UI
            updateServiceStatusUI()
            
            // Start service if phone numbers are configured and service is not running
            startServiceIfNeeded()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView() {
        requestHistoryAdapter = RequestHistoryAdapter()
        binding.requestHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = requestHistoryAdapter
        }
    }

    private fun setupListeners() {
        // Save button
        binding.saveButton.setOnClickListener {
            val phoneNumbers = binding.phoneNumberEditText.text.toString().trim()
            
            if (Utils.validatePhoneNumbers(phoneNumbers)) {
                val cleanedPhoneNumbers = Utils.cleanPhoneNumbers(phoneNumbers)
                viewModel.savePhoneNumbers(cleanedPhoneNumbers)
                Toast.makeText(this, "Phone numbers saved", Toast.LENGTH_SHORT).show()
                
                // Start service if it's not running
                if (!viewModel.isServiceRunning()) {
                    PingService.start(this)
                    viewModel.saveServiceRunning(true)
                    updateServiceStatusUI()
                }
            } else {
                Toast.makeText(this, "Invalid phone numbers format", Toast.LENGTH_SHORT).show()
            }
        }

        // Service status FAB
        binding.serviceStatusFab.setOnClickListener {
            toggleService()
        }
    }

    private fun loadPhoneNumbers() {
        val phoneNumbers = viewModel.getPhoneNumbers()
        binding.phoneNumberEditText.setText(phoneNumbers)
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.INTERNET
        )

        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }

    private fun observeRequestHistory() {
        viewModel.requestHistory.observe(this) { requests ->
            requestHistoryAdapter.submitList(requests)
            
            // Show empty state if there are no requests
            binding.emptyStateTextView.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun toggleService() {
        val isRunning = viewModel.isServiceRunning()
        
        if (isRunning) {
            // Stop service
            PingService.stop(this)
            viewModel.saveServiceRunning(false)
        } else {
            // Start service
            PingService.start(this)
            viewModel.saveServiceRunning(true)
        }
        
        updateServiceStatusUI()
    }

    private fun updateServiceStatusUI() {
        if (!::viewModel.isInitialized || !::binding.isInitialized) {
            return  // Exit early if either viewModel or binding is not initialized
        }
        
        val isRunning = viewModel.isServiceRunning()
        
        binding.serviceStatusFab.setImageResource(
            if (isRunning) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun startServiceIfNeeded() {
        val phoneNumbers = viewModel.getPhoneNumbers()
        val isServiceRunning = viewModel.isServiceRunning()
        
        if (phoneNumbers.isNotEmpty() && !isServiceRunning) {
            PingService.start(this)
            viewModel.saveServiceRunning(true)
            updateServiceStatusUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatusUI()
    }
}