package com.gurukulaboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.admin.AdminDashboardActivity
import com.gurukulaboard.auth.LoginActivity
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.databinding.ActivityMainBinding
import com.gurukulaboard.models.UserRole
import com.gurukulaboard.pdf.PDFUploadActivity
import com.gurukulaboard.ncert.NCERTManagementActivity
import com.gurukulaboard.paper.PaperGeneratorActivity
import com.gurukulaboard.paper.SavedPapersActivity
import com.gurukulaboard.questionbank.QuestionBankActivity
import com.gurukulaboard.scraping.ScrapingActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        updateUI()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        
        val userName = sessionManager.getUserName() ?: "User"
        binding.tvWelcome.text = "Welcome, $userName"
    }
    
    private fun updateUI() {
        val userRole = sessionManager.getUserRole()
        
        // Show admin dashboard only for Admin and Super Admin
        if (userRole == UserRole.ADMIN || userRole == UserRole.SUPER_ADMIN) {
            binding.cardAdminDashboard.visibility = android.view.View.VISIBLE
        } else {
            binding.cardAdminDashboard.visibility = android.view.View.GONE
        }
    }
    
    private fun setupClickListeners() {
        // Question Bank
        binding.cardQuestionBank.setOnClickListener {
            startActivity(Intent(this, QuestionBankActivity::class.java))
        }
        
        // Scraping
        binding.cardScraping.setOnClickListener {
            startActivity(Intent(this, ScrapingActivity::class.java))
        }
        
        // PDF Upload
        binding.cardPdfUpload.setOnClickListener {
            startActivity(Intent(this, PDFUploadActivity::class.java))
        }
        
        // Paper Generator
        binding.cardPaperGenerator.setOnClickListener {
            startActivity(Intent(this, PaperGeneratorActivity::class.java))
        }
        
        // NCERT Books
        binding.cardNCERT.setOnClickListener {
            startActivity(Intent(this, NCERTManagementActivity::class.java))
        }
        
        // Admin Dashboard
        binding.cardAdminDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }
        
        // Logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun logout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_saved_papers -> {
                startActivity(Intent(this, SavedPapersActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
