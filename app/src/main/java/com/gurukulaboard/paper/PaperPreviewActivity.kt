package com.gurukulaboard.paper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gurukulaboard.R
import com.gurukulaboard.auth.SessionManager
import com.gurukulaboard.databinding.ActivityPaperPreviewBinding
import com.gurukulaboard.export.AnswerKeyGenerator
import com.gurukulaboard.export.PDFExporter
import com.gurukulaboard.export.PrintManagerHelper
import com.gurukulaboard.models.*
import com.gurukulaboard.paper.templates.FormattedPaper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PaperPreviewActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPaperPreviewBinding
    private val viewModel: PaperGeneratorViewModel by viewModels()
    
    @Inject
    lateinit var pdfExporter: PDFExporter
    
    @Inject
    lateinit var answerKeyGenerator: AnswerKeyGenerator
    
    @Inject
    lateinit var printManager: PrintManagerHelper
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var paperRepository: PaperRepository
    
    private var currentQuestions: List<Question> = emptyList()
    private var currentExamType: ExamType = ExamType.PU_BOARD
    private var currentSubject: String = ""
    private var currentClassLevel: Int = 11
    private var currentTotalMarks: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaperPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadQuestions()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Paper Preview"
    }
    
    private fun setupClickListeners() {
        binding.btnExportPdf.setOnClickListener {
            exportToPDF()
        }
        
        binding.btnExportAnswerKey.setOnClickListener {
            exportAnswerKey()
        }
        
        binding.btnPrint.setOnClickListener {
            printPaper()
        }
        
        binding.btnShare.setOnClickListener {
            sharePaper()
        }
        
        binding.btnSave.setOnClickListener {
            savePaper()
        }
    }
    
    private fun loadQuestions() {
        currentExamType = intent.getSerializableExtra("EXAM_TYPE") as? ExamType ?: ExamType.PU_BOARD
        currentSubject = intent.getStringExtra("SUBJECT") ?: ""
        currentClassLevel = intent.getIntExtra("CLASS_LEVEL", 11)
        currentTotalMarks = intent.getIntExtra("TOTAL_MARKS", 100)
        
        // Display preview - questions are already loaded in ViewModel
        viewModel.generatedPaper.observe(this) { questions ->
            currentQuestions = questions
            displayPreview(questions)
        }
        
        viewModel.formattedPaper.observe(this) { formattedPaper ->
            formattedPaper?.let {
                displayFormattedPreview(it)
            }
        }
    }
    
    private fun displayPreview(questions: List<Question>) {
        val previewText = questions.mapIndexed { index, question ->
            buildString {
                append("${index + 1}. ${question.content}\n")
                question.options?.forEachIndexed { optIndex, option ->
                    append("   ${('a' + optIndex)}. $option\n")
                }
                if (question.answer != null) {
                    append("   Answer: ${question.answer}\n")
                }
                append("\n")
            }
        }.joinToString("\n")
        
        binding.tvPreview.text = previewText
    }
    
    private fun displayFormattedPreview(formattedPaper: FormattedPaper) {
        val previewText = buildString {
            append("TOTAL MARKS: ${formattedPaper.totalMarks}\n\n")
            
            if (formattedPaper.sectionA.isNotEmpty()) {
                append("SECTION A\n")
                append("=".repeat(50) + "\n")
                formattedPaper.sectionA.forEachIndexed { index, question ->
                    append("${index + 1}. ${question.content}\n")
                    question.options?.forEachIndexed { optIndex, option ->
                        append("   ${('a' + optIndex)}. $option\n")
                    }
                    append("\n")
                }
            }
            
            if (formattedPaper.sectionB.isNotEmpty()) {
                append("\nSECTION B\n")
                append("=".repeat(50) + "\n")
                formattedPaper.sectionB.forEachIndexed { index, question ->
                    append("${index + 1}. ${question.content}\n")
                    question.options?.forEachIndexed { optIndex, option ->
                        append("   ${('a' + optIndex)}. $option\n")
                    }
                    append("\n")
                }
            }
            
            if (formattedPaper.sectionC.isNotEmpty()) {
                append("\nSECTION C\n")
                append("=".repeat(50) + "\n")
                formattedPaper.sectionC.forEachIndexed { index, question ->
                    append("${index + 1}. ${question.content}\n")
                    question.options?.forEachIndexed { optIndex, option ->
                        append("   ${('a' + optIndex)}. $option\n")
                    }
                    append("\n")
                }
            }
        }
        
        binding.tvPreview.text = previewText
    }
    
    private fun exportToPDF() {
        if (currentQuestions.isEmpty()) {
            Toast.makeText(this, "No questions to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            
            val fileName = "QuestionPaper_${System.currentTimeMillis()}.pdf"
            val headerFooter = HeaderFooterConfig(
                schoolName = "Gurukula Board",
                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                subject = currentSubject,
                classLevel = "Class $currentClassLevel",
                examType = currentExamType.name
            )
            
            val result = pdfExporter.exportQuestionPaper(
                this@PaperPreviewActivity,
                currentQuestions,
                fileName,
                headerFooter
            )
            
            binding.progressBar.visibility = android.view.View.GONE
            
            result.onSuccess { file ->
                Toast.makeText(this@PaperPreviewActivity, "PDF exported: ${file.name}", Toast.LENGTH_LONG).show()
                // Open the PDF
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@PaperPreviewActivity, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { exception ->
                Toast.makeText(this@PaperPreviewActivity, "Export failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun exportAnswerKey() {
        if (currentQuestions.isEmpty()) {
            Toast.makeText(this, "No questions to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            
            val fileName = "AnswerKey_${System.currentTimeMillis()}.pdf"
            val result = answerKeyGenerator.generateAnswerKey(
                this@PaperPreviewActivity,
                currentQuestions,
                fileName
            )
            
            binding.progressBar.visibility = android.view.View.GONE
            
            result.onSuccess { file ->
                Toast.makeText(this@PaperPreviewActivity, "Answer key exported: ${file.name}", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(file), "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@PaperPreviewActivity, "No PDF viewer found", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { exception ->
                Toast.makeText(this@PaperPreviewActivity, "Export failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun printPaper() {
        if (currentQuestions.isEmpty()) {
            Toast.makeText(this, "No questions to print", Toast.LENGTH_SHORT).show()
            return
        }
        
        val htmlContent = buildString {
            append("<html><body>")
            append("<h2>Question Paper</h2>")
            append("<p><strong>Subject:</strong> $currentSubject</p>")
            append("<p><strong>Class:</strong> $currentClassLevel</p>")
            append("<p><strong>Total Marks:</strong> $currentTotalMarks</p>")
            append("<hr>")
            currentQuestions.forEachIndexed { index, question ->
                append("<p><strong>${index + 1}. ${question.content}</strong></p>")
                question.options?.forEachIndexed { optIndex, option ->
                    append("<p>&nbsp;&nbsp;${('a' + optIndex)}. $option</p>")
                }
                append("<br>")
            }
            append("</body></html>")
        }
        
        printManager.printDocument(this, htmlContent, "Question Paper")
        Toast.makeText(this, "Print dialog opened", Toast.LENGTH_SHORT).show()
    }
    
    private fun sharePaper() {
        if (currentQuestions.isEmpty()) {
            Toast.makeText(this, "No questions to share", Toast.LENGTH_SHORT).show()
            return
        }
        
        val shareText = buildString {
            append("Question Paper\n")
            append("Subject: $currentSubject\n")
            append("Class: $currentClassLevel\n")
            append("Total Marks: $currentTotalMarks\n\n")
            currentQuestions.forEachIndexed { index, question ->
                append("${index + 1}. ${question.content}\n")
                question.options?.forEachIndexed { optIndex, option ->
                    append("   ${('a' + optIndex)}. $option\n")
                }
                append("\n")
            }
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Question Paper - $currentSubject")
        }
        
        startActivity(Intent.createChooser(intent, "Share Question Paper"))
    }
    
    private fun savePaper() {
        if (currentQuestions.isEmpty()) {
            Toast.makeText(this, "No questions to save", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userId = sessionManager.getUserId() ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        val editText = android.widget.EditText(this).apply {
            hint = "Paper Title"
            setText("${currentSubject} - Class $currentClassLevel - ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())}")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Save Paper")
            .setMessage("Enter a title for this paper:")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val title = editText.text.toString().takeIf { it.isNotBlank() }
                    ?: "${currentSubject} - Class $currentClassLevel"
                
                lifecycleScope.launch {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    
                    val questionIds = currentQuestions.map { it.id }
                    val difficultyDistribution = currentQuestions.groupBy { it.difficulty }
                        .mapValues { it.value.size }
                        .mapKeys { it.key }
                    
                    val paper = QuestionPaper(
                        title = title,
                        examType = currentExamType,
                        subject = currentSubject,
                        `class` = currentClassLevel,
                        questions = questionIds,
                        difficultyDistribution = difficultyDistribution,
                        totalMarks = currentTotalMarks,
                        createdBy = userId,
                        headerFooter = HeaderFooterConfig(
                            schoolName = "Gurukula Board",
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            subject = currentSubject,
                            classLevel = "Class $currentClassLevel",
                            examType = currentExamType.name
                        )
                    )
                    
                    val result = paperRepository.savePaper(paper)
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    result.onSuccess {
                        Toast.makeText(this@PaperPreviewActivity, "Paper saved successfully", Toast.LENGTH_SHORT).show()
                    }.onFailure { exception ->
                        Toast.makeText(this@PaperPreviewActivity, "Failed to save: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_paper_preview, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_export_pdf -> {
                exportToPDF()
                true
            }
            R.id.menu_export_answer_key -> {
                exportAnswerKey()
                true
            }
            R.id.menu_print -> {
                printPaper()
                true
            }
            R.id.menu_share -> {
                sharePaper()
                true
            }
            R.id.menu_save -> {
                savePaper()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

