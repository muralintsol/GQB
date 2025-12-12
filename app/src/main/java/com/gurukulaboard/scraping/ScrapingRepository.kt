package com.gurukulaboard.scraping

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.gurukulaboard.models.Question
import com.gurukulaboard.utils.Constants
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScrapingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun saveScrapingHistory(source: String, url: String, contentHash: String) {
        try {
            val history = mapOf(
                "source" to source,
                "url" to url,
                "lastScrapedAt" to Timestamp.now(),
                "contentHash" to contentHash
            )
            
            firestore.collection(Constants.COLLECTION_SCRAPING_HISTORY)
                .add(history)
                .await()
        } catch (e: Exception) {
            // Log error but don't fail scraping
        }
    }
    
    suspend fun getLastScrapingHash(source: String, url: String): String? {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_SCRAPING_HISTORY)
                .whereEqualTo("source", source)
                .whereEqualTo("url", url)
                .orderBy("lastScrapedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                snapshot.documents[0].getString("contentHash")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun checkDuplicateQuestion(content: String): Boolean {
        return try {
            // Generate hash of question content
            val contentHash = generateContentHash(content)
            
            // Check if question with similar content exists
            val snapshot = firestore.collection(Question.COLLECTION_NAME)
                .whereArrayContains("contentHash", contentHash)
                .limit(1)
                .get()
                .await()
            
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    fun generateContentHash(content: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(content.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

