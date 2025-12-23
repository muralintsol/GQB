package com.gurukulaboard.content

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityMcqDisplayBinding
import com.gurukulaboard.models.Question
import com.gurukulaboard.questionbank.QuestionBankRepository
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MCQDisplayActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMcqDisplayBinding
    
    @Inject
    lateinit var questionBankRepository: QuestionBankRepository
    
    private var contentId: String? = null
    private var questions: List<Question> = emptyList()
    private var currentQuestionIndex: Int = 0
    private var isAnswerRevealed: Boolean = false
    private var isFullScreen: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMcqDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        contentId = intent.getStringExtra("CONTENT_ID")
        if (contentId == null) {
            Toast.makeText(this, "Content ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupClickListeners()
        loadMCQs()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.qa_session)
    }
    
    private fun setupClickListeners() {
        binding.btnPrevious.setOnClickListener {
            showPreviousQuestion()
        }
        
        binding.btnNext.setOnClickListener {
            showNextQuestion()
        }
        
        binding.btnRevealAnswer.setOnClickListener {
            revealAnswer()
        }
        
        binding.btnFullScreen.setOnClickListener {
            toggleFullScreen()
        }
    }
    
    private fun loadMCQs() {
        contentId?.let { id ->
            // Load MCQs linked to this content
            // MCQs are stored in question bank with sourceDetails = "teacherContent:{contentId}"
            lifecycleScope.launch {
                val result = questionBankRepository.getQuestions(
                    searchQuery = null,
                    limit = 1000
                )
                result.onSuccess { allQuestions ->
                    questions = allQuestions.filter { question ->
                        question.sourceDetails?.startsWith("teacherContent:$id") == true
                    }
                    
                    if (questions.isEmpty()) {
                        binding.root.showSnackbar("No MCQs found for this content")
                        binding.tvNoQuestions.visibility = View.VISIBLE
                        binding.layoutQuestionDisplay.visibility = View.GONE
                    } else {
                        binding.tvNoQuestions.visibility = View.GONE
                        binding.layoutQuestionDisplay.visibility = View.VISIBLE
                        currentQuestionIndex = 0
                        displayQuestion(questions[0])
                    }
                }.onFailure { exception ->
                    binding.root.showSnackbar(exception.message ?: "Failed to load MCQs")
                }
            }
        }
    }
    
    private fun displayQuestion(question: Question) {
        binding.tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        binding.tvQuestion.text = question.content
        
        // Display options
        val optionsText = question.options?.mapIndexed { index, option ->
            "${('a' + index)}. $option"
        }?.joinToString("\n") ?: "No options available"
        
        binding.tvOptions.text = optionsText
        
        // Hide answer initially
        binding.tvAnswer.visibility = View.GONE
        binding.tvAnswerLabel.visibility = View.GONE
        binding.btnRevealAnswer.visibility = View.VISIBLE
        isAnswerRevealed = false
        
        // Update navigation buttons
        binding.btnPrevious.isEnabled = currentQuestionIndex > 0
        binding.btnNext.isEnabled = currentQuestionIndex < questions.size - 1
        
        // Update progress
        val progress = ((currentQuestionIndex + 1).toFloat() / questions.size * 100).toInt()
        binding.progressBar.progress = progress
    }
    
    private fun revealAnswer() {
        if (isAnswerRevealed) return
        
        val question = questions[currentQuestionIndex]
        val answerText = "Answer: ${question.answer ?: "N/A"}"
        
        binding.tvAnswer.text = answerText
        binding.tvAnswer.visibility = View.VISIBLE
        binding.tvAnswerLabel.visibility = View.VISIBLE
        binding.btnRevealAnswer.visibility = View.GONE
        isAnswerRevealed = true
    }
    
    private fun showNextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            displayQuestion(questions[currentQuestionIndex])
        }
    }
    
    private fun showPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            displayQuestion(questions[currentQuestionIndex])
        }
    }
    
    private fun toggleFullScreen() {
        if (isFullScreen) {
            // Exit full screen
            supportActionBar?.show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            binding.toolbar.visibility = View.VISIBLE
            binding.btnFullScreen.text = getString(R.string.full_screen)
            isFullScreen = false
        } else {
            // Enter full screen
            supportActionBar?.hide()
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            binding.toolbar.visibility = View.GONE
            binding.btnFullScreen.text = getString(R.string.exit_full_screen)
            isFullScreen = true
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

