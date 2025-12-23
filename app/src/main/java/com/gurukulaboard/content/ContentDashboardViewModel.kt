package com.gurukulaboard.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.gurukulaboard.content.models.ContentType
import com.gurukulaboard.content.models.TeachingContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentDashboardViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {
    
    private val _contentList = MutableLiveData<List<TeachingContent>>()
    val contentList: LiveData<List<TeachingContent>> = _contentList
    
    private val _filteredContent = MutableLiveData<List<TeachingContent>>()
    val filteredContent: LiveData<List<TeachingContent>> = _filteredContent
    
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private var allContent: List<TeachingContent> = emptyList()
    private var currentFilter: ContentFilter? = null
    
    init {
        loadAllContent()
    }
    
    fun loadAllContent() {
        _loadingState.value = LoadingState.Loading
        viewModelScope.launch {
            val result = repository.getAllContent()
            result.onSuccess { content ->
                allContent = content
                _contentList.value = content
                _filteredContent.value = content
                _loadingState.value = LoadingState.Success
                _errorMessage.value = null
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to load content")
                _errorMessage.value = exception.message ?: "Failed to load content"
            }
        }
    }
    
    fun filterBySubject(subject: String, classLevel: Int) {
        _loadingState.value = LoadingState.Loading
        currentFilter = ContentFilter.SubjectFilter(subject, classLevel)
        viewModelScope.launch {
            val result = repository.getContentBySubject(subject, classLevel)
            result.onSuccess { content ->
                _filteredContent.value = content
                _loadingState.value = LoadingState.Success
                _errorMessage.value = null
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to filter content")
                _errorMessage.value = exception.message ?: "Failed to filter content"
            }
        }
    }
    
    fun filterByChapter(subject: String, classLevel: Int, chapter: String) {
        _loadingState.value = LoadingState.Loading
        currentFilter = ContentFilter.ChapterFilter(subject, classLevel, chapter)
        viewModelScope.launch {
            val result = repository.getContentByChapter(subject, classLevel, chapter)
            result.onSuccess { content ->
                _filteredContent.value = content
                _loadingState.value = LoadingState.Success
                _errorMessage.value = null
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to filter content")
                _errorMessage.value = exception.message ?: "Failed to filter content"
            }
        }
    }
    
    fun searchContent(query: String) {
        if (query.isBlank()) {
            _filteredContent.value = allContent
            return
        }
        
        _loadingState.value = LoadingState.Loading
        currentFilter = ContentFilter.SearchFilter(query)
        viewModelScope.launch {
            val result = repository.searchContent(query)
            result.onSuccess { content ->
                _filteredContent.value = content
                _loadingState.value = LoadingState.Success
                _errorMessage.value = null
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to search content")
                _errorMessage.value = exception.message ?: "Failed to search content"
            }
        }
    }
    
    fun filterByTags(tags: List<String>) {
        if (tags.isEmpty()) {
            _filteredContent.value = allContent
            return
        }
        
        _loadingState.value = LoadingState.Loading
        currentFilter = ContentFilter.TagsFilter(tags)
        viewModelScope.launch {
            val result = repository.getContentByTags(tags)
            result.onSuccess { content ->
                _filteredContent.value = content
                _loadingState.value = LoadingState.Success
                _errorMessage.value = null
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to filter by tags")
                _errorMessage.value = exception.message ?: "Failed to filter by tags"
            }
        }
    }
    
    fun filterByType(contentType: ContentType) {
        _loadingState.value = LoadingState.Loading
        currentFilter = ContentFilter.TypeFilter(contentType)
        viewModelScope.launch {
            val result = repository.getContentByType(contentType)
            result.onSuccess { content ->
                _filteredContent.value = content
                _loadingState.value = LoadingState.Success
                _errorMessage.value = null
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to filter by type")
                _errorMessage.value = exception.message ?: "Failed to filter by type"
            }
        }
    }
    
    fun clearFilters() {
        currentFilter = null
        _filteredContent.value = allContent
    }
    
    fun updateContent(content: TeachingContent) {
        viewModelScope.launch {
            val result = repository.updateContent(content)
            result.onSuccess {
                // Reload content after update
                loadAllContent()
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Failed to update content"
            }
        }
    }
    
    fun deleteContent(id: String, fileUrl: String) {
        _loadingState.value = LoadingState.Loading
        viewModelScope.launch {
            val result = repository.deleteContent(id, fileUrl)
            result.onSuccess {
                // Reload content after deletion
                loadAllContent()
            }.onFailure { exception ->
                _loadingState.value = LoadingState.Error(exception.message ?: "Failed to delete content")
                _errorMessage.value = exception.message ?: "Failed to delete content"
            }
        }
    }
    
    fun downloadContent(content: TeachingContent) {
        viewModelScope.launch {
            repository.incrementDownloadCount(content.id)
        }
    }
    
    fun getRecentContent(limit: Int = 10): List<TeachingContent> {
        return allContent.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            .take(limit)
    }
    
    fun getFavoriteContent(): List<TeachingContent> {
        return allContent.filter { it.isFavorite }
    }
    
    suspend fun uploadContent(content: TeachingContent, fileUri: Uri, userId: String): Result<String> {
        _loadingState.value = LoadingState.Loading
        return repository.uploadContent(content, fileUri, userId)
    }
    
    suspend fun getContentById(id: String): Result<TeachingContent> {
        return repository.getContentById(id)
    }
}

sealed class ContentFilter {
    data class SubjectFilter(val subject: String, val classLevel: Int) : ContentFilter()
    data class ChapterFilter(val subject: String, val classLevel: Int, val chapter: String) : ContentFilter()
    data class SearchFilter(val query: String) : ContentFilter()
    data class TagsFilter(val tags: List<String>) : ContentFilter()
    data class TypeFilter(val contentType: ContentType) : ContentFilter()
}

