package com.gurukulaboard.content

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentStorageManager @Inject constructor(
    private val storage: FirebaseStorage
) {
    
    suspend fun uploadPDF(uri: Uri, fileName: String, userId: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val timestamp = System.currentTimeMillis()
            val storagePath = "teaching-content/$userId/${timestamp}_$fileName"
            val storageRef = storage.reference.child(storagePath)
            
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadPDF(context: Context, url: String, localFileName: String): Result<File> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.getReferenceFromUrl(url)
            val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
            
            val downloadsDir = File(context.getExternalFilesDir(null), "downloads")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val localFile = File(downloadsDir, localFileName)
            FileOutputStream(localFile).use { output ->
                output.write(bytes)
            }
            
            Result.success(localFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePDF(fileUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.getReferenceFromUrl(fileUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFileSize(url: String): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val storageRef = storage.getReferenceFromUrl(url)
            val metadata = storageRef.metadata.await()
            Result.success(metadata.sizeBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

