package com.gurukulaboard.ncert

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTStorageManager @Inject constructor(
    private val storage: FirebaseStorage
) {
    
    /**
     * Upload NCERT PDF file to Firebase Storage
     * Path: ncert/{subject}/{class}/{filename}
     */
    suspend fun uploadNCERTPDF(
        file: File,
        subject: String,
        classLevel: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = file.name
            val storagePath = "ncert/${subject.lowercase()}/${classLevel}/$fileName"
            val storageRef = storage.reference.child(storagePath)
            
            val uploadTask = storageRef.putFile(android.net.Uri.fromFile(file)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download NCERT PDF from Firebase Storage
     */
    suspend fun downloadNCERTPDF(
        downloadUrl: String,
        destinationFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
            
            destinationFile.parentFile?.mkdirs()
            destinationFile.writeBytes(bytes)
            
            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete NCERT PDF from Firebase Storage
     */
    suspend fun deleteNCERTPDF(downloadUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get file metadata from Firebase Storage
     */
    suspend fun getFileMetadata(downloadUrl: String): Result<com.google.firebase.storage.FileMetadata> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            val metadata = storageRef.metadata.await()
            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

