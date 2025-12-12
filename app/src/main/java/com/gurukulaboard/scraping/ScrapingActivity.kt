package com.gurukulaboard.scraping

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityScrapingBinding
import com.gurukulaboard.models.ExamType
import com.gurukulaboard.models.Subject
import com.gurukulaboard.utils.ChaptersData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScrapingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityScrapingBinding
    private val viewModel: ScrapingViewModel by viewModels()
    
    private var selectedSource: String = "NCERT"
    private var selectedExamType: ExamType = ExamType.PU_BOARD
    private var selectedSubject: String = ""
    private var selectedChapter: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrapingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSourceSelection()
        setupSubjectSpinner()
        setupClassListener()
        setupObservers()
        setupClickListeners()
        
        // Initialize with first subject and chapter if competitive exam is selected
        if (selectedSource in listOf("NEET", "JEE", "K-CET")) {
            val competitiveSubjects = ChaptersData.getSubjectsForCompetitiveExams()
            if (competitiveSubjects.isNotEmpty()) {
                selectedSubject = competitiveSubjects[0]
                updateChapterSpinner()
            }
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.scraping)
    }
    
    private fun setupSourceSelection() {
        val sources = arrayOf("NCERT", "Karnataka PU", "NEET", "JEE", "K-CET")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sources)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSource.adapter = adapter
        
        binding.spinnerSource.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedSource = sources[position]
                updateUIForSource()
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun setupSubjectSpinner() {
        // For competitive exams, show PCMB subjects
        val competitiveSubjects = ChaptersData.getSubjectsForCompetitiveExams()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, competitiveSubjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubject.adapter = adapter
        
        binding.spinnerSubject.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedSubject = competitiveSubjects[position]
                updateChapterSpinner()
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun setupClassListener() {
        binding.etClass.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && selectedSubject.isNotBlank()) {
                updateChapterSpinner()
            }
        }
        
        // Also listen to text changes
        binding.etClass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (selectedSubject.isNotBlank()) {
                    updateChapterSpinner()
                }
            }
        })
    }
    
    private fun updateChapterSpinner() {
        val classLevel = binding.etClass.text.toString().toIntOrNull() ?: 11
        val chapters = ChaptersData.getChaptersForSubject(selectedSubject, classLevel)
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chapters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerChapter.adapter = adapter
        
        binding.spinnerChapter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (chapters.isNotEmpty()) {
                    selectedChapter = chapters[position]
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun updateUIForSource() {
        when (selectedSource) {
            "NCERT" -> {
                binding.tilChapter.visibility = View.VISIBLE
                binding.spinnerExamType.visibility = View.GONE
                binding.tilSubject.visibility = View.VISIBLE
            }
            "Karnataka PU" -> {
                binding.tilChapter.visibility = View.GONE
                binding.spinnerExamType.visibility = View.VISIBLE
                binding.tilSubject.visibility = View.VISIBLE
                setupExamTypeSpinner()
            }
            "NEET", "JEE", "K-CET" -> {
                binding.tilChapter.visibility = View.VISIBLE
                binding.spinnerExamType.visibility = View.GONE
                binding.tilSubject.visibility = View.VISIBLE
                // Update subject spinner for competitive exams
                setupSubjectSpinner()
                // Initialize chapter spinner if subject is already selected
                if (selectedSubject.isNotBlank()) {
                    updateChapterSpinner()
                }
            }
        }
    }
    
    private fun setupExamTypeSpinner() {
        val examTypes = ExamType.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, examTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerExamType.adapter = adapter
        
        binding.spinnerExamType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedExamType = ExamType.values()[position]
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun setupObservers() {
        viewModel.scrapingState.observe(this) { state ->
            when (state) {
                is ScrapingState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnScrape.isEnabled = false
                }
                is ScrapingState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnScrape.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is ScrapingState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnScrape.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
        
        viewModel.scrapedQuestions.observe(this) { questions ->
            if (questions.isNotEmpty()) {
                Toast.makeText(this, "${questions.size} questions scraped and added to pending", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnScrape.setOnClickListener {
            val classLevel = binding.etClass.text.toString().toIntOrNull() ?: 11
            val subject = selectedSubject.ifBlank { binding.spinnerSubject.selectedItem?.toString() ?: "" }
            val chapter = selectedChapter.ifBlank { binding.spinnerChapter.selectedItem?.toString() ?: "" }
            
            if (subject.isBlank()) {
                Toast.makeText(this, "Please select subject", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            when (selectedSource) {
                "NCERT" -> {
                    if (chapter.isBlank()) {
                        Toast.makeText(this, "Please select chapter", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.scrapeNCERT(classLevel, subject, chapter)
                }
                "Karnataka PU" -> {
                    viewModel.scrapeKarnataka(subject, classLevel, selectedExamType)
                }
                "NEET" -> {
                    if (chapter.isBlank()) {
                        Toast.makeText(this, "Please select chapter", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.scrapeCompetitiveExam(ExamType.NEET, subject, classLevel, chapter)
                }
                "JEE" -> {
                    if (chapter.isBlank()) {
                        Toast.makeText(this, "Please select chapter", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.scrapeCompetitiveExam(ExamType.JEE, subject, classLevel, chapter)
                }
                "K-CET" -> {
                    if (chapter.isBlank()) {
                        Toast.makeText(this, "Please select chapter", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.scrapeCompetitiveExam(ExamType.K_CET, subject, classLevel, chapter)
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

