package com.gurukulaboard.ncert

import com.gurukulaboard.ncert.models.NCERTChapter
import com.gurukulaboard.ncert.models.NCERTSubTopic
import com.gurukulaboard.ncert.models.NCERTTopic
import com.gurukulaboard.pdf.PDFExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTContentExtractor @Inject constructor(
    private val pdfExtractor: PDFExtractor,
    private val repository: NCERTRepository
) {
    
    /**
     * Extract text content from a chapter
     */
    suspend fun extractChapterContent(
        pdfFile: File,
        bookId: String,
        chapter: NCERTChapter
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Check cache first
            val cached = repository.getContentCache(bookId, chapter.name, null, null)
            cached.getOrNull()?.let {
                return@withContext Result.success(it)
            }
            
            // Extract from PDF
            val inputStream = FileInputStream(pdfFile)
            val result = pdfExtractor.extractTextFromPages(
                inputStream,
                chapter.startPage,
                chapter.endPage
            )
            inputStream.close()
            
            result.onSuccess { content ->
                // Cache the content
                repository.saveContentCache(bookId, chapter.name, null, null, content)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract text content from a topic
     */
    suspend fun extractTopicContent(
        pdfFile: File,
        bookId: String,
        chapter: NCERTChapter,
        topic: NCERTTopic
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Check cache first
            val cached = repository.getContentCache(bookId, chapter.name, topic.name, null)
            cached.getOrNull()?.let {
                return@withContext Result.success(it)
            }
            
            // Extract from PDF
            val inputStream = FileInputStream(pdfFile)
            val result = pdfExtractor.extractTextFromPages(
                inputStream,
                topic.startPage,
                topic.endPage
            )
            inputStream.close()
            
            result.onSuccess { content ->
                // Cache the content
                repository.saveContentCache(bookId, chapter.name, topic.name, null, content)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract text content from a subtopic
     */
    suspend fun extractSubtopicContent(
        pdfFile: File,
        bookId: String,
        chapter: NCERTChapter,
        topic: NCERTTopic,
        subtopic: NCERTSubTopic
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Check cache first
            val cached = repository.getContentCache(bookId, chapter.name, topic.name, subtopic.name)
            cached.getOrNull()?.let {
                return@withContext Result.success(it)
            }
            
            // Extract from PDF
            val inputStream = FileInputStream(pdfFile)
            val result = pdfExtractor.extractTextFromPages(
                inputStream,
                subtopic.startPage,
                subtopic.endPage
            )
            inputStream.close()
            
            result.onSuccess { content ->
                // Cache the content
                repository.saveContentCache(bookId, chapter.name, topic.name, subtopic.name, content)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean and format extracted text
     */
    fun cleanExtractedText(text: String): String {
        return text
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .replace(Regex("\n{3,}"), "\n\n") // Replace multiple newlines with double newline
            .trim()
    }
}

