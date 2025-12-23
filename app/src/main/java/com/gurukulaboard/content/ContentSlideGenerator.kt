package com.gurukulaboard.content

import android.content.Context
import com.gurukulaboard.content.models.SlideData
import com.gurukulaboard.content.models.SlideType
import com.gurukulaboard.content.models.TeacherPPT
import com.gurukulaboard.ncert.NCERTSlideGenerator
import com.gurukulaboard.ncert.NCERTSlideTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentSlideGenerator @Inject constructor(
    private val contentMCQGenerator: ContentMCQGenerator,
    private val ncertSlideGenerator: NCERTSlideGenerator
) {
    
    data class ContentMetadata(
        val title: String,
        val subject: String,
        val classLevel: Int,
        val chapter: String?
    )
    
    /**
     * Generate slides from content stored in Firebase Storage
     */
    suspend fun generateSlidesFromContent(
        context: Context,
        fileUrl: String,
        metadata: ContentMetadata,
        contentId: String,
        createdBy: String
    ): Result<TeacherPPT> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Extract text from PDF
            val textResult = contentMCQGenerator.extractTextFromContent(context, fileUrl)
            textResult.fold(
                onSuccess = { text ->
                    generateSlidesFromText(text, metadata, contentId, createdBy)
                },
                onFailure = { exception ->
                    Result.failure<TeacherPPT>(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure<TeacherPPT>(e)
        }
    }
    
    /**
     * Generate slides from selected sections of content
     */
    suspend fun generateSlidesFromSections(
        context: Context,
        fileUrl: String,
        sections: List<com.gurukulaboard.content.models.ContentSection>,
        metadata: ContentMetadata,
        contentId: String,
        createdBy: String
    ): Result<TeacherPPT> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (sections.isEmpty()) {
                return@withContext Result.failure<TeacherPPT>(Exception("No sections selected for PPT generation."))
            }

            // Download PDF once
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .getReferenceFromUrl(fileUrl)
            val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
            
            // Save to temp file
            val tempFile = java.io.File(context.cacheDir, "temp_content_ppt_sections_${System.currentTimeMillis()}.pdf")
            java.io.FileOutputStream(tempFile).use { output ->
                output.write(bytes)
            }
            
            val textBuilder = StringBuilder()
            val reader = com.itextpdf.kernel.pdf.PdfReader(tempFile.inputStream())
            val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(reader)

            sections.sortedBy { it.startPage }.forEach { section ->
                // Extract text for each section
                for (pageNum in section.startPage..section.endPage) {
                    if (pageNum > 0 && pageNum <= pdfDoc.numberOfPages) {
                        val page = pdfDoc.getPage(pageNum)
                        val text = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(page)
                        textBuilder.append(text).append("\n")
                    }
                }
                textBuilder.append("\n\n")
            }

            pdfDoc.close()
            reader.close()
            tempFile.delete() // Clean up temp file

            val combinedText = textBuilder.toString()
            generateSlidesFromText(combinedText, metadata, contentId, createdBy)

        } catch (e: Exception) {
            Result.failure<TeacherPPT>(e)
        }
    }
    
    /**
     * Generate slides from text content
     */
    suspend fun generateSlidesFromText(
        text: String,
        metadata: ContentMetadata,
        contentId: String,
        createdBy: String
    ): Result<TeacherPPT> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Use NCERT slide generator to create HTML
            val htmlResult = ncertSlideGenerator.generateSlides(
                content = text,
                title = metadata.title,
                subject = metadata.subject,
                chapter = metadata.chapter ?: "General"
            )
            
            htmlResult.map { htmlContent ->
                // Parse HTML to extract slide data
                val slides = parseSlidesFromHTML(htmlContent, text)
                
                TeacherPPT(
                    title = metadata.title,
                    contentId = contentId,
                    htmlContent = htmlContent,
                    slides = slides,
                    subject = metadata.subject,
                    classLevel = metadata.classLevel,
                    chapter = metadata.chapter,
                    createdBy = createdBy
                )
            }
        } catch (e: Exception) {
            Result.failure<TeacherPPT>(e)
        }
    }
    
    /**
     * Parse slides from HTML content
     */
    private fun parseSlidesFromHTML(htmlContent: String, originalText: String): List<SlideData> {
        val slides = mutableListOf<SlideData>()
        
        // Split HTML by slide markers (div.slide or similar)
        val slidePattern = Regex("""<div[^>]*class="slide"[^>]*>(.*?)</div>""", RegexOption.DOT_MATCHES_ALL)
        val slideMatches = slidePattern.findAll(htmlContent)
        
        var slideNumber = 1
        slideMatches.forEach { match ->
            val slideHtml = match.groupValues[1]
            val title = extractTitleFromSlide(slideHtml)
            val content = extractContentFromSlide(slideHtml)
            val slideType = determineSlideType(slideHtml, slideNumber)
            
            slides.add(
                SlideData(
                    slideNumber = slideNumber,
                    title = title,
                    content = content,
                    slideType = slideType,
                    order = slideNumber
                )
            )
            slideNumber++
        }
        
        // If no slides found, create from paragraphs
        if (slides.isEmpty()) {
            val paragraphs = originalText.split(Regex("\n\n+"))
                .map { it.trim() }
                .filter { it.length > 50 }
            
            paragraphs.forEachIndexed { index, paragraph ->
                slides.add(
                    SlideData(
                        slideNumber = index + 1,
                        title = "Slide ${index + 1}",
                        content = paragraph.take(500),
                        slideType = if (index == 0) SlideType.TITLE else SlideType.CONTENT,
                        order = index + 1
                    )
                )
            }
        }
        
        return slides
    }
    
    private fun extractTitleFromSlide(slideHtml: String): String {
        val titlePattern = Regex("""<h[1-3][^>]*>(.*?)</h[1-3]>""", RegexOption.DOT_MATCHES_ALL)
        val match = titlePattern.find(slideHtml)
        return match?.groupValues?.get(1)?.replace(Regex("<[^>]+>"), "")?.trim() ?: "Slide"
    }
    
    private fun extractContentFromSlide(slideHtml: String): String {
        // Remove HTML tags
        return slideHtml.replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(500)
    }
    
    private fun determineSlideType(slideHtml: String, slideNumber: Int): SlideType {
        return when {
            slideNumber == 1 -> SlideType.TITLE
            slideHtml.contains("<ul>") || slideHtml.contains("<ol>") -> SlideType.BULLET_POINTS
            slideHtml.contains("summary", ignoreCase = true) -> SlideType.SUMMARY
            slideHtml.contains("<img") -> SlideType.IMAGE
            else -> SlideType.CONTENT
        }
    }
}

