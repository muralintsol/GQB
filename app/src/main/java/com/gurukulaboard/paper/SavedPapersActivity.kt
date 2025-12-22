package com.gurukulaboard.paper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivitySavedPapersBinding
import com.gurukulaboard.models.QuestionPaper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedPapersActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySavedPapersBinding
    private val viewModel: SavedPapersViewModel by viewModels()
    private lateinit var adapter: SavedPapersAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedPapersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        
        viewModel.loadPapers()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Saved Papers"
    }
    
    private fun setupRecyclerView() {
        adapter = SavedPapersAdapter(
            onItemClick = { paper ->
                viewPaper(paper)
            },
            onDeleteClick = { paper ->
                deletePaper(paper)
            }
        )
        
        binding.recyclerViewPapers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPapers.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.papers.observe(this) { papers ->
            adapter.submitList(papers)
            binding.emptyView.visibility = if (papers.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        
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
    }
    
    private fun viewPaper(paper: QuestionPaper) {
        // Load questions and open preview
        lifecycleScope.launch {
            val intent = Intent(this@SavedPapersActivity, PaperPreviewActivity::class.java)
            intent.putExtra("EXAM_TYPE", paper.examType)
            intent.putExtra("SUBJECT", paper.subject)
            intent.putExtra("CLASS_LEVEL", paper.`class`)
            intent.putExtra("TOTAL_MARKS", paper.totalMarks)
            // Note: We'd need to load questions by IDs here
            startActivity(intent)
        }
    }
    
    private fun deletePaper(paper: QuestionPaper) {
        AlertDialog.Builder(this)
            .setTitle("Delete Paper")
            .setMessage("Are you sure you want to delete '${paper.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePaper(paper.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

