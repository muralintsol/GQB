package com.gurukulaboard.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.content.ContentMCQGenerator.GeneratedMCQ
import com.gurukulaboard.content.models.ContentSection
import com.gurukulaboard.models.Difficulty
import com.gurukulaboard.models.Question
import com.gurukulaboard.questionbank.QuestionBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MCQGenerationViewModel @Inject constructor(
    private val contentMCQGenerator: ContentMCQGenerator,
    private val questionBankRepository: QuestionBankRepository
) : ViewModel() {
    
    private val _generatedMCQs = MutableLiveData<List<GeneratedMCQ>>()
    val generatedMCQs: LiveData<List<GeneratedMCQ>> = _generatedMCQs
    
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _savedQuestions = MutableLiveData<List<String>>()
    val savedQuestions: LiveData<List<String>> = _savedQuestions
    
    fun generateMCQs(
        context: android.content.Context,
        fileUrl: String,
        numberOfQuestions: Int,
        difficulty: Difficulty,
        subject: String,
        classLevel: Int,
        chapter: String?,
        contentId: String,
        createdBy: String
    ) {
        _loadingState.value = LoadingState.Loading
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                // Extract text from PDF
                val textResult = contentMCQGenerator.extractTextFromContent(context, fileUrl)
                textResult.onSuccess { text ->
                    // Generate MCQs
                    val mcqResult = contentMCQGenerator.generateMCQsFromText(
                        text = text,
                        numberOfQuestions = numberOfQuestions,
                        difficulty = difficulty,
                        chapterName = chapter ?: "Content"
                    )
                    
                    mcqResult.onSuccess { mcqs ->
                        _generatedMCQs.value = mcqs
                        _loadingState.value = LoadingState.Success
                    }.onFailure { exception ->
                        _loadingState.value = LoadingState.Error(exception.message ?: "Failed to generate MCQs")
                        _errorMessage.value = exception.message ?: "Failed to generate MCQs"
                    }
                }.onFailure { exception ->
                    _loadingState.value = LoadingState.Error(exception.message ?: "Failed to extract text from PDF")
                    _errorMessage.value = exception.message ?: "Failed to extract text from PDF"
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "An error occurred")
                _errorMessage.value = e.message ?: "An error occurred"
            }
        }
    }
    
    fun generateMCQsFromSections(
        context: android.content.Context,
        fileUrl: String,
        sections: List<ContentSection>,
        numberOfQuestions: Int,
        difficulty: Difficulty,
        subject: String,
        classLevel: Int,
        chapter: String?,
        contentId: String,
        createdBy: String
    ) {
        _loadingState.value = LoadingState.Loading
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                // Generate MCQs from selected sections
                val mcqResult = contentMCQGenerator.generateMCQsFromSections(
                    context = context,
                    fileUrl = fileUrl,
                    sections = sections,
                    numberOfQuestions = numberOfQuestions,
                    difficulty = difficulty,
                    chapterName = chapter ?: "Content"
                )
                
                mcqResult.onSuccess { mcqs ->
                    _generatedMCQs.value = mcqs
                    _loadingState.value = LoadingState.Success
                }.onFailure { exception ->
                    _loadingState.value = LoadingState.Error(exception.message ?: "Failed to generate MCQs from selected sections")
                    _errorMessage.value = exception.message ?: "Failed to generate MCQs from selected sections"
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "An error occurred")
                _errorMessage.value = e.message ?: "An error occurred"
            }
        }
    }
    
    fun saveMCQsToQuestionBank(
        mcqs: List<GeneratedMCQ>,
        subject: String,
        classLevel: Int,
        chapter: String?,
        contentId: String,
        createdBy: String
    ) {
        _loadingState.value = LoadingState.Loading
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val questionIds = mutableListOf<String>()
                
                mcqs.forEach { mcq ->
                    val question = contentMCQGenerator.toQuestion(
                        generatedMCQ = mcq,
                        subject = subject,
                        classLevel = classLevel,
                        chapter = chapter,
                        contentId = contentId,
                        createdBy = createdBy
                    )
                    
                    val result = questionBankRepository.createQuestion(question)
                    result.onSuccess { id ->
                        questionIds.add(id)
                    }.onFailure { exception ->
                        _errorMessage.value = "Failed to save some questions: ${exception.message}"
                    }
                }
                
                _savedQuestions.value = questionIds
                _loadingState.value = LoadingState.Success
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(e.message ?: "Failed to save MCQs")
                _errorMessage.value = e.message ?: "Failed to save MCQs"
            }
        }
    }
}

