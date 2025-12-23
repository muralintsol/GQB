package com.gurukulaboard.content.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class TeachingContent(
    val id: String = "",
    val title: String,
    val description: String? = null,
    val fileName: String,
    val fileUrl: String, // Firebase Storage URL
    val fileSize: Long, // in bytes
    val subject: String,
    val classLevel: Int, // 11 or 12
    val chapter: String? = null,
    val tags: List<String> = emptyList(), // Custom tags
    val contentType: ContentType,
    val uploadedBy: String, // User ID
    val uploadedByName: String,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val downloadCount: Int = 0,
    val isFavorite: Boolean = false,
    val sectionsAnalyzed: Boolean = false // Track if PDF analysis is complete
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "description" to description,
            "fileName" to fileName,
            "fileUrl" to fileUrl,
            "fileSize" to fileSize,
            "subject" to subject,
            "classLevel" to classLevel,
            "chapter" to chapter,
            "tags" to tags,
            "contentType" to contentType.name,
            "uploadedBy" to uploadedBy,
            "uploadedByName" to uploadedByName,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "downloadCount" to downloadCount,
            "isFavorite" to isFavorite,
            "sectionsAnalyzed" to sectionsAnalyzed
        )
    }
    
    companion object {
        const val COLLECTION_NAME = "teachingContent"
        
        fun fromDocument(document: DocumentSnapshot): TeachingContent? {
            return try {
                TeachingContent(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    description = document.getString("description"),
                    fileName = document.getString("fileName") ?: "",
                    fileUrl = document.getString("fileUrl") ?: "",
                    fileSize = document.getLong("fileSize") ?: 0L,
                    subject = document.getString("subject") ?: "",
                    classLevel = document.getLong("classLevel")?.toInt() ?: 11,
                    chapter = document.getString("chapter"),
                    tags = (document.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    contentType = ContentType.valueOf(document.getString("contentType") ?: "OTHER"),
                    uploadedBy = document.getString("uploadedBy") ?: "",
                    uploadedByName = document.getString("uploadedByName") ?: "",
                    createdAt = document.getTimestamp("createdAt"),
                updatedAt = document.getTimestamp("updatedAt"),
                downloadCount = document.getLong("downloadCount")?.toInt() ?: 0,
                isFavorite = document.getBoolean("isFavorite") ?: false,
                sectionsAnalyzed = document.getBoolean("sectionsAnalyzed") ?: false
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

enum class ContentType {
    NOTES,
    WORKSHEET,
    PRESENTATION,
    LESSON_PLAN,
    REFERENCE,
    OTHER
}

