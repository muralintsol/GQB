package com.gurukulaboard.content

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.content.models.ContentType
import com.gurukulaboard.content.models.TeachingContent
import com.gurukulaboard.databinding.ActivityContentUploadBinding
import com.gurukulaboard.models.Subject
import com.gurukulaboard.utils.ChaptersData
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContentUploadActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityContentUploadBinding
    private val viewModel: ContentDashboardViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var pdfContentAnalyzer: PDFContentAnalyzer
    
    @Inject
    lateinit var contentSectionsRepository: ContentSectionsRepository
    
    private var selectedFileUri: Uri? = null
    private var selectedSubject: String = ""
    private var selectedClass: Int = 11
    private val tags = mutableListOf<String>()
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            binding.tvSelectedFile.text = getFileName(it)
            binding.btnSelectFile.text = getString(R.string.change_file)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSpinners()
        setupClickListeners()
        setupObservers()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.upload_content)
    }
    
    private fun setupSpinners() {
        // Subject spinner
        val subjects = Subject.PU_BOARD_SUBJECTS.map { it.name }
        val subjectAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubject.adapter = subjectAdapter
        
        binding.spinnerSubject.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedSubject = subjects[position]
                updateChapterSpinner()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        // Class spinner
        val classes = listOf("11", "12")
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, classes)
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClass.adapter = classAdapter
        
        binding.spinnerClass.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedClass = classes[position].toInt()
                updateChapterSpinner()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        // Content Type spinner
        val contentTypes = ContentType.values().map { it.name }
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, contentTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerContentType.adapter = typeAdapter
    }
    
    private fun updateChapterSpinner() {
        val chapters = ChaptersData.getChaptersForSubject(selectedSubject, selectedClass)
        val chapterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("General") + chapters)
        chapterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerChapter.adapter = chapterAdapter
    }
    
    private fun setupClickListeners() {
        binding.btnSelectFile.setOnClickListener {
            filePickerLauncher.launch("application/pdf")
        }
        
        binding.btnAddTag.setOnClickListener {
            val tag = binding.etTag.text?.toString()?.trim() ?: ""
            if (tag.isNotBlank() && !tags.contains(tag)) {
                tags.add(tag)
                updateTagsDisplay()
                binding.etTag.text?.clear()
            }
        }
        
        binding.btnUpload.setOnClickListener {
            uploadContent()
        }
    }
    
    private fun setupObservers() {
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                is LoadingState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnUpload.isEnabled = false
                }
                is LoadingState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnUpload.isEnabled = true
                    Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
                is LoadingState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnUpload.isEnabled = true
                }
                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnUpload.isEnabled = true
                }
            }
        }
        
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                binding.root.showSnackbar(it)
            }
        }
    }
    
    private fun updateTagsDisplay() {
        binding.chipGroupTags.removeAllViews()
        tags.forEach { tag ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = tag
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                tags.remove(tag)
                updateTagsDisplay()
            }
            binding.chipGroupTags.addView(chip)
        }
    }
    
    private fun uploadContent() {
        if (selectedFileUri == null) {
            Toast.makeText(this, getString(R.string.please_select_file), Toast.LENGTH_SHORT).show()
            return
        }
        
        val title = binding.etTitle.text.toString().trim()
        if (title.isBlank()) {
            binding.etTitle.error = getString(R.string.title_required)
            return
        }
        
        val description = binding.etDescription.text.toString().trim()
        val chapter = if (binding.spinnerChapter.selectedItemPosition == 0) null 
                     else binding.spinnerChapter.selectedItem.toString()
        val contentType = ContentType.values()[binding.spinnerContentType.selectedItemPosition]
        val fileName = getFileName(selectedFileUri!!) ?: "content_${System.currentTimeMillis()}.pdf"
        
        val userId = sessionManager.getUserId() ?: return
        val userName = sessionManager.getUserName() ?: "Unknown"
        
        val content = TeachingContent(
            title = title,
            description = description.ifBlank { null },
            fileName = fileName,
            fileUrl = "", // Will be set after upload
            fileSize = 0L, // Will be set after upload
            subject = selectedSubject,
            classLevel = selectedClass,
            chapter = chapter,
            tags = tags,
            contentType = contentType,
            uploadedBy = userId,
            uploadedByName = userName
        )
        
        lifecycleScope.launch {
            val result = viewModel.uploadContent(content, selectedFileUri!!, userId)
            result.onSuccess { contentId ->
                Toast.makeText(this@ContentUploadActivity, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                
                // Get the fileUrl from the uploaded content to trigger analysis
                val contentResult = viewModel.getContentById(contentId)
                contentResult.onSuccess { uploadedContent ->
                    // Trigger PDF analysis in background (async, don't block)
                    if (uploadedContent.fileUrl.isNotBlank()) {
                        analyzePDFInBackground(contentId, uploadedContent.fileUrl)
                    }
                }
                
                finish()
            }.onFailure { exception ->
                binding.root.showSnackbar(exception.message ?: getString(R.string.upload_failed))
            }
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let {
                val cut = it.lastIndexOf('/')
                if (cut != -1) {
                    it.substring(cut + 1)
                } else {
                    it
                }
            }
        }
        return result
    }
    
    private fun analyzePDFInBackground(contentId: String, fileUrl: String) {
        lifecycleScope.launch {
            try {
                // Analyze PDF in background (async, don't block upload completion)
                val result = pdfContentAnalyzer.analyzePDF(this@ContentUploadActivity, fileUrl)
                result.onSuccess { sections ->
                    // Save sections to Firestore cache
                    contentSectionsRepository.saveSections(contentId, sections, fileUrl)
                }.onFailure { exception ->
                    // Analysis failed, but don't block upload success
                    // Sections will be analyzed on-demand when needed
                }
            } catch (e: Exception) {
                // Silent failure - analysis can be done later
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

