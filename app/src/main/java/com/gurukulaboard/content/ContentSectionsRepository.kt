package com.gurukulaboard.content

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.gurukulaboard.content.models.ContentSection
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentSectionsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        const val COLLECTION_NAME = "contentSections"
    }
    
    /**
     * Save detected sections to Firestore
     */
    suspend fun saveSections(
        contentId: String,
        sections: List<ContentSection>,
        fileUrl: String? = null
    ): Result<Unit> {
        return try {
            val sectionsData = sections.map { section ->
                mapOf(
                    "id" to section.id,
                    "title" to section.title,
                    "type" to section.type.name,
                    "startPage" to section.startPage,
                    "endPage" to section.endPage,
                    "pageRange" to section.pageRange,
                    "preview" to section.preview,
                    "isSelected" to section.isSelected
                )
            }
            
            val documentData = mapOf(
                "contentId" to contentId,
                "sections" to sectionsData,
                "analyzedAt" to Timestamp.now(),
                "pdfFileUrl" to (fileUrl ?: "")
            )
            
            firestore.collection(COLLECTION_NAME)
                .document(contentId)
                .set(documentData)
                .await()
            
            // Update TeachingContent to mark sections as analyzed
            try {
                firestore.collection("teachingContent")
                    .document(contentId)
                    .update("sectionsAnalyzed", true)
                    .await()
            } catch (e: Exception) {
                // Non-critical, continue
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get cached sections from Firestore
     */
    suspend fun getSections(contentId: String): Result<List<ContentSection>> {
        return try {
            val document = firestore.collection(COLLECTION_NAME)
                .document(contentId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.success(emptyList())
            }
            
            val sectionsList = (document.get("sections") as? List<*>)?.mapNotNull { sectionMap ->
                if (sectionMap is Map<*, *>) {
                    try {
                        ContentSection(
                            id = sectionMap["id"] as? String ?: "",
                            title = sectionMap["title"] as? String ?: "",
                            type = com.gurukulaboard.content.models.SectionType.valueOf(
                                sectionMap["type"] as? String ?: "OTHER"
                            ),
                            startPage = (sectionMap["startPage"] as? Long)?.toInt() ?: 0,
                            endPage = (sectionMap["endPage"] as? Long)?.toInt() ?: 0,
                            pageRange = sectionMap["pageRange"] as? String ?: "",
                            preview = sectionMap["preview"] as? String ?: "",
                            isSelected = sectionMap["isSelected"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null
            } ?: emptyList()
            
            Result.success(sectionsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if sections exist for content
     */
    suspend fun hasSections(contentId: String): Result<Boolean> {
        return try {
            val document = firestore.collection(COLLECTION_NAME)
                .document(contentId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.success(false)
            }
            
            val sections = document.get("sections") as? List<*>
            Result.success(sections != null && sections.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete sections for content
     */
    suspend fun deleteSections(contentId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(contentId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get analysis timestamp
     */
    suspend fun getAnalyzedAt(contentId: String): Result<Timestamp?> {
        return try {
            val document = firestore.collection(COLLECTION_NAME)
                .document(contentId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.success(null)
            }
            
            val analyzedAt = document.getTimestamp("analyzedAt")
            Result.success(analyzedAt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

