package com.gurukulaboard.ncert

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gurukulaboard.R
import com.gurukulaboard.databinding.ActivityNcertManagementBinding
import com.gurukulaboard.ncert.models.NCERTBook
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class NCERTManagementActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNcertManagementBinding
    private val viewModel: NCERTManagementViewModel by viewModels()
    private lateinit var adapter: NCERTBooksAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcertManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        viewModel.loadBooks()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "NCERT Books"
    }
    
    private fun setupRecyclerView() {
        adapter = NCERTBooksAdapter(
            onItemClick = { book ->
                viewBookDetails(book)
            },
            onProcessClick = { book ->
                processBook(book)
            }
        )
        
        binding.recyclerViewBooks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBooks.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.books.observe(this) { books ->
            adapter.submitList(books)
            binding.emptyView.visibility = if (books.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.processingState.observe(this) { state ->
            when (state) {
                is ProcessingState.Processing -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.btnScanZipFiles.isEnabled = false
                }
                else -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnScanZipFiles.isEnabled = true
                }
            }
        }
        
        viewModel.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // User selected a directory - process files from there
            val selectedPath = it.path
            Toast.makeText(this, "Selected: $selectedPath\nNote: Direct folder access may be limited. Use 'Scan' button for app storage.", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnScanZipFiles.setOnClickListener {
            scanAndProcessZipFiles()
        }
    }
    
    private fun scanAndProcessZipFiles() {
        AlertDialog.Builder(this)
            .setTitle("Process NCERT Books")
            .setMessage("This will scan the NCERT BOOKS folder and process all zip files. This may take several minutes.\n\nNote: Zip files should be accessible from app storage or external storage.")
            .setPositiveButton("Start") { _, _ ->
                // For Android, we'll use the app's external files directory
                // User should copy zip files there or use file picker
                val ncertBooksDir = File(getExternalFilesDir(null), "NCERT BOOKS")
                ncertBooksDir.mkdirs()
                
                // Check if directory has zip files
                val zipFiles = ncertBooksDir.listFiles { _, name -> name.endsWith(".zip", ignoreCase = true) }
                
                if (zipFiles != null && zipFiles.isNotEmpty()) {
                    viewModel.processZipFiles(ncertBooksDir)
                } else {
                    // Try alternative: check if user wants to select folder
                    Toast.makeText(this, "No zip files found in ${ncertBooksDir.absolutePath}. Please copy zip files there first, or use file picker.", Toast.LENGTH_LONG).show()
                    // Could add file picker here in future
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun viewBookDetails(book: NCERTBook) {
        val intent = Intent(this, NCERTBookDetailsActivity::class.java)
        intent.putExtra("BOOK_ID", book.id)
        startActivity(intent)
    }
    
    private fun processBook(book: NCERTBook) {
        viewModel.processBook(book.id)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

