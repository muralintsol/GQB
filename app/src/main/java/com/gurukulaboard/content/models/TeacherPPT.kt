package com.gurukulaboard.content.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class TeacherPPT(
    val id: String = "",
    val title: String,
    val contentId: String, // Reference to TeachingContent
    val htmlContent: String, // Full HTML slide deck
    val slides: List<SlideData>, // Structured slide data
    val subject: String,
    val classLevel: Int,
    val chapter: String? = null,
    val createdBy: String,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val isDraft: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "contentId" to contentId,
            "htmlContent" to htmlContent,
            "slides" to slides.map { it.toMap() },
            "subject" to subject,
            "classLevel" to classLevel,
            "chapter" to chapter,
            "createdBy" to createdBy,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isDraft" to isDraft
        )
    }
    
    companion object {
        const val COLLECTION_NAME = "teacherPPTs"
        
        fun fromDocument(document: DocumentSnapshot): TeacherPPT? {
            return try {
                val slidesList = (document.get("slides") as? List<*>)?.mapNotNull { slideMap ->
                    if (slideMap is Map<*, *>) {
                        SlideData.fromMap(slideMap)
                    } else null
                } ?: emptyList()
                
                TeacherPPT(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    contentId = document.getString("contentId") ?: "",
                    htmlContent = document.getString("htmlContent") ?: "",
                    slides = slidesList,
                    subject = document.getString("subject") ?: "",
                    classLevel = document.getLong("classLevel")?.toInt() ?: 11,
                    chapter = document.getString("chapter"),
                    createdBy = document.getString("createdBy") ?: "",
                    createdAt = document.getTimestamp("createdAt"),
                    updatedAt = document.getTimestamp("updatedAt"),
                    isDraft = document.getBoolean("isDraft") ?: false
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class SlideData(
    val slideNumber: Int,
    val title: String,
    val content: String,
    val slideType: SlideType,
    val order: Int
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "slideNumber" to slideNumber,
            "title" to title,
            "content" to content,
            "slideType" to slideType.name,
            "order" to order
        )
    }
    
    companion object {
        fun fromMap(map: Map<*, *>): SlideData? {
            return try {
                SlideData(
                    slideNumber = (map["slideNumber"] as? Long)?.toInt() ?: 0,
                    title = map["title"] as? String ?: "",
                    content = map["content"] as? String ?: "",
                    slideType = SlideType.valueOf(map["slideType"] as? String ?: "CONTENT"),
                    order = (map["order"] as? Long)?.toInt() ?: 0
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

enum class SlideType {
    TITLE,
    CONTENT,
    IMAGE,
    BULLET_POINTS,
    SUMMARY
}

