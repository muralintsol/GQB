package com.gurukulaboard.ncert

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTFileHelper @Inject constructor() {
    
    /**
     * Copy zip files from source directory to app's external files directory
     * This makes them accessible to the app
     */
    suspend fun copyZipFilesToAppStorage(
        context: Context,
        sourceDir: File,
        targetDir: File
    ): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!sourceDir.exists() || !sourceDir.isDirectory) {
                return@withContext Result.failure(Exception("Source directory does not exist: ${sourceDir.absolutePath}"))
            }
            
            targetDir.mkdirs()
            
            val zipFiles = sourceDir.listFiles { _, name -> name.endsWith(".zip", ignoreCase = true) }
                ?: emptyArray()
            
            var copiedCount = 0
            for (zipFile in zipFiles) {
                val targetFile = File(targetDir, zipFile.name)
                if (!targetFile.exists()) {
                    zipFile.copyTo(targetFile, overwrite = false)
                    copiedCount++
                }
            }
            
            Result.success(copiedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get the recommended directory for NCERT BOOKS
     */
    fun getNCERTBooksDirectory(context: Context): File {
        return File(context.getExternalFilesDir(null), "NCERT BOOKS")
    }
}

