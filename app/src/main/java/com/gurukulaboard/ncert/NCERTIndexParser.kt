package com.gurukulaboard.ncert

import com.gurukulaboard.ncert.models.NCERTChapter
import com.gurukulaboard.ncert.models.NCERTIndex
import com.gurukulaboard.ncert.models.NCERTSubTopic
import com.gurukulaboard.ncert.models.NCERTTopic
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlin.math.maxOf
import kotlin.math.minOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTIndexParser @Inject constructor() {
    
    /**
     * Parse PDF index/contents page to extract hierarchical structure
     * Looks for table of contents typically in first few pages
     */
    suspend fun parseIndexFromPDF(pdfFile: File, bookId: String): Result<NCERTIndex> = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = FileInputStream(pdfFile)
            val result = parseIndexFromInputStream(inputStream, bookId)
            inputStream.close()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun parseIndexFromInputStream(pdfInputStream: InputStream, bookId: String): Result<NCERTIndex> = withContext(Dispatchers.IO) {
        return@withContext try {
            val reader = PdfReader(pdfInputStream)
            val pdfDoc = PdfDocument(reader)
            
            // Look for index in first 10 pages (typically in first few pages)
            val indexPages = minOf(10, pdfDoc.numberOfPages)
            var indexText = ""
            
            for (pageNum in 1..indexPages) {
                val page = pdfDoc.getPage(pageNum)
                val text = PdfTextExtractor.getTextFromPage(page)
                
                // Check if this looks like an index/contents page
                if (isIndexPage(text)) {
                    indexText += text + "\n"
                }
            }
            
            if (indexText.isEmpty()) {
                pdfDoc.close()
                reader.close()
                return@withContext Result.failure(Exception("No index/contents page found in PDF"))
            }
            
            val totalPages = pdfDoc.numberOfPages
            pdfDoc.close()
            reader.close()
            
            val chapters = parseChaptersFromIndexText(indexText, totalPages)
            val index = NCERTIndex(
                bookId = bookId,
                chapters = chapters,
                totalPages = totalPages
            )
            
            Result.success(index)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if text looks like an index/contents page
     */
    private fun isIndexPage(text: String): Boolean {
        val lowerText = text.lowercase()
        val indexKeywords = listOf(
            "contents", "index", "chapter", "unit", "syllabus",
            "chapter 1", "chapter 2", "1.", "2.", "unit 1", "unit 2"
        )
        
        return indexKeywords.any { keyword -> lowerText.contains(keyword) } &&
               (text.contains("page", ignoreCase = true) || 
                Regex("\\d+\\s*$").find(text) != null) // Ends with page numbers
    }
    
    /**
     * Parse chapters, topics, and subtopics from index text
     */
    private fun parseChaptersFromIndexText(indexText: String, totalPages: Int): List<NCERTChapter> {
        val chapters = mutableListOf<NCERTChapter>()
        val lines = indexText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        
        var currentChapter: NCERTChapter? = null
        var currentTopic: NCERTTopic? = null
        var chapterNumber = 0
        var topicOrder = 0
        var subtopicOrder = 0
        
        for (line in lines) {
            // Try to match chapter pattern
            val chapterMatch = Regex("""(?i)(?:chapter|unit|chapter\s*)?(\d+)[\.:]\s*(.+?)(?:\s+(\d+))?$""").find(line)
            if (chapterMatch != null) {
                // Save previous chapter
                currentChapter?.let { chapters.add(it) }
                
                chapterNumber++
                val chapterName = chapterMatch.groupValues[2].trim()
                val pageNum = chapterMatch.groupValues[3].toIntOrNull() ?: 0
                
                currentChapter = NCERTChapter(
                    name = chapterName,
                    number = chapterNumber,
                    startPage = pageNum,
                    endPage = pageNum, // Will be updated when next chapter is found
                    order = chapterNumber,
                    topics = mutableListOf()
                )
                currentTopic = null
                topicOrder = 0
                continue
            }
            
            // Try to match topic pattern (indented or numbered under chapter)
            val topicMatch = Regex("""(?i)^(\d+\.\d+|\d+\.|[a-z]\)|[ivx]+\))\s*(.+?)(?:\s+(\d+))?$""").find(line)
            if (topicMatch != null && currentChapter != null) {
                // Save previous topic
                if (currentTopic != null) {
                    val topics = currentChapter.topics.toMutableList()
                    topics.add(currentTopic!!)
                    currentChapter = currentChapter.copy(topics = topics)
                }
                
                topicOrder++
                val topicName = topicMatch.groupValues[2].trim()
                val pageNum = topicMatch.groupValues[3].toIntOrNull() ?: 0
                
                currentTopic = NCERTTopic(
                    name = topicName,
                    startPage = pageNum,
                    endPage = pageNum,
                    order = topicOrder,
                    subtopics = mutableListOf()
                )
                subtopicOrder = 0
                continue
            }
            
            // Try to match subtopic pattern (further indented)
            val subtopicMatch = Regex("""(?i)^(\d+\.\d+\.\d+|\d+\.\d+|[a-z]\)|[ivx]+\))\s*(.+?)(?:\s+(\d+))?$""").find(line)
            if (subtopicMatch != null && currentTopic != null) {
                subtopicOrder++
                val subtopicName = subtopicMatch.groupValues[2].trim()
                val pageNum = subtopicMatch.groupValues[3].toIntOrNull() ?: 0
                
                val subtopic = NCERTSubTopic(
                    name = subtopicName,
                    startPage = pageNum,
                    endPage = pageNum,
                    order = subtopicOrder
                )
                
                val subtopics = currentTopic.subtopics.toMutableList()
                subtopics.add(subtopic)
                currentTopic = currentTopic.copy(subtopics = subtopics)
                continue
            }
            
            // If line contains just a page number, it might be continuation
            val pageOnlyMatch = Regex("""^\s*(\d+)\s*$""").find(line)
            if (pageOnlyMatch != null && currentChapter != null) {
                val pageNum = pageOnlyMatch.groupValues[1].toIntOrNull() ?: 0
                if (currentTopic != null && currentTopic.endPage == currentTopic.startPage && pageNum > currentTopic.startPage) {
                    currentTopic = currentTopic.copy(endPage = pageNum)
                } else if (currentChapter.endPage == currentChapter.startPage && pageNum > currentChapter.startPage) {
                    currentChapter = currentChapter.copy(endPage = pageNum)
                }
            }
        }
        
        // Save last chapter and topic
        currentTopic?.let {
            if (currentChapter != null) {
                val topics = currentChapter.topics.toMutableList()
                topics.add(it)
                currentChapter = currentChapter.copy(topics = topics)
            }
        }
        currentChapter?.let { chapters.add(it) }
        
        // Update end pages - set each chapter's end page to next chapter's start page
        for (i in chapters.indices) {
            val endPage = if (i < chapters.size - 1) {
                chapters[i + 1].startPage - 1
            } else {
                totalPages
            }
            chapters[i] = chapters[i].copy(endPage = maxOf(chapters[i].startPage, endPage))
            
            // Update topic end pages within chapter
            val updatedTopics = chapters[i].topics.mapIndexed { topicIndex, topic ->
                val topicEndPage = if (topicIndex < chapters[i].topics.size - 1) {
                    chapters[i].topics[topicIndex + 1].startPage - 1
                } else {
                    chapters[i].endPage
                }
                topic.copy(endPage = maxOf(topic.startPage, topicEndPage))
            }
            chapters[i] = chapters[i].copy(topics = updatedTopics)
        }
        
        return chapters
    }
}

