package com.gurukulaboard.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    var id: String = "",
    val mobileNumber: String = "",
    val pin: String = "", // Hashed PIN (birth year)
    val role: UserRole = UserRole.TEACHER,
    val name: String = "",
    val createdAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_NAME = "users"
    }
}

