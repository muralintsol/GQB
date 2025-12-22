package com.gurukulaboard.ncert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.ncert.models.NCERTBook
import com.gurukulaboard.ncert.models.NCERTBookStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NCERTManagementViewModel @Inject constructor(
    private val zipExtractor: ZipExtractor,
    private val indexParser: NCERTIndexParser,
    private val storageManager: NCERTStorageManager,
    private val repository: NCERTRepository
) : ViewModel() {
    
    private val _books = MutableLiveData<List<NCERTBook>>()
    val books: LiveData<List<NCERTBook>> = _books
    
    private val _processingState = MutableLiveData<ProcessingState>()
    val processingState: LiveData<ProcessingState> = _processingState
    
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    
    fun loadBooks() {
        viewModelScope.launch {
            val result = repository.getAllBooks()
            result.onSuccess { bookList ->
                _books.value = bookList
            }.onFailure { exception ->
                _message.value = "Error loading books: ${exception.message}"
            }
        }
    }
    
    fun processZipFiles(ncertBooksFolder: File) {
        _processingState.value = ProcessingState.Processing
        
        viewModelScope.launch {
            try {
                // Extract PDFs from zip files
                val extractResult = zipExtractor.extractPDFsFromZipFiles(ncertBooksFolder)
                
                extractResult.onSuccess { extractedPDFs ->
                    var processedCount = 0
                    var errorCount = 0
                    
                    for (extractedPDF in extractedPDFs) {
                        try {
                            // Create book record
                            val book = zipExtractor.createNCERTBook(extractedPDF)
                            val bookResult = repository.saveBook(book)
                            
                            bookResult.onSuccess { bookId ->
                                // Upload to Firebase Storage
                                val uploadResult = storageManager.uploadNCERTPDF(
                                    extractedPDF.file,
                                    extractedPDF.subject,
                                    extractedPDF.classLevel
                                )
                                
                                uploadResult.onSuccess { downloadUrl ->
                                    // Update book with download URL
                                    val updatedBook = book.copy(
                                        id = bookId,
                                        firebaseStorageUrl = downloadUrl,
                                        status = NCERTBookStatus.PROCESSING
                                    )
                                    repository.updateBook(bookId, updatedBook)
                                    
                                    // Parse index
                                    val indexResult = indexParser.parseIndexFromPDF(extractedPDF.file, bookId)
                                    
                                    indexResult.onSuccess { index ->
                                        // Save index
                                        val indexSaveResult = repository.saveIndex(index)
                                        
                                        indexSaveResult.onSuccess { indexId ->
                                            // Update book with index ID and status
                                            val finalBook = updatedBook.copy(
                                                indexId = indexId,
                                                status = NCERTBookStatus.INDEXED,
                                                processedAt = com.google.firebase.Timestamp.now()
                                            )
                                            repository.updateBook(bookId, finalBook)
                                            processedCount++
                                        }.onFailure {
                                            errorCount++
                                        }
                                    }.onFailure {
                                        errorCount++
                                    }
                                }.onFailure {
                                    errorCount++
                                }
                            }.onFailure {
                                errorCount++
                            }
                        } catch (e: Exception) {
                            errorCount++
                        }
                    }
                    
                    _message.value = "Processed $processedCount books. Errors: $errorCount"
                    _processingState.value = ProcessingState.Idle
                    loadBooks() // Reload books list
                }.onFailure { exception ->
                    _message.value = "Error extracting PDFs: ${exception.message}"
                    _processingState.value = ProcessingState.Idle
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                _processingState.value = ProcessingState.Idle
            }
        }
    }
    
    fun processBook(bookId: String) {
        viewModelScope.launch {
            _processingState.value = ProcessingState.Processing
            
            val bookResult = repository.getBook(bookId)
            bookResult.onSuccess { book ->
                if (book.localFilePath != null) {
                    val pdfFile = File(book.localFilePath)
                    if (pdfFile.exists()) {
                        // Parse index if not already done
                        if (book.indexId == null) {
                            val indexResult = indexParser.parseIndexFromPDF(pdfFile, bookId)
                            indexResult.onSuccess { index ->
                                val indexSaveResult = repository.saveIndex(index)
                                indexSaveResult.onSuccess { indexId ->
                                    val updatedBook = book.copy(
                                        indexId = indexId,
                                        status = NCERTBookStatus.INDEXED,
                                        processedAt = com.google.firebase.Timestamp.now()
                                    )
                                    repository.updateBook(bookId, updatedBook)
                                    _message.value = "Book processed successfully"
                                    loadBooks()
                                }
                            }
                        }
                    }
                }
            }
            
            _processingState.value = ProcessingState.Idle
        }
    }
}

sealed class ProcessingState {
    object Idle : ProcessingState()
    object Processing : ProcessingState()
}

