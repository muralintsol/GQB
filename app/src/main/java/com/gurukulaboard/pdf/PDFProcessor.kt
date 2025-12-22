package com.gurukulaboard.pdf

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.gurukulaboard.pdf.models.ExtractedQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFProcessor @Inject constructor(
    private val storage: FirebaseStorage,
    private val pdfExtractor: PDFExtractor
) {
    
    suspend fun uploadPDF(uri: Uri, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.reference.child("pdfs/$fileName")
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun extractQuestionsFromUri(context: Context, uri: Uri): Result<List<ExtractedQuestion>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open PDF file"))
            
            val result = pdfExtractor.extractQuestions(inputStream)
            inputStream.close()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun extractQuestionsFromUrl(url: String): Result<List<ExtractedQuestion>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.getReferenceFromUrl(url)
            val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
            val inputStream = bytes.inputStream()
            
            val result = pdfExtractor.extractQuestions(inputStream)
            inputStream.close()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload NCERT PDF file to Firebase Storage
     */
    suspend fun uploadNCERTPDF(file: java.io.File, subject: String, classLevel: Int): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = file.name
            val storagePath = "ncert/${subject.lowercase()}/$classLevel/$fileName"
            val storageRef = storage.reference.child(storagePath)
            
            val uploadTask = storageRef.putFile(android.net.Uri.fromFile(file)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

