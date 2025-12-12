package com.gurukulaboard.paper

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityPaperPreviewBinding
import com.gurukulaboard.models.ExamType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaperPreviewActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPaperPreviewBinding
    private val viewModel: PaperGeneratorViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaperPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadQuestions()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Paper Preview"
    }
    
    private fun loadQuestions() {
        val questionIds = intent.getStringArrayExtra("QUESTION_IDS") ?: return
        val examTypeName = intent.getStringExtra("EXAM_TYPE") ?: ExamType.PU_BOARD.name
        
        // Display preview - questions are already loaded in ViewModel
        viewModel.generatedPaper.observe(this) { questions ->
            displayPreview(questions)
        }
    }
    
    private fun displayPreview(questions: List<com.gurukulaboard.models.Question>) {
        val previewText = questions.mapIndexed { index, question ->
            "${index + 1}. ${question.content}\n"
        }.joinToString("\n")
        
        binding.tvPreview.text = previewText
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

