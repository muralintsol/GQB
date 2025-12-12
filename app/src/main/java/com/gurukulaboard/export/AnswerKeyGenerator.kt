package com.gurukulaboard.export

import android.content.Context
import com.gurukulaboard.models.Question
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
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
            
            document.add(Paragraph("ANSWER KEY"))
            document.add(Paragraph("\n"))
            
            questions.forEachIndexed { index, question ->
                document.add(Paragraph("${index + 1}. ${question.answer ?: "N/A"}"))
            }
            
            document.close()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

