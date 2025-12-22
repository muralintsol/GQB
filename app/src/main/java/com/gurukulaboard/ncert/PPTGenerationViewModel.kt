package com.gurukulaboard.ncert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.ncert.models.NCERTBook
import com.gurukulaboard.ncert.models.NCERTIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PPTGenerationViewModel @Inject constructor(
    private val repository: NCERTRepository,
    private val contentExtractor: NCERTContentExtractor,
    private val slideGenerator: NCERTSlideGenerator
) : ViewModel() {
    
    private val _book = MutableLiveData<NCERTBook>()
    val book: LiveData<NCERTBook> = _book
    
    private val _index = MutableLiveData<NCERTIndex>()
    val index: LiveData<NCERTIndex> = _index
    
    private val _content = MutableLiveData<String>()
    val content: LiveData<String> = _content
    
    private val _generatedHTML = MutableLiveData<String?>()
    val generatedHTML: LiveData<String?> = _generatedHTML
    
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
    
    fun generateSlides() {
        val content = _content.value ?: return
        val book = _book.value ?: return
        val chapter = chapterName ?: return
        
        _generatingState.value = GeneratingState.Generating
        
        viewModelScope.launch {
            val result = slideGenerator.generateSlides(
                content = content,
                title = chapter,
                subject = book.subject,
                chapter = chapter
            )
            
            result.onSuccess { html ->
                _generatedHTML.value = html
                _generatingState.value = GeneratingState.Idle
            }.onFailure {
                _generatingState.value = GeneratingState.Error(it.message ?: "Generation failed")
            }
        }
    }
}

