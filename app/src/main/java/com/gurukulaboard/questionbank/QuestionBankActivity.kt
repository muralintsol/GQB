package com.gurukulaboard.questionbank

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.databinding.ActivityQuestionBankBinding
import com.gurukulaboard.models.*
import com.gurukulaboard.questionbank.adapters.QuestionAdapter
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuestionBankActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQuestionBankBinding
    private val viewModel: QuestionBankViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private lateinit var adapter: QuestionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBankBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFilterChips()
        
        viewModel.loadQuestions()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.question_bank)
    }
    
    private fun setupRecyclerView() {
        adapter = QuestionAdapter(
            onItemClick = { question ->
                openQuestionDetail(question)
            },
            onApproveClick = { question ->
                approveQuestion(question)
            },
            onRejectClick = { question ->
                rejectQuestion(question)
            }
        )
        
        binding.recyclerViewQuestions.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewQuestions.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.questions.observe(this) { questions ->
            adapter.submitList(questions)
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
                else -> {}
            }
        }
        
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                binding.root.showSnackbar(it)
            }
        }
    }
    
    private fun setupFilterChips() {
        binding.chipPending.setOnClickListener {
            viewModel.setFilter(status = QuestionStatus.PENDING)
        }
        
        binding.chipApproved.setOnClickListener {
            viewModel.setFilter(status = QuestionStatus.APPROVED)
        }
        
        binding.chipRejected.setOnClickListener {
            viewModel.setFilter(status = QuestionStatus.REJECTED)
        }
        
        binding.chipEasy.setOnClickListener {
            viewModel.setFilter(difficulty = Difficulty.EASY)
        }
        
        binding.chipMedium.setOnClickListener {
            viewModel.setFilter(difficulty = Difficulty.MEDIUM)
        }
        
        binding.chipHard.setOnClickListener {
            viewModel.setFilter(difficulty = Difficulty.HARD)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_question_bank, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query)
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.setSearchQuery(null)
                }
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_clear_filters -> {
                viewModel.clearFilters()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun openQuestionDetail(question: Question) {
        val intent = Intent(this, QuestionDetailActivity::class.java)
        intent.putExtra("QUESTION_ID", question.id)
        startActivity(intent)
    }
    
    private fun approveQuestion(question: Question) {
        val userId = sessionManager.getUserId() ?: return
        viewModel.approveQuestion(question.id, userId)
    }
    
    private fun rejectQuestion(question: Question) {
        viewModel.rejectQuestion(question.id)
    }
}

