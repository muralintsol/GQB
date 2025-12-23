package com.gurukulaboard.content

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.gurukulaboard.R
import com.gurukulaboard.content.adapters.ContentSectionAdapter
import com.gurukulaboard.content.models.ContentSection
import com.gurukulaboard.content.models.SectionType
import com.gurukulaboard.databinding.ActivityContentSelectionBinding
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContentSelectionActivity : AppCompatActivity() {
    
    companion object {
        const val MODE_PPT = "PPT"
        const val MODE_MCQ = "MCQ"
        const val EXTRA_MODE = "MODE"
    }
    
    private lateinit var binding: ActivityContentSelectionBinding
    
    @Inject
    lateinit var pdfContentAnalyzer: PDFContentAnalyzer
    
    @Inject
    lateinit var contentSectionsRepository: ContentSectionsRepository
    
    private lateinit var sectionAdapter: ContentSectionAdapter
    private var allSections: List<ContentSection> = emptyList()
    private var filteredSections: List<ContentSection> = emptyList()
    private var currentFilter: SectionType? = null
    
    private var contentId: String? = null
    private var fileUrl: String? = null
    private var subject: String = ""
    private var classLevel: Int = 11
    private var chapter: String? = null
    private var mode: String = MODE_PPT // Default to PPT mode
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        contentId = intent.getStringExtra("CONTENT_ID")
        fileUrl = intent.getStringExtra("FILE_URL")
        subject = intent.getStringExtra("SUBJECT") ?: ""
        classLevel = intent.getIntExtra("CLASS_LEVEL", 11)
        chapter = intent.getStringExtra("CHAPTER")
        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_PPT
        
        if (fileUrl == null || contentId == null) {
            Toast.makeText(this, "Invalid content", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
        updateUIForMode()
        loadSections()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.select_sections)
    }
    
    private fun setupRecyclerView() {
        sectionAdapter = ContentSectionAdapter { section, isSelected ->
            updateSelectedCount()
        }
        binding.recyclerViewSections.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSections.adapter = sectionAdapter
    }
    
    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            filterSections(null)
        }
        
        binding.chipSubtopic.setOnClickListener {
            filterSections(SectionType.SUBTOPIC)
        }
        
        binding.chipExercise.setOnClickListener {
            filterSections(SectionType.EXERCISE)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSelectAll.setOnClickListener {
            selectAllSections(true)
        }
        
        binding.btnDeselectAll.setOnClickListener {
            selectAllSections(false)
        }
        
        binding.btnGeneratePPT.setOnClickListener {
            when (mode) {
                MODE_MCQ -> generateMCQFromSelected()
                else -> generatePPTFromSelected()
            }
        }
    }
    
    private fun loadSections() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvNoSections.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                // First, try to load from cache
                val cachedResult = contentSectionsRepository.getSections(contentId!!)
                cachedResult.onSuccess { cachedSections ->
                    if (cachedSections.isNotEmpty()) {
                        // Use cached sections
                        allSections = cachedSections
                        filteredSections = cachedSections
                        sectionAdapter.submitList(cachedSections)
                        updateSelectedCount()
                        binding.progressBar.visibility = View.GONE
                        
                        // Show cached indicator
                        supportActionBar?.subtitle = getString(R.string.loaded_from_cache)
                        
                        if (cachedSections.isEmpty()) {
                            binding.tvNoSections.visibility = View.VISIBLE
                            binding.tvNoSections.text = getString(R.string.no_sections_detected)
                        }
                    } else {
                        // No cache, analyze PDF
                        analyzePDF()
                    }
                }.onFailure {
                    // Cache read failed, analyze PDF
                    analyzePDF()
                }
            } catch (e: Exception) {
                // Fallback to PDF analysis
                analyzePDF()
            }
        }
    }
    
    private fun analyzePDF() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvNoSections.visibility = View.GONE
        supportActionBar?.subtitle = getString(R.string.analyzing_pdf)
        
        lifecycleScope.launch {
            try {
                val result = pdfContentAnalyzer.analyzePDF(this@ContentSelectionActivity, fileUrl!!)
                result.onSuccess { sections ->
                    allSections = sections
                    filteredSections = sections
                    sectionAdapter.submitList(sections)
                    updateSelectedCount()
                    
                    // Save to cache for future use
                    contentSectionsRepository.saveSections(contentId!!, sections, fileUrl)
                    
                    supportActionBar?.subtitle = null
                    
                    if (sections.isEmpty()) {
                        binding.tvNoSections.visibility = View.VISIBLE
                        binding.tvNoSections.text = getString(R.string.no_sections_detected)
                    }
                    
                    binding.progressBar.visibility = View.GONE
                }.onFailure { exception ->
                    binding.progressBar.visibility = View.GONE
                    binding.root.showSnackbar(exception.message ?: "Failed to analyze PDF")
                    binding.tvNoSections.visibility = View.VISIBLE
                    binding.tvNoSections.text = getString(R.string.failed_to_analyze_pdf)
                    supportActionBar?.subtitle = null
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.root.showSnackbar("Error: ${e.message}")
                supportActionBar?.subtitle = null
            }
        }
    }
    
    private fun reAnalyzePDF() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvNoSections.visibility = View.GONE
        supportActionBar?.subtitle = getString(R.string.re_analyzing_pdf)
        
        lifecycleScope.launch {
            try {
                val result = pdfContentAnalyzer.analyzePDF(this@ContentSelectionActivity, fileUrl!!)
                result.onSuccess { sections ->
                    allSections = sections
                    filteredSections = sections
                    sectionAdapter.submitList(sections)
                    updateSelectedCount()
                    
                    // Update cache
                    contentSectionsRepository.saveSections(contentId!!, sections, fileUrl)
                    
                    supportActionBar?.subtitle = null
                    binding.root.showSnackbar(getString(R.string.re_analysis_complete))
                    
                    if (sections.isEmpty()) {
                        binding.tvNoSections.visibility = View.VISIBLE
                        binding.tvNoSections.text = getString(R.string.no_sections_detected)
                    }
                    
                    binding.progressBar.visibility = View.GONE
                }.onFailure { exception ->
                    binding.progressBar.visibility = View.GONE
                    binding.root.showSnackbar(exception.message ?: "Failed to re-analyze PDF")
                    supportActionBar?.subtitle = null
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.root.showSnackbar("Error: ${e.message}")
                supportActionBar?.subtitle = null
            }
        }
    }
    
    private fun filterSections(type: SectionType?) {
        currentFilter = type
        
        // Update chip selection
        binding.chipAll.isChecked = type == null
        binding.chipSubtopic.isChecked = type == SectionType.SUBTOPIC
        binding.chipExercise.isChecked = type == SectionType.EXERCISE
        
        filteredSections = if (type == null) {
            allSections
        } else {
            allSections.filter { it.type == type }
        }
        
        sectionAdapter.submitList(filteredSections)
        updateSelectedCount()
    }
    
    private fun selectAllSections(select: Boolean) {
        filteredSections.forEach { section ->
            section.isSelected = select
        }
        sectionAdapter.notifyDataSetChanged()
        updateSelectedCount()
    }
    
    private fun updateUIForMode() {
        when (mode) {
            MODE_MCQ -> {
                binding.btnGeneratePPT.text = getString(R.string.generate_mcqs)
            }
            else -> {
                binding.btnGeneratePPT.text = getString(R.string.generate_ppt)
            }
        }
    }
    
    private fun updateSelectedCount() {
        val selectedCount = allSections.count { it.isSelected }
        binding.tvSelectedCount.text = getString(R.string.selected_count, selectedCount)
        binding.btnGeneratePPT.isEnabled = selectedCount > 0
    }
    
    private fun generatePPTFromSelected() {
        val selectedSections = allSections.filter { it.isSelected }
        
        if (selectedSections.isEmpty()) {
            binding.root.showSnackbar(getString(R.string.please_select_sections))
            return
        }
        
        // Pass selected sections to PPTEditorActivity
        val intent = Intent(this, PPTEditorActivity::class.java)
        intent.putExtra("CONTENT_ID", contentId)
        intent.putExtra("FILE_URL", fileUrl)
        intent.putExtra("SUBJECT", subject)
        intent.putExtra("CLASS_LEVEL", classLevel)
        intent.putExtra("CHAPTER", chapter)
        intent.putParcelableArrayListExtra("SELECTED_SECTIONS", ArrayList(selectedSections))
        startActivity(intent)
        finish()
    }
    
    private fun generateMCQFromSelected() {
        val selectedSections = allSections.filter { it.isSelected }
        
        if (selectedSections.isEmpty()) {
            binding.root.showSnackbar(getString(R.string.please_select_sections))
            return
        }
        
        // Pass selected sections to MCQGenerationActivity
        val intent = Intent(this, MCQGenerationActivity::class.java)
        intent.putExtra("CONTENT_ID", contentId)
        intent.putExtra("FILE_URL", fileUrl)
        intent.putExtra("SUBJECT", subject)
        intent.putExtra("CLASS_LEVEL", classLevel)
        intent.putExtra("CHAPTER", chapter)
        intent.putParcelableArrayListExtra("SELECTED_SECTIONS", ArrayList(selectedSections))
        startActivity(intent)
        finish()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_content_selection, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_reanalyze -> {
                reAnalyzePDF()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

