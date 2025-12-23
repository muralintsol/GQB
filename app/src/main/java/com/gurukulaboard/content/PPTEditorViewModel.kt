package com.gurukulaboard.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.content.models.SlideData
import com.gurukulaboard.content.models.SlideType
import com.gurukulaboard.content.models.TeacherPPT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PPTEditorViewModel @Inject constructor(
    private val pptRepository: ContentPPTRepository
) : ViewModel() {
    
    private val _currentPPT = MutableLiveData<TeacherPPT?>()
    val currentPPT: LiveData<TeacherPPT?> = _currentPPT
    
    private val _slides = MutableLiveData<List<SlideData>>()
    val slides: LiveData<List<SlideData>> = _slides
    
    private val _selectedSlideIndex = MutableLiveData<Int>(-1)
    val selectedSlideIndex: LiveData<Int> = _selectedSlideIndex
    
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun loadPPT(pptId: String) {
        _loadingState.value = LoadingState.Loading
        viewModelScope.launch {
            val result = pptRepository.getPPTById(pptId)
            result.onSuccess { ppt ->
                _currentPPT.value = ppt
                _slides.value = ppt.slides
                _loadingState.value = LoadingState.Success
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to load PPT")
                _errorMessage.value = exception.message ?: "Failed to load PPT"
            }
        }
    }
    
    fun initializeWithPPT(ppt: TeacherPPT) {
        _currentPPT.value = ppt
        _slides.value = ppt.slides.toMutableList()
        if (ppt.slides.isNotEmpty()) {
            _selectedSlideIndex.value = 0
        }
    }
    
    fun selectSlide(index: Int) {
        if (index >= 0 && index < (_slides.value?.size ?: 0)) {
            _selectedSlideIndex.value = index
        }
    }
    
    fun addSlide(slideType: SlideType = SlideType.CONTENT, position: Int = -1) {
        val currentSlides = _slides.value?.toMutableList() ?: mutableListOf()
        val newSlideNumber = currentSlides.size + 1
        val insertPosition = if (position >= 0 && position <= currentSlides.size) {
            position
        } else {
            currentSlides.size
        }
        
        val newSlide = SlideData(
            slideNumber = newSlideNumber,
            title = "New Slide",
            content = "",
            slideType = slideType,
            order = insertPosition + 1
        )
        
        // Update order of existing slides
        currentSlides.forEachIndexed { index, slide ->
            if (index >= insertPosition) {
                currentSlides[index] = slide.copy(order = slide.order + 1)
            }
        }
        
        currentSlides.add(insertPosition, newSlide)
        _slides.value = currentSlides
        _selectedSlideIndex.value = insertPosition
    }
    
    fun removeSlide(index: Int) {
        val currentSlides = _slides.value?.toMutableList() ?: return
        if (index < 0 || index >= currentSlides.size) return
        
        currentSlides.removeAt(index)
        
        // Update order of remaining slides
        currentSlides.forEachIndexed { i, slide ->
            currentSlides[i] = slide.copy(order = i + 1, slideNumber = i + 1)
        }
        
        _slides.value = currentSlides
        
        // Adjust selected index
        val newSelectedIndex = when {
            currentSlides.isEmpty() -> -1
            index >= currentSlides.size -> currentSlides.size - 1
            else -> index
        }
        _selectedSlideIndex.value = newSelectedIndex
    }
    
    fun updateSlide(index: Int, title: String, content: String, slideType: SlideType? = null) {
        val currentSlides = _slides.value?.toMutableList() ?: return
        if (index < 0 || index >= currentSlides.size) return
        
        val slide = currentSlides[index]
        currentSlides[index] = slide.copy(
            title = title,
            content = content,
            slideType = slideType ?: slide.slideType
        )
        
        _slides.value = currentSlides
    }
    
    fun reorderSlides(fromPosition: Int, toPosition: Int) {
        val currentSlides = _slides.value?.toMutableList() ?: return
        if (fromPosition < 0 || fromPosition >= currentSlides.size ||
            toPosition < 0 || toPosition >= currentSlides.size) return
        
        val slide = currentSlides.removeAt(fromPosition)
        currentSlides.add(toPosition, slide)
        
        // Update order
        currentSlides.forEachIndexed { index, s ->
            currentSlides[index] = s.copy(order = index + 1, slideNumber = index + 1)
        }
        
        _slides.value = currentSlides
        _selectedSlideIndex.value = toPosition
    }
    
    fun savePPT(isDraft: Boolean = false) {
        val currentPPT = _currentPPT.value ?: return
        val currentSlides = _slides.value ?: return
        
        _loadingState.value = LoadingState.Loading
        viewModelScope.launch {
            // Regenerate HTML from slides
            val htmlContent = generateHTMLFromSlides(currentPPT.title, currentSlides, currentPPT.subject, currentPPT.chapter ?: "")
            
            val updatedPPT = currentPPT.copy(
                slides = currentSlides,
                htmlContent = htmlContent,
                isDraft = isDraft,
                updatedAt = com.google.firebase.Timestamp.now()
            )
            
            val result = if (updatedPPT.id.isBlank()) {
                pptRepository.savePPT(updatedPPT)
            } else {
                val updateResult = pptRepository.updatePPT(updatedPPT)
                if (updateResult.isSuccess) {
                    Result.success(updatedPPT.id)
                } else {
                    Result.failure<String>(updateResult.exceptionOrNull() ?: Exception("Failed to update PPT"))
                }
            }
            
            result.onSuccess { id ->
                _currentPPT.value = updatedPPT.copy(id = id)
                _loadingState.value = LoadingState.Success
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to save PPT")
                _errorMessage.value = exception.message ?: "Failed to save PPT"
            }
        }
    }
    
    fun generateHTMLFromSlides(title: String, slides: List<SlideData>, subject: String, chapter: String): String {
        // Use NCERT slide template to generate HTML
        val slideContents = slides.map { slide ->
            com.gurukulaboard.ncert.SlideContent(
                type = when (slide.slideType) {
                    SlideType.TITLE -> com.gurukulaboard.ncert.SlideType.TITLE
                    SlideType.BULLET_POINTS -> com.gurukulaboard.ncert.SlideType.CONTENT
                    SlideType.SUMMARY -> com.gurukulaboard.ncert.SlideType.SUMMARY
                    else -> com.gurukulaboard.ncert.SlideType.CONTENT
                },
                title = slide.title,
                content = slide.content,
                bulletPoints = if (slide.slideType == SlideType.BULLET_POINTS) {
                    slide.content.split("\n").filter { it.trim().isNotEmpty() }
                } else null
            )
        }
        
        return com.gurukulaboard.ncert.NCERTSlideTemplate.generateSlideHTML(title, slideContents, subject, chapter)
    }
}

