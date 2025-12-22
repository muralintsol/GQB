package com.gurukulaboard.export

import android.content.Context
import com.gurukulaboard.models.HeaderFooterConfig
import com.gurukulaboard.models.Question
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
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
            
            // Add header with better formatting
            if (headerFooter.schoolName.isNotBlank()) {
                val schoolName = Paragraph(headerFooter.schoolName)
                    .setFontSize(18f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                document.add(schoolName)
            }
            
            if (headerFooter.date.isNotBlank() || headerFooter.subject.isNotBlank() || headerFooter.classLevel.isNotBlank()) {
                val headerInfo = Paragraph()
                if (headerFooter.date.isNotBlank()) {
                    headerInfo.add(Text("Date: ${headerFooter.date}\n"))
                }
                if (headerFooter.subject.isNotBlank()) {
                    headerInfo.add(Text("Subject: ${headerFooter.subject}\n"))
                }
                if (headerFooter.classLevel.isNotBlank()) {
                    headerInfo.add(Text("Class: ${headerFooter.classLevel}\n"))
                }
                headerInfo.setTextAlignment(TextAlignment.CENTER)
                document.add(headerInfo)
            }
            
            document.add(Paragraph("\n").setMarginBottom(20f))
            
            // Add questions with better formatting
            questions.forEachIndexed { index, question ->
                val questionText = Paragraph("${index + 1}. ${question.content}")
                    .setFontSize(12f)
                    .setMarginBottom(8f)
                document.add(questionText)
                
                question.options?.forEachIndexed { optIndex, option ->
                    val optionText = Paragraph("   ${('a' + optIndex)}. $option")
                        .setFontSize(11f)
                        .setMarginLeft(20f)
                        .setMarginBottom(4f)
                    document.add(optionText)
                }
                
                // Add space between questions
                document.add(Paragraph("\n").setMarginBottom(10f))
            }
            
            // Add footer
            document.add(Paragraph("\n").setMarginTop(20f))
            if (headerFooter.examType.isNotBlank()) {
                val footer = Paragraph("Exam Type: ${headerFooter.examType}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                document.add(footer)
            }
            
            document.close()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

