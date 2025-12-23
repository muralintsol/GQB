package com.gurukulaboard.content

import android.content.Context
import android.net.Uri
import com.gurukulaboard.content.models.ContentSection
import com.gurukulaboard.models.Difficulty
import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionSource
import com.gurukulaboard.models.QuestionStatus
import com.gurukulaboard.models.QuestionType
import com.gurukulaboard.ncert.NCERTMCQGenerator
import com.gurukulaboard.pdf.PDFExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentMCQGenerator @Inject constructor(
    private val pdfExtractor: PDFExtractor,
    private val ncertMCQGenerator: NCERTMCQGenerator
) {
    
    data class GeneratedMCQ(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int, // Index of correct option (0-based)
        val explanation: String? = null
    )
    
    /**
     * Extract text from PDF content stored in Firebase Storage
     */
    suspend fun extractTextFromContent(
        context: Context,
        fileUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Download PDF from Firebase Storage
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .getReferenceFromUrl(fileUrl)
            val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
            val inputStream = bytes.inputStream()
            
            // Extract text from PDF
            val result = extractTextFromPDF(inputStream)
            inputStream.close()
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract text from PDF InputStream
     */
    private suspend fun extractTextFromPDF(inputStream: InputStream): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val reader = com.itextpdf.kernel.pdf.PdfReader(inputStream)
            val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(reader)
            val textBuilder = StringBuilder()
            
            for (pageNum in 1..pdfDoc.numberOfPages) {
                val page = pdfDoc.getPage(pageNum)
                val text = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(page)
                textBuilder.append(text).append("\n")
            }
            
            pdfDoc.close()
            reader.close()
            
            Result.success(textBuilder.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate MCQs from selected sections of content
     */
    suspend fun generateMCQsFromSections(
        context: Context,
        fileUrl: String,
        sections: List<ContentSection>,
        numberOfQuestions: Int,
        difficulty: Difficulty = Difficulty.MEDIUM,
        chapterName: String = "Content"
    ): Result<List<GeneratedMCQ>> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (sections.isEmpty()) {
                return@withContext Result.failure(Exception("No sections selected for MCQ generation."))
            }

            // Download PDF once
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .getReferenceFromUrl(fileUrl)
            val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
            
            // Save to temp file
            val tempFile = File(context.cacheDir, "temp_content_mcq_sections_${System.currentTimeMillis()}.pdf")
            FileOutputStream(tempFile).use { output ->
                output.write(bytes)
            }
            
            val textBuilder = StringBuilder()
            val reader = com.itextpdf.kernel.pdf.PdfReader(tempFile.inputStream())
            val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(reader)

            sections.sortedBy { it.startPage }.forEach { section ->
                // Extract text for each section
                val sectionText = extractTextFromPDFPages(pdfDoc, section.startPage, section.endPage)
                sectionText.onSuccess { text ->
                    textBuilder.append("<h2>${section.title}</h2>\n").append(text).append("\n\n")
                }
            }

            pdfDoc.close()
            reader.close()
            tempFile.delete() // Clean up temp file

            val combinedText = textBuilder.toString()
            generateMCQsFromText(combinedText, numberOfQuestions, difficulty, chapterName)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract text from specific page range in PDF
     */
    private suspend fun extractTextFromPDFPages(
        pdfDoc: com.itextpdf.kernel.pdf.PdfDocument,
        startPage: Int,
        endPage: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val textBuilder = StringBuilder()
            for (pageNum in startPage..endPage) {
                if (pageNum > 0 && pageNum <= pdfDoc.numberOfPages) {
                    val page = pdfDoc.getPage(pageNum)
                    val text = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(page)
                    textBuilder.append(text).append("\n")
                }
            }
            Result.success(textBuilder.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate MCQs from extracted text
     */
    suspend fun generateMCQsFromText(
        text: String,
        numberOfQuestions: Int,
        difficulty: Difficulty = Difficulty.MEDIUM,
        chapterName: String = "Content"
    ): Result<List<GeneratedMCQ>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = NCERTMCQGenerator.MCQGenerationRequest(
                content = text,
                chapterName = chapterName,
                numberOfQuestions = numberOfQuestions,
                difficulty = difficulty
            )
            
            val result = ncertMCQGenerator.generateMCQs(request)
            result.map { mcqs ->
                // Convert to our GeneratedMCQ format
                mcqs.map { ncertMCQ ->
                    GeneratedMCQ(
                        question = ncertMCQ.question,
                        options = ncertMCQ.options,
                        correctAnswer = ncertMCQ.correctAnswer,
                        explanation = ncertMCQ.explanation
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convert GeneratedMCQ to Question model for saving to question bank
     */
    fun toQuestion(
        generatedMCQ: GeneratedMCQ,
        subject: String,
        classLevel: Int,
        chapter: String?,
        contentId: String,
        createdBy: String
    ): Question {
        return Question(
            content = generatedMCQ.question,
            type = QuestionType.MCQ,
            subject = subject,
            `class` = classLevel,
            chapter = chapter ?: "",
            difficulty = Difficulty.MEDIUM,
            examType = com.gurukulaboard.models.ExamType.PU_BOARD,
            source = QuestionSource.MANUAL,
            sourceDetails = "teacherContent:$contentId",
            status = QuestionStatus.APPROVED, // Auto-approve teacher-generated MCQs
            createdBy = createdBy,
            answer = generatedMCQ.options[generatedMCQ.correctAnswer],
            options = generatedMCQ.options
        )
    }
}

