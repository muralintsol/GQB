package com.gurukulaboard.ncert

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.gurukulaboard.ncert.models.NCERTBook
import com.gurukulaboard.ncert.models.NCERTBookStatus
import com.gurukulaboard.ncert.models.NCERTIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        const val BOOKS_COLLECTION = "ncertBooks"
        const val INDEXES_COLLECTION = "ncertIndexes"
        const val CONTENT_COLLECTION = "ncertContent"
    }
    
    // Book operations
    suspend fun saveBook(book: NCERTBook): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val bookWithTimestamp = book.copy(
                uploadedAt = book.uploadedAt ?: Timestamp.now(),
                processedAt = if (book.status == NCERTBookStatus.INDEXED) Timestamp.now() else null
            )
            val docRef = firestore.collection(BOOKS_COLLECTION)
                .add(bookWithTimestamp)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBook(bookId: String, book: NCERTBook): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            firestore.collection(BOOKS_COLLECTION)
                .document(bookId)
                .set(book.copy(id = bookId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBook(bookId: String): Result<NCERTBook> = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = firestore.collection(BOOKS_COLLECTION)
                .document(bookId)
                .get()
                .await()
            
            if (!doc.exists()) {
                return@withContext Result.failure(Exception("Book not found"))
            }
            
            val book = doc.toObject(NCERTBook::class.java)?.copy(id = doc.id)
            if (book != null) {
                Result.success(book)
            } else {
                Result.failure(Exception("Failed to parse book"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBooksBySubjectAndClass(subject: String, classLevel: Int): Result<List<NCERTBook>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = firestore.collection(BOOKS_COLLECTION)
                .whereEqualTo("subject", subject)
                .whereEqualTo("classLevel", classLevel)
                .get()
                .await()
            
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NCERTBook::class.java)?.copy(id = doc.id)
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllBooks(): Result<List<NCERTBook>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = firestore.collection(BOOKS_COLLECTION)
                .orderBy("subject")
                .orderBy("classLevel")
                .get()
                .await()
            
            val books = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NCERTBook::class.java)?.copy(id = doc.id)
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Index operations
    suspend fun saveIndex(index: NCERTIndex): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val docRef = firestore.collection(INDEXES_COLLECTION)
                .add(index)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getIndex(bookId: String): Result<NCERTIndex> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = firestore.collection(INDEXES_COLLECTION)
                .whereEqualTo("bookId", bookId)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return@withContext Result.failure(Exception("Index not found for book: $bookId"))
            }
            
            val index = snapshot.documents[0].toObject(NCERTIndex::class.java)
            if (index != null) {
                Result.success(index)
            } else {
                Result.failure(Exception("Failed to parse index"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateIndex(indexId: String, index: NCERTIndex): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            firestore.collection(INDEXES_COLLECTION)
                .document(indexId)
                .set(index)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Content cache operations
    suspend fun saveContentCache(
        bookId: String,
        chapterName: String,
        topicName: String?,
        subtopicName: String?,
        content: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val contentId = "${bookId}_${chapterName}_${topicName ?: ""}_${subtopicName ?: ""}"
            val contentData = mapOf(
                "bookId" to bookId,
                "chapterName" to chapterName,
                "topicName" to (topicName ?: ""),
                "subtopicName" to (subtopicName ?: ""),
                "content" to content,
                "cachedAt" to Timestamp.now()
            )
            
            firestore.collection(CONTENT_COLLECTION)
                .document(contentId)
                .set(contentData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContentCache(
        bookId: String,
        chapterName: String,
        topicName: String?,
        subtopicName: String?
    ): Result<String?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val contentId = "${bookId}_${chapterName}_${topicName ?: ""}_${subtopicName ?: ""}"
            val doc = firestore.collection(CONTENT_COLLECTION)
                .document(contentId)
                .get()
                .await()
            
            if (doc.exists()) {
                val content = doc.getString("content")
                Result.success(content)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

