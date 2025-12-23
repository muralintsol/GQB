package com.gurukulaboard.content

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.content.adapters.SlideListAdapter
import com.gurukulaboard.content.models.SlideData
import com.gurukulaboard.content.models.SlideType
import com.gurukulaboard.content.models.TeacherPPT
import com.gurukulaboard.databinding.ActivityPptEditorBinding
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PPTEditorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPptEditorBinding
    private val viewModel: PPTEditorViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var contentSlideGenerator: ContentSlideGenerator
    
    private lateinit var slideAdapter: SlideListAdapter
    private var contentId: String? = null
    private var fileUrl: String? = null
    private var isNewPPT: Boolean = true
    private var selectedSections: List<com.gurukulaboard.content.models.ContentSection>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPptEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        contentId = intent.getStringExtra("CONTENT_ID")
        fileUrl = intent.getStringExtra("FILE_URL")
        val pptId = intent.getStringExtra("PPT_ID")
        
        // Get selected sections if provided
        val sectionsList = intent.getParcelableArrayListExtra<com.gurukulaboard.content.models.ContentSection>("SELECTED_SECTIONS")
        selectedSections = sectionsList?.toList()
        
        if (contentId == null || fileUrl == null) {
            Toast.makeText(this, "Invalid content", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupRecyclerView()
        setupSpinners()
        setupObservers()
        setupClickListeners()
        
        if (pptId != null) {
            // Load existing PPT
            isNewPPT = false
            viewModel.loadPPT(pptId)
        } else {
            // Generate new PPT (with or without selected sections)
            generateInitialPPT()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_ppt)
    }
    
    private fun setupRecyclerView() {
        slideAdapter = SlideListAdapter(
            onSlideClick = { index ->
                viewModel.selectSlide(index)
            },
            onSlideDelete = { index ->
                viewModel.removeSlide(index)
            }
        )
        
        binding.recyclerViewSlides.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSlides.adapter = slideAdapter
        
        // Enable drag and drop for reordering
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                viewModel.reorderSlides(fromPosition, toPosition)
                slideAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Handled by adapter
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewSlides)
    }
    
    private fun setupSpinners() {
        val slideTypes = SlideType.values().map { it.name }
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, slideTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSlideType.adapter = typeAdapter
    }
    
    private fun setupObservers() {
        viewModel.slides.observe(this) { slides ->
            slideAdapter.submitList(slides)
            if (slides.isNotEmpty() && viewModel.selectedSlideIndex.value == -1) {
                viewModel.selectSlide(0)
            }
        }
        
        viewModel.selectedSlideIndex.observe(this) { index ->
            if (index >= 0 && index < (viewModel.slides.value?.size ?: 0)) {
                val slide = viewModel.slides.value?.get(index)
                slide?.let { displaySlideForEditing(it) }
            }
        }
        
        viewModel.currentPPT.observe(this) { ppt ->
            ppt?.let {
                binding.etTitle.setText(it.title)
            }
        }
        
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                is LoadingState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is LoadingState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                }
                is LoadingState.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
        
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                binding.root.showSnackbar(it)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnAddSlide.setOnClickListener {
            val slideType = SlideType.values()[binding.spinnerSlideType.selectedItemPosition]
            val insertPosition = viewModel.selectedSlideIndex.value?.let { it + 1 } ?: -1
            viewModel.addSlide(slideType, insertPosition)
        }
        
        binding.btnSaveSlide.setOnClickListener {
            saveCurrentSlide()
        }
        
        binding.btnPreview.setOnClickListener {
            previewPPT()
        }
    }
    
    private fun generateInitialPPT() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val metadata = ContentSlideGenerator.ContentMetadata(
                    title = selectedSections?.let { 
                        if (it.size == 1) it[0].title else "Selected Sections"
                    } ?: "Generated PPT",
                    subject = intent.getStringExtra("SUBJECT") ?: "",
                    classLevel = intent.getIntExtra("CLASS_LEVEL", 11),
                    chapter = intent.getStringExtra("CHAPTER")
                )
                
                val userId = sessionManager.getUserId() ?: return@launch
                
                val result = if (selectedSections != null && selectedSections!!.isNotEmpty()) {
                    // Generate from selected sections
                    contentSlideGenerator.generateSlidesFromSections(
                        context = this@PPTEditorActivity,
                        fileUrl = fileUrl!!,
                        sections = selectedSections!!,
                        metadata = metadata,
                        contentId = contentId!!,
                        createdBy = userId
                    )
                } else {
                    // Generate from entire PDF
                    contentSlideGenerator.generateSlidesFromContent(
                        context = this@PPTEditorActivity,
                        fileUrl = fileUrl!!,
                        metadata = metadata,
                        contentId = contentId!!,
                        createdBy = userId
                    )
                }
                
                result.onSuccess { ppt ->
                    viewModel.initializeWithPPT(ppt)
                    binding.progressBar.visibility = View.GONE
                }.onFailure { exception ->
                    binding.progressBar.visibility = View.GONE
                    binding.root.showSnackbar(exception.message ?: "Failed to generate PPT")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.root.showSnackbar("Error: ${e.message}")
            }
        }
    }
    
    private fun displaySlideForEditing(slide: SlideData) {
        binding.etSlideTitle.setText(slide.title)
        binding.etSlideContent.setText(slide.content)
        binding.spinnerSlideType.setSelection(SlideType.values().indexOf(slide.slideType))
    }
    
    private fun saveCurrentSlide() {
        val index = viewModel.selectedSlideIndex.value ?: return
        val title = binding.etSlideTitle.text.toString().trim()
        val content = binding.etSlideContent.text.toString().trim()
        val slideType = SlideType.values()[binding.spinnerSlideType.selectedItemPosition]
        
        viewModel.updateSlide(index, title, content, slideType)
        Toast.makeText(this, "Slide updated", Toast.LENGTH_SHORT).show()
    }
    
    private fun previewPPT() {
        // Save current slide first
        saveCurrentSlide()
        
        // Generate HTML and show preview
        lifecycleScope.launch {
            val ppt = viewModel.currentPPT.value ?: return@launch
            val slides = viewModel.slides.value ?: return@launch
            
            // Regenerate HTML
            val html = viewModel.generateHTMLFromSlides(ppt.title, slides, ppt.subject, ppt.chapter ?: "")
            
            val intent = Intent(this@PPTEditorActivity, PPTViewerActivity::class.java)
            intent.putExtra("HTML_CONTENT", html)
            intent.putExtra("PREVIEW_MODE", true)
            startActivity(intent)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_ppt_editor, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save_draft -> {
                savePPT(isDraft = true)
                true
            }
            R.id.menu_save_final -> {
                savePPT(isDraft = false)
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun savePPT(isDraft: Boolean) {
        saveCurrentSlide()
        
        val title = binding.etTitle.text.toString().trim()
        if (title.isBlank()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val currentPPT = viewModel.currentPPT.value
            val slides = viewModel.slides.value ?: return@launch
            
            val userId = sessionManager.getUserId() ?: return@launch
            
            val metadata = ContentSlideGenerator.ContentMetadata(
                title = title,
                subject = intent.getStringExtra("SUBJECT") ?: "",
                classLevel = intent.getIntExtra("CLASS_LEVEL", 11),
                chapter = intent.getStringExtra("CHAPTER")
            )
            
            val ppt = currentPPT?.copy(
                title = title,
                slides = slides,
                isDraft = isDraft
            ) ?: TeacherPPT(
                title = title,
                contentId = contentId!!,
                htmlContent = "",
                slides = slides,
                subject = metadata.subject,
                classLevel = metadata.classLevel,
                chapter = metadata.chapter,
                createdBy = userId
            )
            
            viewModel.savePPT(isDraft)
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
    }
}

