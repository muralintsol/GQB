package com.gurukulaboard.paper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.models.QuestionPaper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedPapersViewModel @Inject constructor(
    private val paperRepository: PaperRepository
) : ViewModel() {
    
    private val _papers = MutableLiveData<List<QuestionPaper>>()
    val papers: LiveData<List<QuestionPaper>> = _papers
    
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    fun loadPapers() {
        _loadingState.value = LoadingState.Loading
        
        viewModelScope.launch {
            val result = paperRepository.getPapers()
            
            result.onSuccess { paperList ->
                _papers.value = paperList
                _loadingState.value = LoadingState.Success
            }.onFailure {
                _loadingState.value = LoadingState.Error
            }
        }
    }
    
    fun deletePaper(paperId: String) {
        viewModelScope.launch {
            paperRepository.deletePaper(paperId)
            loadPapers() // Reload after deletion
        }
    }
}

