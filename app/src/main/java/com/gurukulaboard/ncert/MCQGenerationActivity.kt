package com.gurukulaboard.ncert

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.databinding.ActivityMcqGenerationBinding
import com.gurukulaboard.ncert.models.NCERTBook
import com.gurukulaboard.questionbank.QuestionBankRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MCQGenerationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMcqGenerationBinding
    private val viewModel: MCQGenerationViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var questionBankRepository: QuestionBankRepository
    
    private var bookId: String? = null
    private var chapterName: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMcqGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        bookId = intent.getStringExtra("BOOK_ID")
        chapterName = intent.getStringExtra("CHAPTER_NAME")
        
        setupToolbar()
        setupObservers()
        setupClickListeners()
        
        if (bookId != null && chapterName != null) {
            viewModel.loadContent(bookId!!, chapterName!!)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Generate MCQs"
    }
    
    private fun setupObservers() {
        viewModel.generatedMCQs.observe(this) { mcqs ->
            // Display generated MCQs
            val adapter = MCQPreviewAdapter(mcqs)
            binding.recyclerViewMCQs.layoutManager = LinearLayoutManager(this)
            binding.recyclerViewMCQs.adapter = adapter
        }
        
        viewModel.generatingState.observe(this) { state ->
            when (state) {
                is GeneratingState.Generating -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnGenerate.isEnabled = false
                }
                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnGenerate.isEnabled = true
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnGenerate.setOnClickListener {
            val numberOfQuestions = binding.etNumberOfQuestions.text.toString().toIntOrNull() ?: 5
            viewModel.generateMCQs(numberOfQuestions)
        }
        
        binding.btnSaveAll.setOnClickListener {
            saveAllMCQs()
        }
    }
    
    private fun saveAllMCQs() {
        val userId = sessionManager.getUserId() ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val mcqs = viewModel.generatedMCQs.value ?: return@launch
            var savedCount = 0
            
            for (mcq in mcqs) {
                val question = viewModel.convertToQuestion(mcq, userId)
                val result = questionBankRepository.createQuestion(question)
                if (result.isSuccess) {
                    savedCount++
                }
            }
            
            Toast.makeText(this@MCQGenerationActivity, "Saved $savedCount MCQs to question bank", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

