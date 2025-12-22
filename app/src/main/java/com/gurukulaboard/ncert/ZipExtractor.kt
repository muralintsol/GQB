package com.gurukulaboard.ncert

import com.gurukulaboard.ncert.models.NCERTBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZipExtractor @Inject constructor() {
    
    data class ExtractedPDF(
        val file: File,
        val subject: String,
        val classLevel: Int,
        val language: String,
        val originalZipName: String
    )
    
    /**
     * Extract all PDFs from zip files in the NCERT BOOKS folder
     * Maps zip file names to subjects/classes based on naming convention:
     * - kebo1dd.zip = Biology (bo) Class 11 (1), English (ke)
     * - lech2dd.zip = Chemistry (ch) Class 12 (2), English (le)
     * - kemh1dd.zip = Mathematics (mh) Class 11 (1), English (ke)
     * - keph1dd.zip = Physics (ph) Class 11 (1), English (ke)
     */
    suspend fun extractPDFsFromZipFiles(ncertBooksFolder: File): Result<List<ExtractedPDF>> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!ncertBooksFolder.exists() || !ncertBooksFolder.isDirectory) {
                return@withContext Result.failure(Exception("NCERT BOOKS folder not found: ${ncertBooksFolder.absolutePath}"))
            }
            
            val zipFiles = ncertBooksFolder.listFiles { _, name -> name.endsWith(".zip", ignoreCase = true) }
                ?: emptyArray()
            
            if (zipFiles.isEmpty()) {
                return@withContext Result.failure(Exception("No zip files found in ${ncertBooksFolder.absolutePath}"))
            }
            
            val extractedPDFs = mutableListOf<ExtractedPDF>()
            val tempExtractDir = File(ncertBooksFolder, "temp_extract")
            tempExtractDir.mkdirs()
            
            for (zipFile in zipFiles) {
                val zipInfo = parseZipFileName(zipFile.name)
                if (zipInfo == null) {
                    continue // Skip files that don't match naming convention
                }
                
                val result = extractPDFsFromZip(zipFile, tempExtractDir, zipInfo)
                extractedPDFs.addAll(result)
            }
            
            Result.success(extractedPDFs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractPDFsFromZip(
        zipFile: File,
        extractDir: File,
        zipInfo: ZipInfo
    ): List<ExtractedPDF> {
        val extractedPDFs = mutableListOf<ExtractedPDF>()
        val subjectExtractDir = File(extractDir, "${zipInfo.subject}_${zipInfo.classLevel}")
        subjectExtractDir.mkdirs()
        
        ZipArchiveInputStream(FileInputStream(zipFile)).use { zipInputStream ->
            var entry: ZipArchiveEntry? = zipInputStream.nextEntry
            
            while (entry != null) {
                if (entry.name.endsWith(".pdf", ignoreCase = true)) {
                    val outputFile = File(subjectExtractDir, entry.name)
                    outputFile.parentFile?.mkdirs()
                    
                    FileOutputStream(outputFile).use { outputStream ->
                        zipInputStream.copyTo(outputStream)
                    }
                    
                    extractedPDFs.add(
                        ExtractedPDF(
                            file = outputFile,
                            subject = zipInfo.subject,
                            classLevel = zipInfo.classLevel,
                            language = zipInfo.language,
                            originalZipName = zipFile.name
                        )
                    )
                }
                entry = zipInputStream.nextEntry
            }
        }
        
        return extractedPDFs
    }
    
    private data class ZipInfo(
        val subject: String,
        val classLevel: Int,
        val language: String
    )
    
    /**
     * Parse zip file name to extract subject, class, and language
     * Format: {lang}{subject}{class}dd.zip
     * Examples:
     * - kebo1dd.zip = English Biology Class 11
     * - lech2dd.zip = English Chemistry Class 12
     * - kemh1dd.zip = English Mathematics Class 11
     * - keph1dd.zip = English Physics Class 11
     */
    private fun parseZipFileName(fileName: String): ZipInfo? {
        val nameWithoutExt = fileName.replace(".zip", "", ignoreCase = true)
        
        // Pattern: {lang}{subject}{class}dd
        // lang: ke (English), le (English alternative)
        // subject: bo (Biology), ch (Chemistry), mh (Mathematics), ph (Physics)
        // class: 1 (Class 11), 2 (Class 12)
        
        val subjectMap = mapOf(
            "bo" to "Biology",
            "ch" to "Chemistry",
            "mh" to "Mathematics",
            "ph" to "Physics"
        )
        
        // Try to match pattern
        val pattern = Regex("^(ke|le)(bo|ch|mh|ph)([12])dd$", RegexOption.IGNORE_CASE)
        val match = pattern.find(nameWithoutExt) ?: return null
        
        val language = when (match.groupValues[1].lowercase()) {
            "ke", "le" -> "English"
            else -> "English" // Default
        }
        
        val subjectCode = match.groupValues[2].lowercase()
        val subject = subjectMap[subjectCode] ?: return null
        
        val classLevel = when (match.groupValues[3]) {
            "1" -> 11
            "2" -> 12
            else -> return null
        }
        
        return ZipInfo(subject, classLevel, language)
    }
    
    /**
     * Create NCERTBook from extracted PDF
     */
    fun createNCERTBook(extractedPDF: ExtractedPDF): NCERTBook {
        return NCERTBook(
            subject = extractedPDF.subject,
            classLevel = extractedPDF.classLevel,
            language = extractedPDF.language,
            fileName = extractedPDF.file.name,
            fileSize = extractedPDF.file.length(),
            localFilePath = extractedPDF.file.absolutePath,
            status = NCERTBook.NCERTBookStatus.PENDING
        )
    }
}

