package com.gurukulaboard.auth

import android.content.SharedPreferences
import com.gurukulaboard.models.User
import com.gurukulaboard.models.UserRole
import com.gurukulaboard.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    
    fun saveUserSession(user: User) {
        sharedPreferences.edit().apply {
            putString(Constants.PREF_USER_ID, user.id)
            putString(Constants.PREF_USER_ROLE, user.role.name)
            putString(Constants.PREF_USER_NAME, user.name)
            putBoolean(Constants.PREF_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString(Constants.PREF_USER_ID, null)
    }
    
    fun getUserRole(): UserRole? {
        val roleString = sharedPreferences.getString(Constants.PREF_USER_ROLE, null)
        return roleString?.let { UserRole.valueOf(it) }
    }
    
    fun getUserName(): String? {
        return sharedPreferences.getString(Constants.PREF_USER_NAME, null)
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
    }
    
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}

