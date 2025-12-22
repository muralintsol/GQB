package com.gurukulaboard.paper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.databinding.ItemSavedPaperBinding
import com.gurukulaboard.models.QuestionPaper
import java.text.SimpleDateFormat
import java.util.*

class SavedPapersAdapter(
    private val onItemClick: (QuestionPaper) -> Unit,
    private val onDeleteClick: (QuestionPaper) -> Unit
) : ListAdapter<QuestionPaper, SavedPapersAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedPaperBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(
        private val binding: ItemSavedPaperBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(paper: QuestionPaper) {
            binding.tvTitle.text = paper.title
            binding.tvSubject.text = "Subject: ${paper.subject}"
            binding.tvClass.text = "Class: ${paper.`class`}"
            binding.tvTotalMarks.text = "Total Marks: ${paper.totalMarks}"
            binding.tvQuestionCount.text = "Questions: ${paper.questions.size}"
            
            paper.createdAt?.let {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                binding.tvDate.text = "Created: ${dateFormat.format(it.toDate())}"
            }
            
            binding.root.setOnClickListener {
                onItemClick(paper)
            }
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(paper)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<QuestionPaper>() {
        override fun areItemsTheSame(oldItem: QuestionPaper, newItem: QuestionPaper): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: QuestionPaper, newItem: QuestionPaper): Boolean {
            return oldItem == newItem
        }
    }
}

