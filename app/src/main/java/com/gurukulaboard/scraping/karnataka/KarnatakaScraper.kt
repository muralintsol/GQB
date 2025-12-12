package com.gurukulaboard.scraping.karnataka

import com.gurukulaboard.models.ExamType
import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionSource
import com.gurukulaboard.models.QuestionStatus
import com.gurukulaboard.models.QuestionType
import com.gurukulaboard.scraping.ScrapingRepository
import com.gurukulaboard.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KarnatakaScraper @Inject constructor(
    private val scrapingRepository: ScrapingRepository
) {
    
    suspend fun scrapeKarnatakaPU(
        subject: String,
        classLevel: Int,
        examType: ExamType
    ): Result<List<Question>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = Constants.KARNATAKA_PU_URL
            
            // Check if already scraped
            val lastHash = scrapingRepository.getLastScrapingHash("KARNATAKA", url)
            val currentContent = fetchContent(url)
            val currentHash = scrapingRepository.generateContentHash(currentContent)
            
            if (lastHash == currentHash) {
                return@withContext Result.success(emptyList())
            }
            
            val questions = parseKarnatakaContent(currentContent, subject, classLevel, examType)
            
            // Save scraping history
            scrapingRepository.saveScrapingHistory("KARNATAKA", url, currentHash)
            
            Result.success(value = questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun fetchContent(url: String): String {
        return Jsoup.connect(url)
            .timeout(30000)
            .userAgent("Mozilla/5.0")
            .get()
            .html()
    }
    
    private suspend fun parseKarnatakaContent(
        htmlContent: String,
        subject: String,
        classLevel: Int,
        examType: ExamType
    ): List<Question> = withContext(Dispatchers.Default) {
        val questions = mutableListOf<Question>()
        val doc = Jsoup.parse(htmlContent)
        
        // Parse HTML to extract questions from Karnataka PU question bank
        // Adjust selectors based on actual website structure
        val questionElements = doc.select("div.question, .qb-question, .question-item")
        
        questionElements.forEachIndexed { index, element ->
            val questionText = element.select(".question-text, p").text().trim()
            val options = element.select(".options li, .option").map { it.text().trim() }
            val answer = element.select(".answer, .correct-answer").text().trim()
            
            if (questionText.isNotBlank() && !scrapingRepository.checkDuplicateQuestion(questionText)) {
                val question = Question(
                    content = questionText,
                    type = if (options.isNotEmpty()) QuestionType.MCQ else QuestionType.SHORT_ANSWER,
                    subject = subject,
                    `class` = classLevel,
                    chapter = "", // May need to extract from content
                    examType = examType,
                    source = QuestionSource.KARNATAKA_DEPARTMENT,
                    sourceDetails = "Karnataka PU Question Bank - $subject",
                    status = QuestionStatus.PENDING,
                    options = if (options.isNotEmpty()) options else null,
                    answer = answer.ifBlank { null }
                )
                questions.add(question)
            }
        }
        
        questions
    }
}

