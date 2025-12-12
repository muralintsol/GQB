package com.gurukulaboard.questionbank

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.databinding.ActivityQuestionDetailBinding
import com.gurukulaboard.models.Question
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuestionDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuestionDetailBinding
    private val viewModel: QuestionBankViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private var questionId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        questionId = intent.getStringExtra("QUESTION_ID")
        
        setupToolbar()
        loadQuestion()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Question Details"
    }
    
    private fun loadQuestion() {
        questionId?.let { id ->
            viewModel.questions.observe(this) { questions ->
                val question = questions.find { it.id == id }
                question?.let { displayQuestion(it) }
            }
            viewModel.loadQuestions()
        }
    }
    
    private fun displayQuestion(question: Question) {
        binding.apply {
            tvContent.text = question.content
            tvSubject.text = "Subject: ${question.subject}"
            tvClass.text = "Class: ${question.`class`}"
            tvChapter.text = "Chapter: ${question.chapter}"
            tvDifficulty.text = "Difficulty: ${question.difficulty.name}"
            tvType.text = "Type: ${question.type.name}"
            tvStatus.text = "Status: ${question.status.name}"
            tvSource.text = "Source: ${question.source.name}"
            question.sourceDetails?.let {
                tvSourceDetails.text = "Source Details: $it"
                tvSourceDetails.visibility = android.view.View.VISIBLE
            } ?: run {
                tvSourceDetails.visibility = android.view.View.GONE
            }
            
            question.answer?.let {
                tvAnswer.text = "Answer: $it"
                tvAnswer.visibility = android.view.View.VISIBLE
            } ?: run {
                tvAnswer.visibility = android.view.View.GONE
            }
            
            if (question.status == com.gurukulaboard.models.QuestionStatus.PENDING) {
                btnApprove.visibility = android.view.View.VISIBLE
                btnReject.visibility = android.view.View.VISIBLE
            } else {
                btnApprove.visibility = android.view.View.GONE
                btnReject.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnApprove.setOnClickListener {
            questionId?.let { id ->
                val userId = sessionManager.getUserId() ?: return@setOnClickListener
                viewModel.approveQuestion(id, userId)
                Toast.makeText(this, "Question approved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
        binding.btnReject.setOnClickListener {
            questionId?.let { id ->
                viewModel.rejectQuestion(id)
                Toast.makeText(this, "Question rejected", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

