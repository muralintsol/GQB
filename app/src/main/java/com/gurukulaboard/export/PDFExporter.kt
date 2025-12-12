package com.gurukulaboard.export

import android.content.Context
import com.gurukulaboard.models.HeaderFooterConfig
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
class PDFExporter @Inject constructor() {
    
    suspend fun exportQuestionPaper(
        context: Context,
        questions: List<Question>,
        fileName: String,
        headerFooter: HeaderFooterConfig
    ): Result<File> = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(context.getExternalFilesDir(null), fileName)
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            // Add header
            if (headerFooter.schoolName.isNotBlank()) {
                document.add(Paragraph(headerFooter.schoolName))
            }
            if (headerFooter.date.isNotBlank()) {
                document.add(Paragraph(headerFooter.date))
            }
            if (headerFooter.subject.isNotBlank()) {
                document.add(Paragraph("Subject: ${headerFooter.subject}"))
            }
            
            document.add(Paragraph("\n"))
            
            // Add questions in compact mode
            questions.forEachIndexed { index, question ->
                document.add(Paragraph("${index + 1}. ${question.content}"))
                
                question.options?.forEachIndexed { optIndex, option ->
                    document.add(Paragraph("   ${('a' + optIndex)}. $option"))
                }
                
                document.add(Paragraph("\n"))
            }
            
            // Add footer
            if (headerFooter.examType.isNotBlank()) {
                document.add(Paragraph("\n${headerFooter.examType}"))
            }
            
            document.close()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

