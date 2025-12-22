package com.gurukulaboard.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.gurukulaboard.models.Question
import com.gurukulaboard.questionbank.QuestionBankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val questionBankRepository: QuestionBankRepository,
    private val conflictResolver: ConflictResolver,
    private val offlineCache: OfflineCache
) {
    
    suspend fun syncOnAppOpen(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Enable network to sync
            firestore.enableNetwork()
            
            // Sync cached questions to Firestore
            val cachedQuestions = offlineCache.getCachedQuestions()
            cachedQuestions.forEach { question ->
                questionBankRepository.updateQuestion(question)
            }
            
            // Load latest from Firestore and update cache
            val result = questionBankRepository.getQuestions(limit = 1000)
            result.onSuccess { questions ->
                offlineCache.cacheQuestions(questions)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkForConflicts(): List<Conflict> = withContext(Dispatchers.IO) {
        val conflicts = mutableListOf<Conflict>()
        
        try {
            // Get cached questions
            val cachedQuestions = offlineCache.getCachedQuestions()
            
            // Check each cached question against server version
            cachedQuestions.forEach { localQuestion ->
                val serverResult = questionBankRepository.getQuestionById(localQuestion.id)
                serverResult.onSuccess { serverQuestion ->
                    // Check if there's a conflict (different updatedAt timestamps)
                    val localTime = localQuestion.updatedAt?.seconds ?: 0L
                    val serverTime = serverQuestion.updatedAt?.seconds ?: 0L
                    
                    if (localTime != serverTime && localTime < serverTime) {
                        // Conflict detected - local is older
                        conflicts.add(Conflict(localQuestion.id, localQuestion, serverQuestion))
                    }
                }
            }
        } catch (e: Exception) {
            // Return empty list on error
        }
        
        return@withContext conflicts
    }
    
    suspend fun resolveConflict(conflict: Conflict, keepLocal: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val questionToKeep = if (keepLocal) conflict.localVersion else conflict.serverVersion
            
            // Update in Firestore
            val result = questionBankRepository.updateQuestion(questionToKeep)
            
            // Update cache
            if (result.isSuccess) {
                offlineCache.cacheQuestions(listOf(questionToKeep))
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class Conflict(
    val questionId: String,
    val localVersion: com.gurukulaboard.models.Question,
    val serverVersion: com.gurukulaboard.models.Question
)

