package com.gurukulaboard.content

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityPptViewerBinding
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PPTViewerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPptViewerBinding
    private val viewModel: PPTEditorViewModel by viewModels()
    
    @Inject
    lateinit var pptRepository: ContentPPTRepository
    
    private var pptId: String? = null
    private var htmlContent: String? = null
    private var isPreviewMode: Boolean = false
    private var isFullScreen: Boolean = false
    private var currentSlideIndex: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPptViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        pptId = intent.getStringExtra("PPT_ID")
        htmlContent = intent.getStringExtra("HTML_CONTENT")
        isPreviewMode = intent.getBooleanExtra("PREVIEW_MODE", false)
        
        if (htmlContent == null && pptId == null) {
            Toast.makeText(this, "No PPT content provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupWebView()
        setupClickListeners()
        loadPPT()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isPreviewMode) "PPT Preview" else getString(R.string.presentation)
    }
    
    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
    }
    
    private fun setupClickListeners() {
        binding.btnPrevious.setOnClickListener {
            navigateToPreviousSlide()
        }
        
        binding.btnNext.setOnClickListener {
            navigateToNextSlide()
        }
        
        binding.btnFullScreen.setOnClickListener {
            toggleFullScreen()
        }
    }
    
    private fun loadPPT() {
        if (htmlContent != null) {
            displayPPT(htmlContent!!)
        } else if (pptId != null) {
            lifecycleScope.launch {
                val result = pptRepository.getPPTById(pptId!!)
                result.onSuccess { ppt ->
                    displayPPT(ppt.htmlContent)
                }.onFailure { exception ->
                    binding.root.showSnackbar(exception.message ?: "Failed to load PPT")
                }
            }
        }
    }
    
    private fun displayPPT(html: String) {
        binding.webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
    
    private fun navigateToNextSlide() {
        // Use JavaScript to navigate to next slide
        binding.webView.evaluateJavascript(
            """
            if (typeof currentSlide !== 'undefined' && currentSlide < slides.length - 1) {
                currentSlide++;
                showSlide(currentSlide);
            }
            """.trimIndent(), null
        )
    }
    
    private fun navigateToPreviousSlide() {
        // Use JavaScript to navigate to previous slide
        binding.webView.evaluateJavascript(
            """
            if (typeof currentSlide !== 'undefined' && currentSlide > 0) {
                currentSlide--;
                showSlide(currentSlide);
            }
            """.trimIndent(), null
        )
    }
    
    private fun toggleFullScreen() {
        if (isFullScreen) {
            // Exit full screen
            supportActionBar?.show()
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            binding.toolbar.visibility = View.VISIBLE
            binding.layoutControls.visibility = View.VISIBLE
            binding.btnFullScreen.text = getString(R.string.full_screen)
            isFullScreen = false
        } else {
            // Enter full screen
            supportActionBar?.hide()
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            binding.toolbar.visibility = View.GONE
            binding.layoutControls.visibility = View.GONE
            binding.btnFullScreen.text = getString(R.string.exit_full_screen)
            isFullScreen = true
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

