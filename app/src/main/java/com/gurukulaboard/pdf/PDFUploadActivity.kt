package com.gurukulaboard.pdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityPdfUploadBinding
import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionSource
import com.gurukulaboard.models.QuestionStatus
import com.gurukulaboard.pdf.models.ExtractedQuestion
import com.gurukulaboard.questionbank.QuestionBankRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class PDFUploadActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPdfUploadBinding
    
    @Inject
    lateinit var pdfProcessor: PDFProcessor
    
    @Inject
    lateinit var questionBankRepository: QuestionBankRepository
    
    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processPDF(it) }
    }
    
    private var extractedQuestions: List<ExtractedQuestion> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.upload_pdf)
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewExtracted.layoutManager = LinearLayoutManager(this)
        // Adapter will be set after extraction
    }
    
    private fun setupClickListeners() {
        binding.btnSelectPdf.setOnClickListener {
            pdfPickerLauncher.launch("application/pdf")
        }
        
        binding.btnApproveAll.setOnClickListener {
            approveAllQuestions()
        }
    }
    
    private fun processPDF(uri: Uri) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Extract questions in background
        CoroutineScope(Dispatchers.IO).launch {
            val result = pdfProcessor.extractQuestionsFromUri(this@PDFUploadActivity, uri)
            
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = android.view.View.GONE
                
                result.onSuccess { questions ->
                    extractedQuestions = questions
                    displayExtractedQuestions(questions)
                    Toast.makeText(this@PDFUploadActivity, "Extracted ${questions.size} questions", Toast.LENGTH_SHORT).show()
                }.onFailure { exception ->
                    Toast.makeText(this@PDFUploadActivity, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun displayExtractedQuestions(questions: List<ExtractedQuestion>) {
        // Simple display - can be enhanced with proper adapter
        val questionText = questions.joinToString("\n\n") { 
            "Q: ${it.content}\nOptions: ${it.options?.joinToString(", ") ?: "N/A"}"
        }
        binding.tvExtractedQuestions.text = questionText
        binding.btnApproveAll.isEnabled = questions.isNotEmpty()
    }
    
    private fun approveAllQuestions() {
        if (extractedQuestions.isEmpty()) return
        
        CoroutineScope(Dispatchers.IO).launch {
            extractedQuestions.forEach { extracted ->
                val question = Question(
                    content = extracted.content,
                    type = extracted.type,
                    options = extracted.options,
                    answer = extracted.answer,
                    difficulty = extracted.difficulty ?: com.gurukulaboard.models.Difficulty.MEDIUM,
                    source = QuestionSource.KARNATAKA_DEPARTMENT,
                    sourceDetails = "Extracted from PDF",
                    status = QuestionStatus.PENDING
                )
                
                questionBankRepository.createQuestion(question)
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(this@PDFUploadActivity, "All questions added to pending queue", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

