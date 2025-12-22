package com.gurukulaboard.ncert

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.databinding.ActivityNcertBookDetailsBinding
import com.gurukulaboard.ncert.models.NCERTChapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NCERTBookDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNcertBookDetailsBinding
    private val viewModel: NCERTBookDetailsViewModel by viewModels()
    private lateinit var adapter: NCERTChaptersAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcertBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val receivedBookId = intent.getStringExtra("BOOK_ID")
        if (receivedBookId == null || receivedBookId.isEmpty()) {
            android.widget.Toast.makeText(this, "Book ID not found", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        bookId = receivedBookId
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        viewModel.loadBookDetails(receivedBookId)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Book Details"
    }
    
    private fun setupRecyclerView() {
        adapter = NCERTChaptersAdapter(
            onChapterClick = { chapter ->
                // Show topics/subtopics
            },
            onGenerateMCQClick = { chapter ->
                generateMCQ(chapter)
            },
            onGeneratePPTClick = { chapter ->
                generatePPT(chapter)
            }
        )
        
        binding.recyclerViewChapters.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewChapters.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.book.observe(this) { book ->
            binding.tvBookInfo.text = "${book.subject} - Class ${book.classLevel}"
        }
        
        viewModel.index.observe(this) { index ->
            adapter.submitList(index.chapters)
        }
    }
    
    private fun setupClickListeners() {
        // Buttons are handled in adapter - no action needed here
    }
    
    private var bookId: String? = null
    
    private fun generateMCQ(chapter: NCERTChapter) {
        val currentBookId = bookId ?: viewModel.book.value?.id
        if (currentBookId != null) {
            val intent = Intent(this, MCQGenerationActivity::class.java)
            intent.putExtra("BOOK_ID", currentBookId)
            intent.putExtra("CHAPTER_NAME", chapter.name)
            startActivity(intent)
        }
    }
    
    private fun generatePPT(chapter: NCERTChapter) {
        val currentBookId = bookId ?: viewModel.book.value?.id
        if (currentBookId != null) {
            val intent = Intent(this, PPTGenerationActivity::class.java)
            intent.putExtra("BOOK_ID", currentBookId)
            intent.putExtra("CHAPTER_NAME", chapter.name)
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

