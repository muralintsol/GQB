package com.gurukulaboard.ncert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.models.Question
import com.gurukulaboard.ncert.models.NCERTBook
import com.gurukulaboard.ncert.models.NCERTIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MCQGenerationViewModel @Inject constructor(
    private val repository: NCERTRepository,
    private val contentExtractor: NCERTContentExtractor,
    private val mcqGenerator: NCERTMCQGenerator
) : ViewModel() {
    
    private val _book = MutableLiveData<NCERTBook>()
    val book: LiveData<NCERTBook> = _book
    
    private val _index = MutableLiveData<NCERTIndex>()
    val index: LiveData<NCERTIndex> = _index
    
    private val _content = MutableLiveData<String>()
    val content: LiveData<String> = _content
    
    private val _generatedMCQs = MutableLiveData<List<NCERTMCQGenerator.GeneratedMCQ>>()
    val generatedMCQs: LiveData<List<NCERTMCQGenerator.GeneratedMCQ>> = _generatedMCQs
    
    private val _generatingState = MutableLiveData<GeneratingState>()
    val generatingState: LiveData<GeneratingState> = _generatingState
    
    private var chapterName: String? = null
    
    fun loadContent(bookId: String, chapterName: String) {
        this.chapterName = chapterName
        
        viewModelScope.launch {
            val bookResult = repository.getBook(bookId)
            bookResult.onSuccess { book ->
                _book.value = book
                
                val indexResult = repository.getIndex(bookId)
                indexResult.onSuccess { index ->
                    _index.value = index
                    
                    // Find chapter
                    val chapter = index.chapters.find { it.name == chapterName }
                    if (chapter != null && book.localFilePath != null) {
                        val pdfFile = File(book.localFilePath)
                        if (pdfFile.exists()) {
                            val contentResult = contentExtractor.extractChapterContent(pdfFile, bookId, chapter)
                            contentResult.onSuccess { extractedContent ->
                                _content.value = extractedContent
                            }
                        }
                    }
                }
            }
        }
    }
    
    fun generateMCQs(numberOfQuestions: Int) {
        val content = _content.value ?: return
        val currentChapterName = chapterName ?: return
        
        _generatingState.value = GeneratingState.Generating
        
        viewModelScope.launch {
            val book = _book.value ?: return@launch
            val chapter = _index.value?.chapters?.find { it.name == currentChapterName } ?: return@launch
            
            val request = NCERTMCQGenerator.MCQGenerationRequest(
                content = content,
                chapterName = chapter.name,
                numberOfQuestions = numberOfQuestions,
                difficulty = com.gurukulaboard.models.Difficulty.MEDIUM
            )
            
            val result = mcqGenerator.generateMCQs(request)
            result.onSuccess { mcqs ->
                _generatedMCQs.value = mcqs
                _generatingState.value = GeneratingState.Idle
            }.onFailure {
                _generatingState.value = GeneratingState.Error(it.message ?: "Generation failed")
            }
        }
    }
    
    fun convertToQuestion(mcq: NCERTMCQGenerator.GeneratedMCQ, userId: String): Question {
        val book = _book.value ?: throw IllegalStateException("Book not loaded")
        val currentChapterName = chapterName ?: throw IllegalStateException("Chapter name not set")
        val chapter = _index.value?.chapters?.find { it.name == currentChapterName } ?: throw IllegalStateException("Chapter not found")
        
        return mcqGenerator.toQuestion(
            mcq,
            book.subject,
            book.classLevel,
            chapter.name,
            null,
            userId
        )
    }
}

sealed class GeneratingState {
    object Idle : GeneratingState()
    object Generating : GeneratingState()
    data class Error(val message: String) : GeneratingState()
}

