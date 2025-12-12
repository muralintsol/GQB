package com.gurukulaboard.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gurukulaboard.auth.AuthRepository
import com.gurukulaboard.models.QuestionStatus
import com.gurukulaboard.models.UserRole
import com.gurukulaboard.questionbank.QuestionBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val questionBankRepository: QuestionBankRepository
) : ViewModel() {
    
    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics
    
    private val _createUserState = MutableLiveData<CreateUserState>()
    val createUserState: LiveData<CreateUserState> = _createUserState
    
    fun createTeacherAccount(mobileNumber: String, pin: String, name: String) {
        _createUserState.value = CreateUserState.Loading
        
        viewModelScope.launch {
            val result = authRepository.createUser(mobileNumber, pin, name, UserRole.TEACHER)
            
            result.onSuccess {
                _createUserState.value = CreateUserState.Success("User created successfully")
            }.onFailure { exception ->
                _createUserState.value = CreateUserState.Error(exception.message ?: "Failed to create user")
            }
        }
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                // Load all questions
                val allQuestionsResult = questionBankRepository.getQuestions(limit = 1000)
                
                allQuestionsResult.onSuccess { questions ->
                    val totalQuestions = questions.size
                    val pendingQuestions = questions.count { it.status == QuestionStatus.PENDING }
                    val approvedQuestions = questions.count { it.status == QuestionStatus.APPROVED }
                    
                    val stats = Statistics(
                        totalQuestions = totalQuestions,
                        pendingQuestions = pendingQuestions,
                        approvedQuestions = approvedQuestions
                    )
                    _statistics.value = stats
                }.onFailure {
                    // On error, set default stats
                    _statistics.value = Statistics(0, 0, 0)
                }
            } catch (e: Exception) {
                _statistics.value = Statistics(0, 0, 0)
            }
        }
    }
}

data class Statistics(
    val totalQuestions: Int,
    val pendingQuestions: Int,
    val approvedQuestions: Int
)

sealed class CreateUserState {
    object Idle : CreateUserState()
    object Loading : CreateUserState()
    data class Success(val message: String) : CreateUserState()
    data class Error(val message: String) : CreateUserState()
}

