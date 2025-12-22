package com.gurukulaboard.paper

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.gurukulaboard.models.QuestionPaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaperRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun savePaper(paper: QuestionPaper): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val paperWithTimestamp = paper.copy(
                createdAt = Timestamp.now()
            )
            val docRef = firestore.collection(QuestionPaper.COLLECTION_NAME)
                .add(paperWithTimestamp)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPapers(limit: Int = 50): Result<List<QuestionPaper>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = firestore.collection(QuestionPaper.COLLECTION_NAME)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val papers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(QuestionPaper::class.java)?.copy(id = doc.id)
            }
            Result.success(papers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPaperById(paperId: String): Result<QuestionPaper> = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = firestore.collection(QuestionPaper.COLLECTION_NAME)
                .document(paperId)
                .get()
                .await()
            
            if (doc.exists()) {
                val paper = doc.toObject(QuestionPaper::class.java)?.copy(id = doc.id)
                if (paper != null) {
                    Result.success(paper)
                } else {
                    Result.failure(Exception("Failed to parse paper"))
                }
            } else {
                Result.failure(Exception("Paper not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePaper(paperId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            firestore.collection(QuestionPaper.COLLECTION_NAME)
                .document(paperId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

