package com.gurukulaboard.content

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gurukulaboard.content.models.ContentType
import com.gurukulaboard.content.models.TeachingContent
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageManager: ContentStorageManager
) {
    
    suspend fun uploadContent(
        content: TeachingContent,
        fileUri: Uri,
        userId: String
    ): Result<String> {
        return try {
            // First upload the file
            val uploadResult = storageManager.uploadPDF(fileUri, content.fileName, userId)
            uploadResult.onSuccess { fileUrl ->
                // Then save metadata to Firestore
                val contentWithUrl = content.copy(
                    fileUrl = fileUrl,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                
                val docRef = firestore.collection(TeachingContent.COLLECTION_NAME)
                    .add(contentWithUrl.toMap())
                    .await()
                
                Result.success(docRef.id)
            }.onFailure { exception ->
                Result.failure<String>(exception)
            }
        } catch (e: Exception) {
            Result.failure<String>(e)
        }
    }
    
    suspend fun getAllContent(): Result<List<TeachingContent>> {
        return try {
            val snapshot = firestore.collection(TeachingContent.COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val contentList = snapshot.documents.mapNotNull { doc ->
                TeachingContent.fromDocument(doc)
            }
            
            Result.success(contentList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContentBySubject(subject: String, classLevel: Int): Result<List<TeachingContent>> {
        return try {
            val snapshot = firestore.collection(TeachingContent.COLLECTION_NAME)
                .whereEqualTo("subject", subject)
                .whereEqualTo("classLevel", classLevel)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val contentList = snapshot.documents.mapNotNull { doc ->
                TeachingContent.fromDocument(doc)
            }
            
            Result.success(contentList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContentByChapter(
        subject: String,
        classLevel: Int,
        chapter: String
    ): Result<List<TeachingContent>> {
        return try {
            val snapshot = firestore.collection(TeachingContent.COLLECTION_NAME)
                .whereEqualTo("subject", subject)
                .whereEqualTo("classLevel", classLevel)
                .whereEqualTo("chapter", chapter)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val contentList = snapshot.documents.mapNotNull { doc ->
                TeachingContent.fromDocument(doc)
            }
            
            Result.success(contentList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchContent(query: String): Result<List<TeachingContent>> {
        return try {
            val snapshot = firestore.collection(TeachingContent.COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val lowerQuery = query.lowercase()
            val contentList = snapshot.documents.mapNotNull { doc ->
                TeachingContent.fromDocument(doc)
            }.filter { content ->
                content.title.lowercase().contains(lowerQuery) ||
                content.description?.lowercase()?.contains(lowerQuery) == true ||
                content.tags.any { it.lowercase().contains(lowerQuery) } ||
                content.subject.lowercase().contains(lowerQuery) ||
                content.chapter?.lowercase()?.contains(lowerQuery) == true
            }
            
            Result.success(contentList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContentByTags(tags: List<String>): Result<List<TeachingContent>> {
        return try {
            val snapshot = firestore.collection(TeachingContent.COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val contentList = snapshot.documents.mapNotNull { doc ->
                TeachingContent.fromDocument(doc)
            }.filter { content ->
                tags.any { tag -> content.tags.contains(tag) }
            }
            
            Result.success(contentList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContentByType(contentType: ContentType): Result<List<TeachingContent>> {
        return try {
            val snapshot = firestore.collection(TeachingContent.COLLECTION_NAME)
                .whereEqualTo("contentType", contentType.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val contentList = snapshot.documents.mapNotNull { doc ->
                TeachingContent.fromDocument(doc)
            }
            
            Result.success(contentList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContentById(id: String): Result<TeachingContent> {
        return try {
            val document = firestore.collection(TeachingContent.COLLECTION_NAME)
                .document(id)
                .get()
                .await()
            
            val content = TeachingContent.fromDocument(document)
            if (content != null) {
                Result.success(content)
            } else {
                Result.failure(Exception("Content not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteContent(id: String, fileUrl: String): Result<Unit> {
        return try {
            // Delete from Firestore
            firestore.collection(TeachingContent.COLLECTION_NAME)
                .document(id)
                .delete()
                .await()
            
            // Delete from Storage
            storageManager.deletePDF(fileUrl)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateContent(content: TeachingContent): Result<Unit> {
        return try {
            val updatedContent = content.copy(updatedAt = Timestamp.now())
            firestore.collection(TeachingContent.COLLECTION_NAME)
                .document(content.id)
                .set(updatedContent.toMap())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun incrementDownloadCount(id: String): Result<Unit> {
        return try {
            firestore.collection(TeachingContent.COLLECTION_NAME)
                .document(id)
                .update("downloadCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMCQsForContent(contentId: String): Result<List<com.gurukulaboard.models.Question>> {
        return try {
            // MCQs are stored in question bank with sourceDetails = "teacherContent:{contentId}"
            val snapshot = firestore.collection(com.gurukulaboard.models.Question.COLLECTION_NAME)
                .whereEqualTo("sourceDetails", "teacherContent:$contentId")
                .get()
                .await()
            
            val questions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.gurukulaboard.models.Question::class.java)?.copy(id = doc.id)
            }
            
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun linkMCQsToContent(contentId: String, questionIds: List<String>): Result<Unit> {
        return try {
            // Update questions to link them to content
            questionIds.forEach { questionId ->
                firestore.collection(com.gurukulaboard.models.Question.COLLECTION_NAME)
                    .document(questionId)
                    .update("sourceDetails", "teacherContent:$contentId")
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

