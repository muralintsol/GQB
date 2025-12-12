package com.gurukulaboard.scraping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.models.ExamType
import com.gurukulaboard.models.Question
import com.gurukulaboard.questionbank.QuestionBankRepository
import com.gurukulaboard.scraping.competitive.CompetitiveExamScraper
import com.gurukulaboard.scraping.karnataka.KarnatakaScraper
import com.gurukulaboard.scraping.ncert.NCERTScraper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScrapingViewModel @Inject constructor(
    private val ncertScraper: NCERTScraper,
    private val karnatakaScraper: KarnatakaScraper,
    private val competitiveScraper: CompetitiveExamScraper,
    private val questionBankRepository: QuestionBankRepository
) : ViewModel() {
    
    private val _scrapingState = MutableLiveData<ScrapingState>()
    val scrapingState: LiveData<ScrapingState> = _scrapingState
    
    private val _scrapedQuestions = MutableLiveData<List<Question>>()
    val scrapedQuestions: LiveData<List<Question>> = _scrapedQuestions
    
    fun scrapeNCERT(classLevel: Int, subject: String, chapter: String) {
        _scrapingState.value = ScrapingState.Loading
        
        viewModelScope.launch {
            val result = ncertScraper.scrapeNCERTWebsite(classLevel, subject, chapter)
            
            result.onSuccess { questions ->
                if (questions.isEmpty()) {
                    _scrapingState.value = ScrapingState.Success("No new questions found")
                } else {
                    _scrapedQuestions.value = questions
                    saveScrapedQuestions(questions)
                    _scrapingState.value = ScrapingState.Success("Found ${questions.size} new questions")
                }
            }.onFailure { exception ->
                _scrapingState.value = ScrapingState.Error(exception.message ?: "Scraping failed")
            }
        }
    }
    
    fun scrapeKarnataka(subject: String, classLevel: Int, examType: ExamType) {
        _scrapingState.value = ScrapingState.Loading
        
        viewModelScope.launch {
            val result = karnatakaScraper.scrapeKarnatakaPU(subject, classLevel, examType)
            
            result.onSuccess { questions ->
                if (questions.isEmpty()) {
                    _scrapingState.value = ScrapingState.Success("No new questions found")
                } else {
                    _scrapedQuestions.value = questions
                    saveScrapedQuestions(questions)
                    _scrapingState.value = ScrapingState.Success("Found ${questions.size} new questions")
                }
            }.onFailure { exception ->
                _scrapingState.value = ScrapingState.Error(exception.message ?: "Scraping failed")
            }
        }
    }
    
    fun scrapeCompetitiveExam(examType: ExamType, subject: String, classLevel: Int, chapter: String = "") {
        _scrapingState.value = ScrapingState.Loading
        
        viewModelScope.launch {
            val result = when (examType) {
                ExamType.NEET -> competitiveScraper.scrapeNEET(subject, classLevel, chapter)
                ExamType.JEE -> competitiveScraper.scrapeJEE(subject, classLevel, chapter)
                ExamType.K_CET -> competitiveScraper.scrapeKCET(subject, classLevel, chapter)
                else -> Result.failure(Exception("Unsupported exam type for competitive scraping"))
            }
            
            result.onSuccess { questions ->
                if (questions.isEmpty()) {
                    _scrapingState.value = ScrapingState.Success("No new questions found")
                } else {
                    _scrapedQuestions.value = questions
                    saveScrapedQuestions(questions)
                    _scrapingState.value = ScrapingState.Success("Found ${questions.size} new questions")
                }
            }.onFailure { exception ->
                _scrapingState.value = ScrapingState.Error(exception.message ?: "Scraping failed")
            }
        }
    }
    
    private suspend fun saveScrapedQuestions(questions: List<Question>) {
        questions.forEach { question ->
            questionBankRepository.createQuestion(question)
        }
    }
}

sealed class ScrapingState {
    object Idle : ScrapingState()
    object Loading : ScrapingState()
    data class Success(val message: String) : ScrapingState()
    data class Error(val message: String) : ScrapingState()
}

