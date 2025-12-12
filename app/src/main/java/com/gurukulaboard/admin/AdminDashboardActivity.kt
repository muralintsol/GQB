package com.gurukulaboard.admin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityAdminDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: AdminViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupObservers()
        setupClickListeners()
        viewModel.loadStatistics()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.admin_dashboard)
    }
    
    private fun setupObservers() {
        viewModel.statistics.observe(this) { stats ->
            binding.tvTotalQuestions.text = "Total Questions: ${stats.totalQuestions}"
            binding.tvPendingQuestions.text = "Pending: ${stats.pendingQuestions}"
            binding.tvApprovedQuestions.text = "Approved: ${stats.approvedQuestions}"
        }
    }
    
    private fun setupClickListeners() {
        binding.cardUserManagement.setOnClickListener {
            startActivity(android.content.Intent(this, com.gurukulaboard.admin.UserManagementActivity::class.java))
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

