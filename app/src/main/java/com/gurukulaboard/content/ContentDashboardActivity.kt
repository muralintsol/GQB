package com.gurukulaboard.content

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.content.adapters.ContentCategoryAdapter
import com.gurukulaboard.content.adapters.ContentListAdapter
import com.gurukulaboard.content.adapters.FilterChip
import com.gurukulaboard.content.models.ContentType
import com.gurukulaboard.content.models.TeachingContent
import com.gurukulaboard.databinding.ActivityContentDashboardBinding
import com.gurukulaboard.models.UserRole
import com.gurukulaboard.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContentDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityContentDashboardBinding
    private val viewModel: ContentDashboardViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private lateinit var contentAdapter: ContentListAdapter
    private lateinit var filterAdapter: ContentCategoryAdapter
    private var isGridView = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFilterChips()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.teacher_content)
    }
    
    private fun setupRecyclerView() {
        contentAdapter = ContentListAdapter(
            onItemClick = { content ->
                openContentDetail(content)
            },
            onFavoriteClick = { content ->
                toggleFavorite(content)
            }
        )
        
        binding.recyclerViewContent.layoutManager = if (isGridView) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        binding.recyclerViewContent.adapter = contentAdapter
        
        filterAdapter = ContentCategoryAdapter { filter ->
            handleFilterClick(filter)
        }
        binding.recyclerViewFilters.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewFilters.adapter = filterAdapter
    }
    
    private fun setupFilterChips() {
        val filters = mutableListOf<FilterChip>()
        
        // Add content type filters
        ContentType.values().forEach { type ->
            filters.add(FilterChip.TypeFilter(type.name, type))
        }
        
        // Add clear filter
        filters.add(FilterChip.ClearFilter())
        
        filterAdapter.setFilters(filters)
    }
    
    private fun setupObservers() {
        viewModel.filteredContent.observe(this) { content ->
            contentAdapter.submitList(content)
            binding.tvEmptyState.visibility = if (content.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.loadingState.observe(this) { state ->
            when (state) {
                is LoadingState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is LoadingState.Success -> {
                    binding.progressBar.visibility = View.GONE
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
        binding.fabUpload.setOnClickListener {
            startActivity(Intent(this, ContentUploadActivity::class.java))
        }
    }
    
    private fun handleFilterClick(filter: FilterChip) {
        when (filter) {
            is FilterChip.TypeFilter -> {
                viewModel.filterByType(filter.contentType)
                // Update selected state
                val updatedFilters = filterAdapter.filters.map { f ->
                    if (f == filter) {
                        when (f) {
                            is FilterChip.TypeFilter -> FilterChip.TypeFilter(f.label, f.contentType).apply { isSelected = !f.isSelected }
                            else -> f
                        }
                    } else {
                        when (f) {
                            is FilterChip.TypeFilter -> FilterChip.TypeFilter(f.label, f.contentType).apply { isSelected = false }
                            else -> f
                        }
                    }
                }
                filterAdapter.setFilters(updatedFilters)
            }
            is FilterChip.ClearFilter -> {
                viewModel.clearFilters()
                val updatedFilters = filterAdapter.filters.map { f ->
                    when (f) {
                        is FilterChip.TypeFilter -> FilterChip.TypeFilter(f.label, f.contentType).apply { isSelected = false }
                        else -> f
                    }
                }
                filterAdapter.setFilters(updatedFilters)
            }
            else -> {}
        }
    }
    
    private fun openContentDetail(content: TeachingContent) {
        val intent = Intent(this, ContentDetailActivity::class.java)
        intent.putExtra("CONTENT_ID", content.id)
        startActivity(intent)
    }
    
    private fun toggleFavorite(content: TeachingContent) {
        val updatedContent = content.copy(isFavorite = !content.isFavorite)
        lifecycleScope.launch {
            viewModel.updateContent(updatedContent)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_content_dashboard, menu)
        
        val searchItem = menu?.findItem(R.id.menu_search)
        val searchView = searchItem?.actionView as? SearchView
        
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchContent(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.clearFilters()
                } else {
                    viewModel.searchContent(newText)
                }
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_view_toggle -> {
                toggleView()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun toggleView() {
        isGridView = !isGridView
        binding.recyclerViewContent.layoutManager = if (isGridView) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
    }
}

