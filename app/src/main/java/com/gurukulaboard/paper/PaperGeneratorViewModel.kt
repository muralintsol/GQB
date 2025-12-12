package com.gurukulaboard.paper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.models.*
import com.gurukulaboard.paper.templates.CompetitiveTemplate
import com.gurukulaboard.paper.templates.FormattedPaper
import com.gurukulaboard.paper.templates.PUBoardTemplate
import com.gurukulaboard.questionbank.QuestionBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaperGeneratorViewModel @Inject constructor(
    private val paperGenerator: PaperGenerator,
    private val questionBankRepository: QuestionBankRepository
) : ViewModel() {
    
    private val _availableQuestions = MutableLiveData<List<Question>>()
    val availableQuestions: LiveData<List<Question>> = _availableQuestions
    
    private val _generatedPaper = MutableLiveData<List<Question>>()
    val generatedPaper: LiveData<List<Question>> = _generatedPaper
    
    private val _formattedPaper = MutableLiveData<FormattedPaper?>()
    val formattedPaper: LiveData<FormattedPaper?> = _formattedPaper
    
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    fun loadAvailableQuestions(
        examType: ExamType,
        subject: String,
        classLevel: Int
    ) {
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            val result = questionBankRepository.getQuestions(
                examType = examType,
                subject = subject,
                classLevel = classLevel,
                status = QuestionStatus.APPROVED
            )
            
            result.onSuccess { questions ->
                _availableQuestions.value = questions
                _loadingState.value = LoadingState.Success
            }.onFailure {
                _loadingState.value = LoadingState.Error
            }
        }
    }
    
    fun generatePaper(
        examType: ExamType,
        subject: String,
        classLevel: Int,
        totalMarks: Int,
        difficultyDistribution: Map<Difficulty, Int>,
        questionTypes: List<QuestionType>,
        chapters: List<String>
    ) {
        val available = _availableQuestions.value ?: emptyList()
        
        val result = paperGenerator.generatePaper(
            examType = examType,
            subject = subject,
            classLevel = classLevel,
            availableQuestions = available,
            totalMarks = totalMarks,
            difficultyDistribution = difficultyDistribution,
            questionTypes = questionTypes,
            chapters = chapters
        )
        
        result.onSuccess { questions ->
            _generatedPaper.value = questions
            
            // Apply template formatting
            when (examType) {
                ExamType.PU_BOARD -> {
                    val formatted = PUBoardTemplate.formatPaper(questions, totalMarks)
                    _formattedPaper.value = formatted
                    _generatedPaper.value = formatted.allQuestions
                }
                ExamType.NEET, ExamType.JEE, ExamType.K_CET -> {
                    // Competitive exams return List<Question> directly
                    val formattedQuestions = CompetitiveTemplate.formatPaper(questions, examType, totalMarks)
                    _generatedPaper.value = formattedQuestions
                    // For competitive exams, create a simple formatted paper with all questions in section A
                    val formatted = FormattedPaper(
                        sectionA = formattedQuestions,
                        sectionB = emptyList(),
                        sectionC = emptyList(),
                        totalMarks = totalMarks
                    )
                    _formattedPaper.value = formatted
                }
            }
            _loadingState.value = LoadingState.Success
        }.onFailure {
            _loadingState.value = LoadingState.Error
        }
    }
}

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    object Success : LoadingState()
    object Error : LoadingState()
}

