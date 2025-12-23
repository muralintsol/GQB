package com.gurukulaboard.content

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.content.ContentMCQGenerator.GeneratedMCQ
import com.gurukulaboard.content.adapters.MCQPreviewAdapter
import com.gurukulaboard.content.models.ContentSection
import com.gurukulaboard.databinding.ActivityMcqGenerationBinding
import com.gurukulaboard.models.Difficulty
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MCQGenerationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMcqGenerationBinding
    private val viewModel: MCQGenerationViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private lateinit var adapter: MCQPreviewAdapter
    private var contentId: String? = null
    private var fileUrl: String? = null
    private var subject: String = ""
    private var classLevel: Int = 11
    private var chapter: String? = null
    private var selectedSections: List<ContentSection>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMcqGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        contentId = intent.getStringExtra("CONTENT_ID")
        fileUrl = intent.getStringExtra("FILE_URL")
        subject = intent.getStringExtra("SUBJECT") ?: ""
        classLevel = intent.getIntExtra("CLASS_LEVEL", 11)
        chapter = intent.getStringExtra("CHAPTER")
        
        // Get selected sections if provided
        val sectionsList = intent.getParcelableArrayListExtra<ContentSection>("SELECTED_SECTIONS")
        selectedSections = sectionsList?.toList()
        
        if (fileUrl == null || contentId == null) {
            Toast.makeText(this, "Invalid content", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupSpinners()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.generate_mcq)
    }
    
    private fun setupSpinners() {
        // Number of questions
        val questionCounts = listOf("5", "10", "15", "20", "25", "30")
        val countAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, questionCounts)
        countAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuestionCount.adapter = countAdapter
        
        // Difficulty
        val difficulties = Difficulty.values().map { it.name }
        val difficultyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficulties)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDifficulty.adapter = difficultyAdapter
    }
    
    private fun setupRecyclerView() {
        adapter = MCQPreviewAdapter { mcq ->
            // Show answer
            Toast.makeText(this, "Answer: ${mcq.options[mcq.correctAnswer]}", Toast.LENGTH_LONG).show()
        }
        binding.recyclerViewMCQs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMCQs.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.generatedMCQs.observe(this) { mcqs ->
            adapter.submitList(mcqs)
            binding.tvGeneratedCount.text = "${mcqs.size} questions generated"
            binding.btnSave.isEnabled = mcqs.isNotEmpty()
        }
        
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                is LoadingState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnGenerate.isEnabled = false
                }
                is LoadingState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnGenerate.isEnabled = true
                }
                is LoadingState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnGenerate.isEnabled = true
                }
                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnGenerate.isEnabled = true
                }
            }
        }
        
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                binding.root.showSnackbar(it)
            }
        }
        
        viewModel.savedQuestions.observe(this) { questionIds ->
            if (questionIds.isNotEmpty()) {
                Toast.makeText(this, "${questionIds.size} MCQs saved to question bank", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnGenerate.setOnClickListener {
            generateMCQs()
        }
        
        binding.btnSave.setOnClickListener {
            saveMCQs()
        }
    }
    
    private fun generateMCQs() {
        val numberOfQuestions = binding.spinnerQuestionCount.selectedItem.toString().toIntOrNull() ?: 5
        val difficulty = Difficulty.values()[binding.spinnerDifficulty.selectedItemPosition]
        val userId = sessionManager.getUserId() ?: return
        
        fileUrl?.let { url ->
            if (selectedSections != null && selectedSections!!.isNotEmpty()) {
                // Generate from selected sections
                viewModel.generateMCQsFromSections(
                    context = this,
                    fileUrl = url,
                    sections = selectedSections!!,
                    numberOfQuestions = numberOfQuestions,
                    difficulty = difficulty,
                    subject = subject,
                    classLevel = classLevel,
                    chapter = chapter,
                    contentId = contentId ?: "",
                    createdBy = userId
                )
            } else {
                // Generate from entire PDF (existing behavior)
                viewModel.generateMCQs(
                    context = this,
                    fileUrl = url,
                    numberOfQuestions = numberOfQuestions,
                    difficulty = difficulty,
                    subject = subject,
                    classLevel = classLevel,
                    chapter = chapter,
                    contentId = contentId ?: "",
                    createdBy = userId
                )
            }
        }
    }
    
    private fun saveMCQs() {
        val mcqs = viewModel.generatedMCQs.value ?: return
        if (mcqs.isEmpty()) return
        
        val userId = sessionManager.getUserId() ?: return
        
        viewModel.saveMCQsToQuestionBank(
            mcqs = mcqs,
            subject = subject,
            classLevel = classLevel,
            chapter = chapter,
            contentId = contentId ?: "",
            createdBy = userId
        )
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

