package com.gurukulaboard.paper

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityPaperGeneratorBinding
import com.gurukulaboard.models.*
import com.gurukulaboard.paper.PaperPreviewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaperGeneratorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPaperGeneratorBinding
    private val viewModel: PaperGeneratorViewModel by viewModels()
    
    private var selectedExamType: ExamType = ExamType.PU_BOARD
    private var selectedDifficultyDistribution = PaperGenerator.getDefaultDistribution()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaperGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSpinners()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.generate_paper)
    }
    
    private fun setupSpinners() {
        val examTypes = ExamType.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, examTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerExamType.adapter = adapter
        
        binding.spinnerExamType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedExamType = ExamType.values()[position]
                loadAvailableQuestions()
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun setupObservers() {
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                is LoadingState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
        }
        
        viewModel.generatedPaper.observe(this) { questions ->
            if (questions.isNotEmpty()) {
                openPreview(questions)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnGenerate.setOnClickListener {
            generatePaper()
        }
    }
    
    private fun loadAvailableQuestions() {
        val subject = binding.etSubject.text.toString().trim()
        val classLevel = binding.etClass.text.toString().toIntOrNull() ?: 11
        
        if (subject.isNotBlank()) {
            viewModel.loadAvailableQuestions(selectedExamType, subject, classLevel)
        }
    }
    
    private fun generatePaper() {
        val subject = binding.etSubject.text.toString().trim()
        val classLevel = binding.etClass.text.toString().toIntOrNull() ?: 11
        val totalMarks = binding.etTotalMarks.text.toString().toIntOrNull() ?: 100
        
        if (subject.isBlank()) {
            Toast.makeText(this, "Please enter subject", Toast.LENGTH_SHORT).show()
            return
        }
        
        loadAvailableQuestions()
        
        viewModel.generatePaper(
            examType = selectedExamType,
            subject = subject,
            classLevel = classLevel,
            totalMarks = totalMarks,
            difficultyDistribution = selectedDifficultyDistribution,
            questionTypes = emptyList(),
            chapters = emptyList()
        )
    }
    
    private fun openPreview(questions: List<Question>) {
        val intent = Intent(this, PaperPreviewActivity::class.java)
        val questionIds = questions.map { it.id }.toTypedArray()
        intent.putExtra("QUESTION_IDS", questionIds)
        intent.putExtra("EXAM_TYPE", selectedExamType.name)
        startActivity(intent)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

