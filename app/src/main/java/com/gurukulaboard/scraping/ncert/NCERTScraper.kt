package com.gurukulaboard.scraping.ncert

import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionSource
import com.gurukulaboard.models.QuestionStatus
import com.gurukulaboard.models.QuestionType
import com.gurukulaboard.scraping.ScrapingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTScraper @Inject constructor(
    private val scrapingRepository: ScrapingRepository
) {
    
    suspend fun scrapeNCERTWebsite(
        classLevel: Int,
        subject: String,
        chapter: String
    ): Result<List<Question>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = buildNCERTUrl(classLevel, subject, chapter)
            
            // Check if already scraped
            val lastHash = scrapingRepository.getLastScrapingHash("NCERT", url)
            val currentContent = fetchContent(url)
            val currentHash = scrapingRepository.generateContentHash(currentContent)
            
            if (lastHash == currentHash) {
                return@withContext Result.success(emptyList())
            }
            
            val questions = parseNCERTContent(currentContent, subject, classLevel, chapter)
            
            // Save scraping history
            scrapingRepository.saveScrapingHistory("NCERT", url, currentHash)
            
            Result.success(value = questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildNCERTUrl(classLevel: Int, subject: String, chapter: String): String {
        // Base NCERT URL structure - adjust based on actual NCERT website structure
        val baseUrl = "https://ncert.nic.in/textbook.php"
        return "$baseUrl?$subject=$subject&class=$classLevel&chapter=$chapter"
    }
    
    private suspend fun fetchContent(url: String): String {
        return Jsoup.connect(url)
            .timeout(30000)
            .userAgent("Mozilla/5.0")
            .get()
            .html()
    }
    
    private suspend fun parseNCERTContent(
        htmlContent: String,
        subject: String,
        classLevel: Int,
        chapter: String
    ): List<Question> = withContext(Dispatchers.Default) {
        val questions = mutableListOf<Question>()
        val doc = Jsoup.parse(htmlContent)
        
        // Parse HTML to extract questions
        // This is a simplified parser - adjust based on actual NCERT website structure
        val questionElements = doc.select("div.question, p.question, .exercise-question")
        
        questionElements.forEachIndexed { index, element ->
            val questionText = element.text().trim()
            
            if (questionText.isNotBlank() && !scrapingRepository.checkDuplicateQuestion(questionText)) {
                val question = Question(
                    content = questionText,
                    type = QuestionType.MCQ, // Default, can be enhanced
                    subject = subject,
                    `class` = classLevel,
                    chapter = chapter,
                    source = QuestionSource.NCERT,
                    sourceDetails = "NCERT Class $classLevel $subject Chapter: $chapter",
                    status = QuestionStatus.PENDING
                )
                questions.add(question)
            }
        }
        
        questions
    }
}

