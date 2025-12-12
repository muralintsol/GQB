package com.gurukulaboard.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    init {
        // Check if user is already logged in
        val savedUserId = sessionManager.getUserId()
        if (savedUserId != null) {
            loadUser(savedUserId)
        }
    }
    
    fun login(mobileNumber: String, pin: String) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authRepository.login(mobileNumber, pin)
            
            result.onSuccess { user ->
                sessionManager.saveUserSession(user)
                _currentUser.value = user
                _loginState.value = LoginState.Success(user)
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "Login failed")
            }
        }
    }
    
    fun logout() {
        sessionManager.clearSession()
        _currentUser.value = null
        _loginState.value = LoginState.Idle
    }
    
    private fun loadUser(userId: String) {
        viewModelScope.launch {
            val result = authRepository.getUserById(userId)
            result.onSuccess { user ->
                _currentUser.value = user
            }
        }
    }
    
    fun isLoggedIn(): Boolean {
        return sessionManager.getUserId() != null
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

