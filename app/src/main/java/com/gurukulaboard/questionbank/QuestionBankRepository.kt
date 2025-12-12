package com.gurukulaboard.questionbank

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gurukulaboard.models.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionBankRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun getQuestions(
        examType: ExamType? = null,
        subject: String? = null,
        classLevel: Int? = null,
        chapter: String? = null,
        difficulty: Difficulty? = null,
        questionType: QuestionType? = null,
        status: QuestionStatus? = null,
        searchQuery: String? = null,
        limit: Int = 50
    ): Result<List<Question>> {
        return try {
            var query: Query = firestore.collection(Question.COLLECTION_NAME)
            
            examType?.let { query = query.whereEqualTo("examType", it.name) }
            subject?.let { query = query.whereEqualTo("subject", it) }
            classLevel?.let { query = query.whereEqualTo("class", it) }
            chapter?.let { query = query.whereEqualTo("chapter", it) }
            difficulty?.let { query = query.whereEqualTo("difficulty", it.name) }
            questionType?.let { query = query.whereEqualTo("type", it.name) }
            status?.let { query = query.whereEqualTo("status", it.name) }
            
            query = query.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
            
            val snapshot = query.get().await()
            val questions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Question::class.java)?.copy(id = doc.id)
            }
            
            // Filter by search query if provided
            val filteredQuestions = if (searchQuery != null && searchQuery.isNotBlank()) {
                questions.filter { question ->
                    question.content.contains(searchQuery, ignoreCase = true) ||
                    question.chapter.contains(searchQuery, ignoreCase = true) ||
                    question.subject.contains(searchQuery, ignoreCase = true)
                }
            } else {
                questions
            }
            
            Result.success(filteredQuestions)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getQuestionById(questionId: String): Result<Question> {
        return try {
            val document = firestore.collection(Question.COLLECTION_NAME)
                .document(questionId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("Question not found"))
            }
            
            val question = document.toObject(Question::class.java)
            if (question == null) {
                return Result.failure(Exception("Invalid question data"))
            }
            
            Result.success(question.copy(id = document.id))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createQuestion(question: Question): Result<String> {
        return try {
            val questionWithTimestamp = question.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            val docRef = firestore.collection(Question.COLLECTION_NAME)
                .add(questionWithTimestamp)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateQuestion(question: Question): Result<Unit> {
        return try {
            if (question.id.isEmpty()) {
                return Result.failure(Exception("Question ID is required"))
            }
            
            val questionWithTimestamp = question.copy(
                updatedAt = Timestamp.now()
            )
            
            val dataMap = mapOf(
                "content" to questionWithTimestamp.content,
                "type" to questionWithTimestamp.type.name,
                "subject" to questionWithTimestamp.subject,
                "class" to questionWithTimestamp.`class`,
                "chapter" to questionWithTimestamp.chapter,
                "difficulty" to questionWithTimestamp.difficulty.name,
                "examType" to questionWithTimestamp.examType.name,
                "source" to questionWithTimestamp.source.name,
                "sourceDetails" to questionWithTimestamp.sourceDetails,
                "status" to questionWithTimestamp.status.name,
                "answer" to questionWithTimestamp.answer,
                "options" to questionWithTimestamp.options,
                "updatedAt" to questionWithTimestamp.updatedAt
            )
            
            firestore.collection(Question.COLLECTION_NAME)
                .document(question.id)
                .update(dataMap)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteQuestion(questionId: String): Result<Unit> {
        return try {
            firestore.collection(Question.COLLECTION_NAME)
                .document(questionId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun approveQuestion(questionId: String, approvedBy: String): Result<Unit> {
        return try {
            firestore.collection(Question.COLLECTION_NAME)
                .document(questionId)
                .update(
                    mapOf(
                        "status" to QuestionStatus.APPROVED.name,
                        "approvedBy" to approvedBy,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectQuestion(questionId: String): Result<Unit> {
        return try {
            firestore.collection(Question.COLLECTION_NAME)
                .document(questionId)
                .update(
                    mapOf(
                        "status" to QuestionStatus.REJECTED.name,
                        "updatedAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

