package com.gurukulaboard.content

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.content.models.TeachingContent
import com.gurukulaboard.databinding.ActivityContentDetailBinding
import com.gurukulaboard.models.UserRole
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ContentDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityContentDetailBinding
    private val viewModel: ContentDashboardViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var storageManager: ContentStorageManager
    
    @Inject
    lateinit var pptRepository: ContentPPTRepository
    
    @Inject
    lateinit var contentRepository: ContentRepository
    
    private var content: TeachingContent? = null
    private var contentId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        contentId = intent.getStringExtra("CONTENT_ID")
        if (contentId == null) {
            Toast.makeText(this, "Content ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupObservers()
        setupClickListeners()
        loadContent()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.content_details)
    }
    
    private fun setupObservers() {
        viewModel.contentList.observe(this) { contentList ->
            content = contentList.find { it.id == contentId }
            content?.let { displayContent(it) }
        }
        
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                is LoadingState.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is LoadingState.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                }
                is LoadingState.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                }
                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
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
        binding.btnDownload.setOnClickListener {
            downloadContent()
        }
        
        binding.btnShare.setOnClickListener {
            shareContent()
        }
        
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }
        
        binding.btnGenerateMCQ.setOnClickListener {
            openMCQGeneration()
        }
        
        binding.btnGeneratePPT.setOnClickListener {
            openPPTGeneration()
        }
        
        binding.btnViewMCQ.setOnClickListener {
            openMCQDisplay()
        }
        
        binding.btnViewPPT.setOnClickListener {
            openPPTViewer()
        }
    }
    
    private fun openMCQGeneration() {
        content?.let { content ->
            // Open ContentSelectionActivity first to select sections
            val intent = Intent(this, ContentSelectionActivity::class.java)
            intent.putExtra("CONTENT_ID", content.id)
            intent.putExtra("FILE_URL", content.fileUrl)
            intent.putExtra("SUBJECT", content.subject)
            intent.putExtra("CLASS_LEVEL", content.classLevel)
            intent.putExtra("CHAPTER", content.chapter)
            intent.putExtra(ContentSelectionActivity.EXTRA_MODE, ContentSelectionActivity.MODE_MCQ)
            startActivity(intent)
        }
    }
    
    private fun openPPTGeneration() {
        content?.let { content ->
            // Open ContentSelectionActivity first to select sections
            val intent = Intent(this, ContentSelectionActivity::class.java)
            intent.putExtra("CONTENT_ID", content.id)
            intent.putExtra("FILE_URL", content.fileUrl)
            intent.putExtra("SUBJECT", content.subject)
            intent.putExtra("CLASS_LEVEL", content.classLevel)
            intent.putExtra("CHAPTER", content.chapter)
            startActivity(intent)
        }
    }
    
    private fun openMCQDisplay() {
        content?.let { content ->
            val intent = Intent(this, MCQDisplayActivity::class.java)
            intent.putExtra("CONTENT_ID", content.id)
            startActivity(intent)
        }
    }
    
    private fun openPPTViewer() {
        // Check if PPT exists for this content
        lifecycleScope.launch {
            content?.let { content ->
                val result = pptRepository.getPPTsByContent(content.id)
                result.onSuccess { ppts ->
                    if (ppts.isNotEmpty()) {
                        val ppt = ppts.first() // Get the latest PPT
                        val intent = Intent(this@ContentDetailActivity, PPTViewerActivity::class.java)
                        intent.putExtra("PPT_ID", ppt.id)
                        startActivity(intent)
                    } else {
                        binding.root.showSnackbar("No PPT found for this content")
                    }
                }.onFailure { exception ->
                    binding.root.showSnackbar(exception.message ?: "Failed to load PPT")
                }
            }
        }
    }
    
    private fun loadContent() {
        lifecycleScope.launch {
            contentId?.let { id ->
                val result = contentRepository.getContentById(id)
                result.onSuccess { content ->
                    this@ContentDetailActivity.content = content
                    displayContent(content)
                    checkMCQsExist(id)
                    checkPPTExists(id)
                }.onFailure { exception ->
                    binding.root.showSnackbar(exception.message ?: "Failed to load content")
                }
            }
        }
    }
    
    private fun displayContent(content: TeachingContent) {
        binding.tvTitle.text = content.title
        binding.tvDescription.text = content.description ?: "No description"
        binding.tvSubject.text = content.subject
        binding.tvClass.text = "Class ${content.classLevel}"
        binding.tvChapter.text = content.chapter ?: "General"
        binding.tvContentType.text = content.contentType.name
        binding.tvTags.text = content.tags.joinToString(", ")
        binding.tvUploadedBy.text = "Uploaded by: ${content.uploadedByName}"
        binding.tvDownloadCount.text = "${content.downloadCount} downloads"
        
        content.createdAt?.toDate()?.let { date ->
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(date)
        }
        
        // Set favorite icon
        binding.btnFavorite.setImageResource(
            if (content.isFavorite) android.R.drawable.star_big_on
            else android.R.drawable.star_big_off
        )
        
        // Load PDF in WebView
        loadPDF(content.fileUrl)
        
        // Show delete button if user is owner or admin
        val currentUserId = sessionManager.getUserId()
        val userRole = sessionManager.getUserRole()
        if (content.uploadedBy == currentUserId || userRole == UserRole.ADMIN || userRole == UserRole.SUPER_ADMIN) {
            binding.btnDelete.visibility = android.view.View.VISIBLE
            binding.btnDelete.setOnClickListener {
                showDeleteDialog(content)
            }
        } else {
            binding.btnDelete.visibility = android.view.View.GONE
        }
    }
    
    private fun loadPDF(url: String) {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
    }
    
    private fun downloadContent() {
        content?.let { content ->
            binding.progressBar.visibility = android.view.View.VISIBLE
            lifecycleScope.launch {
                val result = storageManager.downloadPDF(this@ContentDetailActivity, content.fileUrl, content.fileName)
                result.onSuccess { file ->
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@ContentDetailActivity, "Downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    viewModel.downloadContent(content)
                }.onFailure { exception ->
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.root.showSnackbar(exception.message ?: "Failed to download")
                }
            }
        }
    }
    
    private fun shareContent() {
        content?.let { content ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, content.title)
                putExtra(Intent.EXTRA_TEXT, "${content.title}\n\n${content.fileUrl}")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Content"))
        }
    }
    
    private fun toggleFavorite() {
        content?.let { content ->
            val updatedContent = content.copy(isFavorite = !content.isFavorite)
            lifecycleScope.launch {
                viewModel.updateContent(updatedContent)
            }
        }
    }
    
    private fun showDeleteDialog(content: TeachingContent) {
        AlertDialog.Builder(this)
            .setTitle("Delete Content")
            .setMessage("Are you sure you want to delete this content?")
            .setPositiveButton("Delete") { _, _ ->
                deleteContent(content)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun checkMCQsExist(contentId: String) {
        lifecycleScope.launch {
            val result = contentRepository.getMCQsForContent(contentId)
            result.onSuccess { questions ->
                binding.btnViewMCQ.visibility = if (questions.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun checkPPTExists(contentId: String) {
        lifecycleScope.launch {
            val result = pptRepository.getPPTsByContent(contentId)
            result.onSuccess { ppts ->
                binding.btnViewPPT.visibility = if (ppts.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun deleteContent(content: TeachingContent) {
        lifecycleScope.launch {
            viewModel.deleteContent(content.id, content.fileUrl)
            Toast.makeText(this@ContentDetailActivity, "Content deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_content_detail, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
    }
}

