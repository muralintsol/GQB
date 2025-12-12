package com.gurukulaboard.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.gurukulaboard.models.User
import com.gurukulaboard.models.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "AppInitializer"
        
        // Super Admin
        const val SUPER_ADMIN_MOBILE = "9900502685"
        const val SUPER_ADMIN_PIN = "1983"
        const val SUPER_ADMIN_NAME = "Super Admin"
        
        // Admin Test Account
        const val ADMIN_MOBILE = "9876543210"
        const val ADMIN_PIN = "1990"
        const val ADMIN_NAME = "Admin User"
        
        // Teacher Test Account
        const val TEACHER_MOBILE = "9876543211"
        const val TEACHER_PIN = "1995"
        const val TEACHER_NAME = "Teacher User"
    }
    
    fun initializeTestAccounts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Initialize Super Admin
                initializeUser(
                    mobileNumber = SUPER_ADMIN_MOBILE,
                    pin = SUPER_ADMIN_PIN,
                    name = SUPER_ADMIN_NAME,
                    role = UserRole.SUPER_ADMIN
                )
                
                // Initialize Admin
                initializeUser(
                    mobileNumber = ADMIN_MOBILE,
                    pin = ADMIN_PIN,
                    name = ADMIN_NAME,
                    role = UserRole.ADMIN
                )
                
                // Initialize Teacher
                initializeUser(
                    mobileNumber = TEACHER_MOBILE,
                    pin = TEACHER_PIN,
                    name = TEACHER_NAME,
                    role = UserRole.TEACHER
                )
                
                Log.d(TAG, "All test accounts initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing test accounts", e)
            }
        }
    }
    
    private suspend fun initializeUser(
        mobileNumber: String,
        pin: String,
        name: String,
        role: UserRole
    ) {
        try {
            val querySnapshot = firestore.collection(User.COLLECTION_NAME)
                .whereEqualTo("mobileNumber", mobileNumber)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                // Create user
                val hashedPin = Validators.hashPin(pin)
                val user = User(
                    mobileNumber = mobileNumber,
                    pin = hashedPin,
                    name = name,
                    role = role
                )
                
                firestore.collection(User.COLLECTION_NAME)
                    .add(user)
                    .await()
                
                Log.d(TAG, "$role user created: $mobileNumber")
            } else {
                // Update existing user to ensure correct role
                val document = querySnapshot.documents[0]
                val existingUser = document.toObject(User::class.java)
                
                if (existingUser?.role != role) {
                    document.reference.update("role", role.name).await()
                    Log.d(TAG, "Existing user updated to $role: $mobileNumber")
                } else {
                    Log.d(TAG, "$role user already exists: $mobileNumber")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing user $mobileNumber", e)
        }
    }
    
    // Backward compatibility
    fun initializeSuperAdmin() {
        initializeTestAccounts()
    }
}

