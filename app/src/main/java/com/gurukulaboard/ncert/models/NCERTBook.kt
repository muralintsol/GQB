package com.gurukulaboard.ncert.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class NCERTBook(
    @DocumentId
    var id: String = "",
    val subject: String,
    val classLevel: Int,
    val language: String = "English", // English, Kannada, etc.
    val fileName: String,
    val fileSize: Long = 0,
    val firebaseStorageUrl: String? = null,
    val localFilePath: String? = null,
    val indexId: String? = null, // Reference to ncertIndexes collection
    val uploadedAt: Timestamp? = null,
    val processedAt: Timestamp? = null,
    val status: NCERTBookStatus = NCERTBookStatus.PENDING
) {
    companion object {
        const val COLLECTION_NAME = "ncertBooks"
    }
}

enum class NCERTBookStatus {
    PENDING,
    PROCESSING,
    INDEXED,
    ERROR
}

