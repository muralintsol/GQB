package com.gurukulaboard.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gurukulaboard.models.User
import com.gurukulaboard.models.UserRole
import com.gurukulaboard.utils.Validators
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun login(mobileNumber: String, pin: String): Result<User> {
        return try {
            if (!Validators.isValidMobileNumber(mobileNumber)) {
                return Result.failure(Exception("Invalid mobile number"))
            }
            
            if (!Validators.isValidPin(pin)) {
                return Result.failure(Exception("Invalid PIN"))
            }
            
            val hashedPin = Validators.hashPin(pin)
            
            val querySnapshot = firestore.collection(User.COLLECTION_NAME)
                .whereEqualTo("mobileNumber", mobileNumber)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("User not found"))
            }
            
            val document = querySnapshot.documents[0]
            val user = document.toObject(User::class.java)
            
            if (user == null) {
                return Result.failure(Exception("Invalid user data"))
            }
            
            if (user.pin != hashedPin) {
                return Result.failure(Exception("Invalid credentials"))
            }
            
            // Update user ID from document
            val authenticatedUser = user.copy(id = document.id)
            Result.success(authenticatedUser)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUser(
        mobileNumber: String,
        pin: String,
        name: String,
        role: UserRole
    ): Result<String> {
        return try {
            if (!Validators.isValidMobileNumber(mobileNumber)) {
                return Result.failure(Exception("Invalid mobile number"))
            }
            
            if (!Validators.isValidPin(pin)) {
                return Result.failure(Exception("Invalid PIN"))
            }
            
            // Check if user already exists
            val existingUser = firestore.collection(User.COLLECTION_NAME)
                .whereEqualTo("mobileNumber", mobileNumber)
                .limit(1)
                .get()
                .await()
            
            if (!existingUser.isEmpty) {
                return Result.failure(Exception("User already exists"))
            }
            
            val hashedPin = Validators.hashPin(pin)
            val user = User(
                mobileNumber = mobileNumber,
                pin = hashedPin,
                name = name,
                role = role
            )
            
            val documentRef = firestore.collection(User.COLLECTION_NAME)
                .add(user)
                .await()
            
            Result.success(documentRef.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = firestore.collection(User.COLLECTION_NAME)
                .document(userId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("User not found"))
            }
            
            val user = document.toObject(User::class.java)
            if (user == null) {
                return Result.failure(Exception("Invalid user data"))
            }
            
            Result.success(user.copy(id = document.id))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

