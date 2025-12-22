package com.gurukulaboard.export

import android.content.Context
import com.gurukulaboard.models.Question
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerKeyGenerator @Inject constructor() {
    
    suspend fun generateAnswerKey(
        context: Context,
        questions: List<Question>,
        fileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(context.getExternalFilesDir(null), fileName)
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            // Title
            val title = Paragraph("ANSWER KEY")
                .setFontSize(18f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
            document.add(title)
            
            document.add(Paragraph("\n"))
            
            // Answers
            questions.forEachIndexed { index, question ->
                val answerText = if (question.answer != null) {
                    "Q${index + 1}. Answer: ${question.answer}"
                } else {
                    "Q${index + 1}. Answer: Not provided"
                }
                val answer = Paragraph(answerText)
                    .setFontSize(12f)
                    .setMarginBottom(8f)
                document.add(answer)
            }
            
            document.close()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

