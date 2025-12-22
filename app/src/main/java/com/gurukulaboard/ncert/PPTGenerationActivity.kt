package com.gurukulaboard.ncert

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gurukulaboard.databinding.ActivityPptGenerationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class PPTGenerationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPptGenerationBinding
    private val viewModel: PPTGenerationViewModel by viewModels()
    
    private var bookId: String? = null
    private var chapterName: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPptGenerationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        bookId = intent.getStringExtra("BOOK_ID")
        chapterName = intent.getStringExtra("CHAPTER_NAME")
        
        setupToolbar()
        setupObservers()
        setupClickListeners()
        
        if (bookId != null && chapterName != null) {
            viewModel.loadContent(bookId!!, chapterName!!)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Generate PPT"
    }
    
    private fun setupObservers() {
        viewModel.generatedHTML.observe(this) { html ->
            if (html != null) {
                displayPreview(html)
            }
        }
        
        viewModel.generatingState.observe(this) { state ->
            when (state) {
                is GeneratingState.Generating -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnGenerate.isEnabled = false
                }
                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnGenerate.isEnabled = true
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnGenerate.setOnClickListener {
            viewModel.generateSlides()
        }
        
        binding.btnExport.setOnClickListener {
            exportHTML()
        }
        
        binding.btnShare.setOnClickListener {
            shareHTML()
        }
    }
    
    private fun displayPreview(html: String) {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
    
    private fun exportHTML() {
        lifecycleScope.launch {
            val html = viewModel.generatedHTML.value ?: return@launch
            
            val fileName = "NCERT_Slides_${System.currentTimeMillis()}.html"
            val file = File(getExternalFilesDir(null), fileName)
            file.writeText(html)
            
            Toast.makeText(this@PPTGenerationActivity, "Exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            
            // Open file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(file), "text/html")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@PPTGenerationActivity, "No HTML viewer found", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun shareHTML() {
        val html = viewModel.generatedHTML.value ?: return
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_TEXT, html)
            putExtra(Intent.EXTRA_SUBJECT, "NCERT Slides - ${chapterName ?: "Chapter"}")
        }
        
        startActivity(Intent.createChooser(intent, "Share HTML Slides"))
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

