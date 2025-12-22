package com.gurukulaboard.ncert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.databinding.ItemNcertChapterBinding
import com.gurukulaboard.ncert.models.NCERTChapter

class NCERTChaptersAdapter(
    private val onChapterClick: (NCERTChapter) -> Unit,
    private val onGenerateMCQClick: (NCERTChapter) -> Unit,
    private val onGeneratePPTClick: (NCERTChapter) -> Unit
) : ListAdapter<NCERTChapter, NCERTChaptersAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNcertChapterBinding.inflate(
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
        private val binding: ItemNcertChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(chapter: NCERTChapter) {
            binding.tvChapterName.text = "Chapter ${chapter.number}: ${chapter.name}"
            binding.tvPageRange.text = "Pages: ${chapter.startPage} - ${chapter.endPage}"
            binding.tvTopicsCount.text = "Topics: ${chapter.topics.size}"
            
            binding.root.setOnClickListener {
                onChapterClick(chapter)
            }
            
            binding.btnGenerateMCQ.setOnClickListener {
                onGenerateMCQClick(chapter)
            }
            
            binding.btnGeneratePPT.setOnClickListener {
                onGeneratePPTClick(chapter)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<NCERTChapter>() {
        override fun areItemsTheSame(oldItem: NCERTChapter, newItem: NCERTChapter): Boolean {
            return oldItem.number == newItem.number && oldItem.name == newItem.name
        }
        
        override fun areContentsTheSame(oldItem: NCERTChapter, newItem: NCERTChapter): Boolean {
            return oldItem == newItem
        }
    }
}

