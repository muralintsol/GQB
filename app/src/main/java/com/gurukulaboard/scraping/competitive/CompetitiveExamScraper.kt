package com.gurukulaboard.scraping.competitive

import android.util.Log
import com.gurukulaboard.models.*
import com.gurukulaboard.scraping.ScrapingRepository
import com.gurukulaboard.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompetitiveExamScraper @Inject constructor(
    private val scrapingRepository: ScrapingRepository
) {
    
    companion object {
        private const val TAG = "CompetitiveExamScraper"
        
        // Common URLs for competitive exam question banks
        private const val NEET_PREVIOUS_YEAR_URL = "https://www.vedantu.com/neet/neet-previous-year-papers"
        private const val JEE_PREVIOUS_YEAR_URL = "https://www.vedantu.com/jee/jee-main-previous-year-papers"
        private const val KCET_PREVIOUS_YEAR_URL = "https://cetonline.karnataka.gov.in/kea/"
    }
    
    suspend fun scrapeNEET(
        subject: String,
        classLevel: Int,
        chapter: String = ""
    ): Result<List<Question>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting NEET scraping for subject: $subject, class: $classLevel")
            
            // For NEET, we'll scrape from NCERT-based sources and previous year papers
            val questions = mutableListOf<Question>()
            
            // Scrape from NCERT website (NEET is based on NCERT)
            val ncertQuestions = scrapeFromNCERTForCompetitive(subject, classLevel, ExamType.NEET, chapter)
            questions.addAll(ncertQuestions)
            
            // Scrape from previous year papers (if available)
            val previousYearQuestions = scrapePreviousYearPapers(ExamType.NEET, subject, chapter)
            questions.addAll(previousYearQuestions)
            
            if (questions.isEmpty()) {
                Log.w(TAG, "No questions found for NEET scraping")
                Result.success(emptyList())
            } else {
                Log.d(TAG, "Found ${questions.size} questions for NEET")
                Result.success(questions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping NEET", e)
            Result.failure(e)
        }
    }
    
    suspend fun scrapeJEE(
        subject: String,
        classLevel: Int,
        chapter: String = ""
    ): Result<List<Question>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting JEE scraping for subject: $subject, class: $classLevel")
            
            val questions = mutableListOf<Question>()
            
            // Scrape from NCERT-based sources (JEE also uses NCERT)
            val ncertQuestions = scrapeFromNCERTForCompetitive(subject, classLevel, ExamType.JEE, chapter)
            questions.addAll(ncertQuestions)
            
            // Scrape from previous year papers
            val previousYearQuestions = scrapePreviousYearPapers(ExamType.JEE, subject, chapter)
            questions.addAll(previousYearQuestions)
            
            if (questions.isEmpty()) {
                Log.w(TAG, "No questions found for JEE scraping")
                Result.success(emptyList())
            } else {
                Log.d(TAG, "Found ${questions.size} questions for JEE")
                Result.success(questions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping JEE", e)
            Result.failure(e)
        }
    }
    
    suspend fun scrapeKCET(
        subject: String,
        classLevel: Int,
        chapter: String = ""
    ): Result<List<Question>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting K-CET scraping for subject: $subject, class: $classLevel")
            
            val questions = mutableListOf<Question>()
            
            // K-CET uses Karnataka PU Board syllabus, so scrape from Karnataka sources
            val karnatakaQuestions = scrapeFromKarnatakaForCompetitive(subject, classLevel, ExamType.K_CET, chapter)
            questions.addAll(karnatakaQuestions)
            
            // Scrape from previous year papers
            val previousYearQuestions = scrapePreviousYearPapers(ExamType.K_CET, subject, chapter)
            questions.addAll(previousYearQuestions)
            
            if (questions.isEmpty()) {
                Log.w(TAG, "No questions found for K-CET scraping")
                Result.success(emptyList())
            } else {
                Log.d(TAG, "Found ${questions.size} questions for K-CET")
                Result.success(questions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping K-CET", e)
            Result.failure(e)
        }
    }
    
    private suspend fun scrapeFromNCERTForCompetitive(
        subject: String,
        classLevel: Int,
        examType: ExamType,
        chapter: String = ""
    ): List<Question> {
        return try {
            val url = "${Constants.NCERT_URL}textbook.php?$subject&class=$classLevel"
            val doc: Document = Jsoup.connect(url)
                .timeout(10000)
                .userAgent("Mozilla/5.0")
                .get()
            
            val questions = mutableListOf<Question>()
            val questionElements = doc.select("div.question, .mcq, .question-text")
            
            for (element in questionElements) {
                val content = element.text().trim()
                if (content.isNotBlank() && !scrapingRepository.checkDuplicateQuestion(content)) {
                    val question = createQuestionFromContent(
                        content = content,
                        subject = subject,
                        classLevel = classLevel,
                        examType = examType,
                        chapter = chapter.ifBlank { "General" },
                        source = QuestionSource.SCRAPED,
                        sourceDetails = "NCERT for $examType"
                    )
                    questions.add(question)
                }
            }
            
            questions
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping from NCERT for competitive", e)
            emptyList()
        }
    }
    
    private suspend fun scrapeFromKarnatakaForCompetitive(
        subject: String,
        classLevel: Int,
        examType: ExamType,
        chapter: String = ""
    ): List<Question> {
        return try {
            val url = "${Constants.KARNATAKA_PU_URL}?subject=$subject&class=$classLevel"
            val doc: Document = Jsoup.connect(url)
                .timeout(10000)
                .userAgent("Mozilla/5.0")
                .get()
            
            val questions = mutableListOf<Question>()
            val questionElements = doc.select("div.question, .question-item, .qbank-item")
            
            for (element in questionElements) {
                val content = element.text().trim()
                if (content.isNotBlank() && !scrapingRepository.checkDuplicateQuestion(content)) {
                    val question = createQuestionFromContent(
                        content = content,
                        subject = subject,
                        classLevel = classLevel,
                        examType = examType,
                        chapter = chapter.ifBlank { "General" },
                        source = QuestionSource.SCRAPED,
                        sourceDetails = "Karnataka PU for $examType"
                    )
                    questions.add(question)
                }
            }
            
            questions
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping from Karnataka for competitive", e)
            emptyList()
        }
    }
    
    private suspend fun scrapePreviousYearPapers(
        examType: ExamType,
        subject: String,
        chapter: String = ""
    ): List<Question> {
        return try {
            val url = when (examType) {
                ExamType.NEET -> NEET_PREVIOUS_YEAR_URL
                ExamType.JEE -> JEE_PREVIOUS_YEAR_URL
                ExamType.K_CET -> KCET_PREVIOUS_YEAR_URL
                else -> return emptyList()
            }
            
            val doc: Document = Jsoup.connect(url)
                .timeout(10000)
                .userAgent("Mozilla/5.0")
                .get()
            
            val questions = mutableListOf<Question>()
            
            // Look for question patterns in previous year papers
            val questionPatterns = doc.select("div.question, .mcq-question, .question-paper-item")
            
            for (element in questionPatterns) {
                val content = element.select("p, .question-text, .q-text").text().trim()
                if (content.isNotBlank() && !scrapingRepository.checkDuplicateQuestion(content)) {
                    // Extract options if available
                    val options = element.select("li.option, .option-item").map { it.text().trim() }
                    
                    val question = Question(
                        content = content,
                        type = QuestionType.MCQ, // Competitive exams are mostly MCQ
                        subject = subject,
                        `class` = 12, // Competitive exams are for class 12
                        chapter = chapter.ifBlank { "Previous Year Paper" },
                        difficulty = Difficulty.MEDIUM,
                        examType = examType,
                        source = QuestionSource.SCRAPED,
                        sourceDetails = "$examType Previous Year Paper",
                        status = QuestionStatus.PENDING,
                        createdBy = "system",
                        options = if (options.isNotEmpty()) options else null
                    )
                    questions.add(question)
                }
            }
            
            questions
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping previous year papers", e)
            emptyList()
        }
    }
    
    private fun createQuestionFromContent(
        content: String,
        subject: String,
        classLevel: Int,
        examType: ExamType,
        chapter: String = "General",
        source: QuestionSource,
        sourceDetails: String
    ): Question {
        return Question(
            content = content,
            type = QuestionType.MCQ, // Default to MCQ for competitive exams
            subject = subject,
            `class` = classLevel,
            chapter = chapter,
            difficulty = Difficulty.MEDIUM,
            examType = examType,
            source = source,
            sourceDetails = sourceDetails,
            status = QuestionStatus.PENDING,
            createdBy = "system"
        )
    }
}

