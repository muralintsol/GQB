package com.gurukulaboard.questionbank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionBankViewModel @Inject constructor(
    private val repository: QuestionBankRepository
) : ViewModel() {
    
    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions
    
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Filters
    private var currentExamType: ExamType? = null
    private var currentSubject: String? = null
    private var currentClass: Int? = null
    private var currentChapter: String? = null
    private var currentDifficulty: Difficulty? = null
    private var currentQuestionType: QuestionType? = null
    private var currentStatus: QuestionStatus? = null
    private var currentSearchQuery: String? = null
    
    fun loadQuestions() {
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            val result = repository.getQuestions(
                examType = currentExamType,
                subject = currentSubject,
                classLevel = currentClass,
                chapter = currentChapter,
                difficulty = currentDifficulty,
                questionType = currentQuestionType,
                status = currentStatus,
                searchQuery = currentSearchQuery
            )
            
            result.onSuccess { questionList ->
                _questions.value = questionList
                _loadingState.value = LoadingState.Success
                _errorMessage.value = null
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error
                _errorMessage.value = exception.message
            }
        }
    }
    
    fun setFilter(
        examType: ExamType? = null,
        subject: String? = null,
        classLevel: Int? = null,
        chapter: String? = null,
        difficulty: Difficulty? = null,
        questionType: QuestionType? = null,
        status: QuestionStatus? = null
    ) {
        examType?.let { currentExamType = it }
        subject?.let { currentSubject = it }
        classLevel?.let { currentClass = it }
        chapter?.let { currentChapter = it }
        difficulty?.let { currentDifficulty = it }
        questionType?.let { currentQuestionType = it }
        status?.let { currentStatus = it }
        loadQuestions()
    }
    
    fun setSearchQuery(query: String?) {
        currentSearchQuery = query
        loadQuestions()
    }
    
    fun createQuestion(question: Question) {
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            val result = repository.createQuestion(question)
            
            result.onSuccess {
                loadQuestions()
            }.onFailure { exception ->
                _errorMessage.value = exception.message
                _loadingState.value = LoadingState.Error
            }
        }
    }
    
    fun updateQuestion(question: Question) {
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            val result = repository.updateQuestion(question)
            
            result.onSuccess {
                loadQuestions()
            }.onFailure { exception ->
                _errorMessage.value = exception.message
                _loadingState.value = LoadingState.Error
            }
        }
    }
    
    fun deleteQuestion(questionId: String) {
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            val result = repository.deleteQuestion(questionId)
            
            result.onSuccess {
                loadQuestions()
            }.onFailure { exception ->
                _errorMessage.value = exception.message
                _loadingState.value = LoadingState.Error
            }
        }
    }
    
    fun approveQuestion(questionId: String, approvedBy: String) {
        viewModelScope.launch {
            val result = repository.approveQuestion(questionId, approvedBy)
            
            result.onSuccess {
                loadQuestions()
            }.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }
    
    fun rejectQuestion(questionId: String) {
        viewModelScope.launch {
            val result = repository.rejectQuestion(questionId)
            
            result.onSuccess {
                loadQuestions()
            }.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }
    
    fun clearFilters() {
        currentExamType = null
        currentSubject = null
        currentClass = null
        currentChapter = null
        currentDifficulty = null
        currentQuestionType = null
        currentStatus = null
        currentSearchQuery = null
        loadQuestions()
    }
}

sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    object Success : LoadingState()
    object Error : LoadingState()
}

