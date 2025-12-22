package com.gurukulaboard.pdf

import com.gurukulaboard.models.Difficulty
import com.gurukulaboard.models.QuestionType
import com.gurukulaboard.ncert.models.NCERTIndex
import com.gurukulaboard.pdf.models.ExtractedQuestion
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlin.math.maxOf
import kotlin.math.minOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFExtractor @Inject constructor() {
    
    suspend fun extractQuestions(pdfInputStream: InputStream): Result<List<ExtractedQuestion>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val reader = PdfReader(pdfInputStream)
            val pdfDoc = PdfDocument(reader)
            val questions = mutableListOf<ExtractedQuestion>()
            
            for (pageNum in 1..pdfDoc.numberOfPages) {
                val page = pdfDoc.getPage(pageNum)
                val text = extractTextFromPage(page)
                
                val extracted = parseQuestionsFromText(text, pageNum)
                questions.addAll(extracted)
            }
            
            pdfDoc.close()
            reader.close()
            
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractTextFromPage(page: com.itextpdf.kernel.pdf.PdfPage): String {
        // Extract text using iText's text extraction
        // This is a simplified version - enhance based on PDF structure
        return try {
            com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(page)
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun parseQuestionsFromText(text: String, pageNumber: Int): List<ExtractedQuestion> {
        val questions = mutableListOf<ExtractedQuestion>()
        val lines = text.split("\n")
        
        var currentQuestion: StringBuilder? = null
        var currentOptions = mutableListOf<String>()
        var questionNumber = 1
        
        for (line in lines) {
            val trimmed = line.trim()
            
            if (trimmed.isEmpty()) continue
            
            // Detect question start (common patterns: "Q1.", "1.", "(1)", etc.)
            if (isQuestionStart(trimmed)) {
                // Save previous question if exists
                currentQuestion?.let { q ->
                    questions.add(createExtractedQuestion(q.toString(), currentOptions, questionNumber++, pageNumber))
                }
                
                currentQuestion = StringBuilder(trimmed)
                currentOptions.clear()
            } else if (trimmed.matches(Regex("^[a-zA-Z]\\)|^[a-zA-Z]\\.|^[ivx]+\\)"))) {
                // Detect option (a), b), etc.)
                currentOptions.add(trimmed)
                currentQuestion?.append("\n$trimmed")
            } else if (currentQuestion != null) {
                // Continue question text
                currentQuestion.append(" $trimmed")
            }
        }
        
        // Add last question
        currentQuestion?.let { q ->
            questions.add(createExtractedQuestion(q.toString(), currentOptions, questionNumber, pageNumber))
        }
        
        return questions
    }
    
    private fun isQuestionStart(line: String): Boolean {
        // Patterns: Q1., 1., (1), Question 1, etc.
        return line.matches(Regex("^(Q\\d+|\\d+|\\(\\d+\\)|Question\\s+\\d+)[\\.:]?\\s+.*", RegexOption.IGNORE_CASE))
    }
    
    private fun createExtractedQuestion(
        content: String,
        options: List<String>,
        questionNumber: Int,
        pageNumber: Int
    ): ExtractedQuestion {
        val questionType = if (options.isNotEmpty()) QuestionType.MCQ else QuestionType.SHORT_ANSWER
        
        return ExtractedQuestion(
            content = content,
            type = questionType,
            options = if (options.isNotEmpty()) options else null,
            pageNumber = pageNumber
        )
    }
    
    /**
     * Extract text content from specific page range
     * Used for extracting content from chapters/topics/subtopics
     */
    suspend fun extractTextFromPages(
        pdfInputStream: InputStream,
        startPage: Int,
        endPage: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val reader = PdfReader(pdfInputStream)
            val pdfDoc = PdfDocument(reader)
            
            val textBuilder = StringBuilder()
            val actualStartPage = maxOf(1, startPage)
            val actualEndPage = minOf(endPage, pdfDoc.numberOfPages)
            
            for (pageNum in actualStartPage..actualEndPage) {
                val page = pdfDoc.getPage(pageNum)
                val pageText = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(page)
                textBuilder.append(pageText)
                if (pageNum < actualEndPage) {
                    textBuilder.append("\n\n")
                }
            }
            
            pdfDoc.close()
            reader.close()
            
            Result.success(textBuilder.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

