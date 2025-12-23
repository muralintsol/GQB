package com.gurukulaboard.content

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gurukulaboard.content.models.TeacherPPT
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentPPTRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun savePPT(ppt: TeacherPPT): Result<String> {
        return try {
            val pptWithTimestamp = ppt.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            val docRef = firestore.collection(TeacherPPT.COLLECTION_NAME)
                .add(pptWithTimestamp.toMap())
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPPTById(id: String): Result<TeacherPPT> {
        return try {
            val document = firestore.collection(TeacherPPT.COLLECTION_NAME)
                .document(id)
                .get()
                .await()
            
            val ppt = TeacherPPT.fromDocument(document)
            if (ppt != null) {
                Result.success(ppt)
            } else {
                Result.failure(Exception("PPT not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPPTsByContent(contentId: String): Result<List<TeacherPPT>> {
        return try {
            val snapshot = firestore.collection(TeacherPPT.COLLECTION_NAME)
                .whereEqualTo("contentId", contentId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val ppts = snapshot.documents.mapNotNull { doc ->
                TeacherPPT.fromDocument(doc)
            }
            
            Result.success(ppts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePPT(ppt: TeacherPPT): Result<Unit> {
        return try {
            val updatedPPT = ppt.copy(updatedAt = Timestamp.now())
            firestore.collection(TeacherPPT.COLLECTION_NAME)
                .document(ppt.id)
                .set(updatedPPT.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePPT(id: String): Result<Unit> {
        return try {
            firestore.collection(TeacherPPT.COLLECTION_NAME)
                .document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

