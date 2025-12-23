package com.gurukulaboard.content

import android.content.Context
import com.gurukulaboard.content.models.ContentSection
import com.gurukulaboard.content.models.SectionType
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFContentAnalyzer @Inject constructor() {
    
    /**
     * Analyze PDF structure to detect subtopics and exercises
     */
    suspend fun analyzePDF(
        context: Context,
        fileUrl: String
    ): Result<List<ContentSection>> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Download PDF from Firebase Storage
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .getReferenceFromUrl(fileUrl)
            val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
            val inputStream = bytes.inputStream()
            
            // Analyze PDF
            val result = analyzePDFFromStream(inputStream)
            inputStream.close()
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun analyzePDFFromStream(inputStream: java.io.InputStream): Result<List<ContentSection>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val reader = PdfReader(inputStream)
            val pdfDoc = PdfDocument(reader)
            
            val sections = mutableListOf<ContentSection>()
            val pageTexts = mutableMapOf<Int, String>()
            
            // Extract text from all pages with page numbers
            for (pageNum in 1..pdfDoc.numberOfPages) {
                val page = pdfDoc.getPage(pageNum)
                val text = PdfTextExtractor.getTextFromPage(page)
                pageTexts[pageNum] = text
            }
            
            // Combine all text for analysis
            val fullText = pageTexts.values.joinToString("\n")
            
            // Detect subtopics
            val subtopics = detectSubtopicSections(fullText, pageTexts)
            sections.addAll(subtopics)
            
            // Detect exercises
            val exercises = detectExerciseSections(fullText, pageTexts)
            sections.addAll(exercises)
            
            pdfDoc.close()
            reader.close()
            
            Result.success(sections.sortedBy { it.startPage })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Detect subtopic sections from PDF text
     */
    private fun detectSubtopicSections(
        fullText: String,
        pageTexts: Map<Int, String>
    ): List<ContentSection> {
        val sections = mutableListOf<ContentSection>()
        
        // Patterns for subtopics
        val subtopicPatterns = listOf(
            Regex("""(\d+\.\d+)\s+(.+?)(?=\d+\.\d+|Exercise|EXERCISE|Chapter|\n\n)""", RegexOption.DOT_MATCHES_ALL),
            Regex("""(Sub-topic|Subsection|Sub-topic)\s*:?\s*(.+?)(?=Sub-topic|Exercise|EXERCISE|\n\n)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+\.\d+\.\d+)\s+(.+?)(?=\d+\.\d+\.\d+|Exercise|EXERCISE|\n\n)""", RegexOption.DOT_MATCHES_ALL)
        )
        
        var sectionIndex = 0
        for (pattern in subtopicPatterns) {
            val matches = pattern.findAll(fullText)
            matches.forEach { match ->
                val number = match.groupValues[1]
                val title = match.groupValues.getOrNull(2)?.trim() ?: number
                
                // Find page range for this section
                val matchStart = match.range.first
                val pageRange = findPageRangeForPosition(matchStart, fullText, pageTexts)
                
                if (pageRange != null) {
                    val preview = extractPreview(match.value, 150)
                    val section = ContentSection(
                        id = ContentSection.createId(SectionType.SUBTOPIC, sectionIndex++),
                        title = "$number $title".take(100),
                        type = SectionType.SUBTOPIC,
                        startPage = pageRange.first,
                        endPage = pageRange.second,
                        pageRange = "${pageRange.first}-${pageRange.second}",
                        preview = preview
                    )
                    sections.add(section)
                }
            }
        }
        
        return sections.distinctBy { it.startPage }
    }
    
    /**
     * Detect exercise sections from PDF text
     */
    private fun detectExerciseSections(
        fullText: String,
        pageTexts: Map<Int, String>
    ): List<ContentSection> {
        val sections = mutableListOf<ContentSection>()
        
        // Patterns for exercises
        val exercisePatterns = listOf(
            Regex("""(Exercise\s*\d+|EXERCISE\s*\d+)\s*:?\s*(.+?)(?=Exercise|EXERCISE|Chapter|\n\n)""", RegexOption.IGNORE_CASE),
            Regex("""(Exercise|EXERCISE)\s*:?\s*(.+?)(?=Exercise|EXERCISE|Chapter|\n\n)""", RegexOption.IGNORE_CASE),
            Regex("""(Problems?\s*\d+|Questions?\s*\d+)\s*:?\s*(.+?)(?=Problem|Question|Exercise|Chapter|\n\n)""", RegexOption.IGNORE_CASE),
            Regex("""(Practice\s*Problems?|Practice\s*Questions?)\s*:?\s*(.+?)(?=Practice|Exercise|Chapter|\n\n)""", RegexOption.IGNORE_CASE)
        )
        
        var sectionIndex = 0
        for (pattern in exercisePatterns) {
            val matches = pattern.findAll(fullText)
            matches.forEach { match ->
                val exerciseTitle = match.groupValues[1].trim()
                val description = match.groupValues.getOrNull(2)?.trim() ?: ""
                
                // Find page range for this section
                val matchStart = match.range.first
                val pageRange = findPageRangeForPosition(matchStart, fullText, pageTexts)
                
                if (pageRange != null) {
                    val preview = extractPreview(match.value, 150)
                    val section = ContentSection(
                        id = ContentSection.createId(SectionType.EXERCISE, sectionIndex++),
                        title = exerciseTitle.take(100),
                        type = SectionType.EXERCISE,
                        startPage = pageRange.first,
                        endPage = pageRange.second,
                        pageRange = "${pageRange.first}-${pageRange.second}",
                        preview = preview
                    )
                    sections.add(section)
                }
            }
        }
        
        return sections.distinctBy { it.startPage }
    }
    
    /**
     * Find page range for a given text position
     */
    private fun findPageRangeForPosition(
        position: Int,
        fullText: String,
        pageTexts: Map<Int, String>
    ): Pair<Int, Int>? {
        var currentPos = 0
        var startPage = 1
        var endPage = 1
        
        // Find start page
        for ((pageNum, pageText) in pageTexts.toSortedMap()) {
            if (currentPos <= position && position < currentPos + pageText.length) {
                startPage = pageNum
                break
            }
            currentPos += pageText.length + 1 // +1 for newline
        }
        
        // Estimate end page (next section or end of document)
        // For simplicity, assume each section spans 2-5 pages
        endPage = minOf(startPage + 4, pageTexts.keys.maxOrNull() ?: startPage)
        
        return Pair(startPage, endPage)
    }
    
    /**
     * Extract preview text from content
     */
    private fun extractPreview(text: String, maxLength: Int): String {
        val cleaned = text.replace(Regex("\\s+"), " ").trim()
        return if (cleaned.length > maxLength) {
            cleaned.take(maxLength) + "..."
        } else {
            cleaned
        }
    }
}

