package com.gurukulaboard.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityUserManagementBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserManagementActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUserManagementBinding
    private val viewModel: AdminViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.user_management)
    }
    
    private fun setupClickListeners() {
        binding.btnCreateUser.setOnClickListener {
            val mobileNumber = binding.etMobileNumber.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            
            if (mobileNumber.isBlank() || pin.isBlank() || name.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.createTeacherAccount(mobileNumber, pin, name)
        }
        
        setupObservers()
    }
    
    private fun setupObservers() {
        viewModel.createUserState.observe(this) { state ->
            when (state) {
                is CreateUserState.Loading -> {
                    binding.btnCreateUser.isEnabled = false
                }
                is CreateUserState.Success -> {
                    binding.btnCreateUser.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    // Clear fields
                    binding.etMobileNumber.text?.clear()
                    binding.etPin.text?.clear()
                    binding.etName.text?.clear()
                }
                is CreateUserState.Error -> {
                    binding.btnCreateUser.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.btnCreateUser.isEnabled = true
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

