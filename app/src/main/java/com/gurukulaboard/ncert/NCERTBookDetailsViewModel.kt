package com.gurukulaboard.ncert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.ncert.models.NCERTBook
import com.gurukulaboard.ncert.models.NCERTIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCERTBookDetailsViewModel @Inject constructor(
    private val repository: NCERTRepository
) : ViewModel() {
    
    private val _book = MutableLiveData<NCERTBook>()
    val book: LiveData<NCERTBook> = _book
    
    private val _index = MutableLiveData<NCERTIndex>()
    val index: LiveData<NCERTIndex> = _index
    
    fun loadBookDetails(bookId: String) {
        viewModelScope.launch {
            val bookResult = repository.getBook(bookId)
            bookResult.onSuccess { book ->
                _book.value = book
                
                // Load index if available
                book.indexId?.let {
                    val indexResult = repository.getIndex(bookId)
                    indexResult.onSuccess { index ->
                        _index.value = index
                    }
                }
            }
        }
    }
}

