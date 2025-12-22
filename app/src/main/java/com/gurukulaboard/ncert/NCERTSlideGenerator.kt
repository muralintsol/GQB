package com.gurukulaboard.ncert

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTSlideGenerator @Inject constructor() {
    
    /**
     * Generate HTML slides from NCERT content
     */
    suspend fun generateSlides(
        content: String,
        title: String,
        subject: String,
        chapter: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val slides = mutableListOf<SlideContent>()
            
            // Title slide
            slides.add(
                SlideContent(
                    type = SlideType.TITLE,
                    content = title
                )
            )
            
            // Split content into paragraphs/sections for slides
            val paragraphs = content.split(Regex("\n\n+"))
                .map { it.trim() }
                .filter { it.length > 50 } // Filter meaningful paragraphs
            
            // Create content slides from paragraphs
            paragraphs.forEachIndexed { index, paragraph ->
                if (paragraph.length > 500) {
                    // Split long paragraphs into multiple slides
                    val sentences = paragraph.split(Regex("[.!?]+"))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    val chunks = sentences.chunked(3) // 3 sentences per slide
                    chunks.forEach { chunk ->
                        val slideContent = chunk.joinToString(". ") + "."
                        slides.add(
                            SlideContent(
                                type = SlideType.CONTENT,
                                title = "Section ${index + 1}",
                                content = slideContent,
                                bulletPoints = null
                            )
                        )
                    }
                } else {
                    // Single slide for paragraph
                    val bulletPoints = extractBulletPoints(paragraph)
                    slides.add(
                        SlideContent(
                            type = SlideType.CONTENT,
                            title = "Section ${index + 1}",
                            content = if (bulletPoints.isEmpty()) paragraph else "",
                            bulletPoints = if (bulletPoints.isNotEmpty()) bulletPoints else null
                        )
                    )
                }
            }
            
            // Summary slide
            val summaryPoints = extractKeyPoints(content)
            slides.add(
                SlideContent(
                    type = SlideType.SUMMARY,
                    content = "Key points from $chapter",
                    bulletPoints = summaryPoints
                )
            )
            
            // Generate HTML
            val html = NCERTSlideTemplate.generateSlideHTML(title, slides, subject, chapter)
            Result.success(html)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract bullet points from content
     */
    private fun extractBulletPoints(content: String): List<String> {
        val bullets = mutableListOf<String>()
        
        // Look for bullet patterns: -, *, •, numbers
        val bulletPattern = Regex("""^[\s]*[-*•]\s+(.+)$""", RegexOption.MULTILINE)
        bulletPattern.findAll(content).forEach { match ->
            bullets.add(match.groupValues[1].trim())
        }
        
        // Look for numbered lists
        val numberedPattern = Regex("""^[\s]*\d+[.)]\s+(.+)$""", RegexOption.MULTILINE)
        numberedPattern.findAll(content).forEach { match ->
            bullets.add(match.groupValues[1].trim())
        }
        
        return bullets.take(10) // Limit to 10 bullet points
    }
    
    /**
     * Extract key points for summary
     */
    private fun extractKeyPoints(content: String): List<String> {
        val sentences = content.split(Regex("[.!?]+"))
            .map { it.trim() }
            .filter { it.length > 30 && it.length < 150 }
        
        // Extract sentences with key terms
        val keyTerms = listOf("important", "significant", "key", "main", "primary", "essential")
        val keySentences = sentences.filter { sentence ->
            keyTerms.any { term -> sentence.contains(term, ignoreCase = true) }
        }
        
        return keySentences.take(5) // Top 5 key points
    }
}

