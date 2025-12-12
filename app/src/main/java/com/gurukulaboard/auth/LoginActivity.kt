package com.gurukulaboard.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gurukulaboard.MainActivity
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityLoginBinding
import com.gurukulaboard.utils.AppInitializer
import com.gurukulaboard.utils.Validators
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    
    @Inject
    lateinit var appInitializer: AppInitializer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize all test accounts
        appInitializer.initializeTestAccounts()
        
        // Check if already logged in
        if (viewModel.isLoggedIn()) {
            navigateToMain()
            return
        }
        
        setupObservers()
        setupClickListeners()
        setupTestMode()
    }
    
    private fun setupTestMode() {
        // Auto-fill test accounts for quick testing
        binding.btnTestSuperAdmin.setOnClickListener {
            binding.etMobileNumber.setText(AppInitializer.SUPER_ADMIN_MOBILE)
            binding.etPin.setText(AppInitializer.SUPER_ADMIN_PIN)
            viewModel.login(AppInitializer.SUPER_ADMIN_MOBILE, AppInitializer.SUPER_ADMIN_PIN)
        }
        
        binding.btnTestAdmin.setOnClickListener {
            binding.etMobileNumber.setText(AppInitializer.ADMIN_MOBILE)
            binding.etPin.setText(AppInitializer.ADMIN_PIN)
            viewModel.login(AppInitializer.ADMIN_MOBILE, AppInitializer.ADMIN_PIN)
        }
        
        binding.btnTestTeacher.setOnClickListener {
            binding.etMobileNumber.setText(AppInitializer.TEACHER_MOBILE)
            binding.etPin.setText(AppInitializer.TEACHER_PIN)
            viewModel.login(AppInitializer.TEACHER_MOBILE, AppInitializer.TEACHER_PIN)
        }
    }
    
    private fun setupObservers() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is LoginState.Success -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.visibility = android.view.View.GONE
                    navigateToMain()
                }
                is LoginState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is LoginState.Idle -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val mobileNumber = binding.etMobileNumber.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()
            
            if (!Validators.isValidMobileNumber(mobileNumber)) {
                binding.etMobileNumber.error = getString(R.string.invalid_mobile_number)
                return@setOnClickListener
            }
            
            if (!Validators.isValidPin(pin)) {
                binding.etPin.error = getString(R.string.invalid_pin)
                return@setOnClickListener
            }
            
            viewModel.login(mobileNumber, pin)
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

