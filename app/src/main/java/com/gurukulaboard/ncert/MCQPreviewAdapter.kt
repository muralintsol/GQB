package com.gurukulaboard.ncert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.databinding.ItemMcqPreviewBinding

class MCQPreviewAdapter(
    private val mcqs: List<NCERTMCQGenerator.GeneratedMCQ>
) : ListAdapter<NCERTMCQGenerator.GeneratedMCQ, MCQPreviewAdapter.ViewHolder>(DiffCallback()) {
    
    init {
        submitList(mcqs)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMcqPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
    
    inner class ViewHolder(
        private val binding: ItemMcqPreviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mcq: NCERTMCQGenerator.GeneratedMCQ, questionNumber: Int) {
            binding.tvQuestionNumber.text = "Q$questionNumber"
            binding.tvQuestion.text = mcq.question
            
            val optionsText = mcq.options.mapIndexed { index, option ->
                "${('a' + index)}. $option"
            }.joinToString("\n")
            
            binding.tvOptions.text = optionsText
            binding.tvCorrectAnswer.text = "Correct Answer: ${('a' + mcq.correctAnswer)}"
            
            mcq.explanation?.let {
                binding.tvExplanation.text = "Explanation: $it"
                binding.tvExplanation.visibility = android.view.View.VISIBLE
            } ?: run {
                binding.tvExplanation.visibility = android.view.View.GONE
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<NCERTMCQGenerator.GeneratedMCQ>() {
        override fun areItemsTheSame(oldItem: NCERTMCQGenerator.GeneratedMCQ, newItem: NCERTMCQGenerator.GeneratedMCQ): Boolean {
            return oldItem.question == newItem.question
        }
        
        override fun areContentsTheSame(oldItem: NCERTMCQGenerator.GeneratedMCQ, newItem: NCERTMCQGenerator.GeneratedMCQ): Boolean {
            return oldItem == newItem
        }
    }
}

