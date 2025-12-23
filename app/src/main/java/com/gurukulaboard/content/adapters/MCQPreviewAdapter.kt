package com.gurukulaboard.content.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.R
import com.gurukulaboard.content.ContentMCQGenerator.GeneratedMCQ

class MCQPreviewAdapter(
    private val onItemClick: (GeneratedMCQ) -> Unit
) : ListAdapter<GeneratedMCQ, MCQPreviewAdapter.MCQViewHolder>(MCQDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MCQViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mcq_preview, parent, false)
        return MCQViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MCQViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class MCQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionView: TextView = itemView.findViewById(R.id.tvQuestion)
        private val optionsView: TextView = itemView.findViewById(R.id.tvOptions)
        private val answerView: TextView = itemView.findViewById(R.id.tvAnswer)
        
        fun bind(mcq: GeneratedMCQ) {
            questionView.text = mcq.question
            
            val optionsText = mcq.options.mapIndexed { index, option ->
                "${('a' + index)}. $option"
            }.joinToString("\n")
            optionsView.text = optionsText
            
            answerView.text = "Answer: ${('a' + mcq.correctAnswer)}. ${mcq.options[mcq.correctAnswer]}"
            answerView.visibility = View.GONE // Hidden by default, show on click
            
            itemView.setOnClickListener {
                answerView.visibility = if (answerView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                onItemClick(mcq)
            }
        }
    }
    
    class MCQDiffCallback : DiffUtil.ItemCallback<GeneratedMCQ>() {
        override fun areItemsTheSame(oldItem: GeneratedMCQ, newItem: GeneratedMCQ): Boolean {
            return oldItem.question == newItem.question
        }
        
        override fun areContentsTheSame(oldItem: GeneratedMCQ, newItem: GeneratedMCQ): Boolean {
            return oldItem == newItem
        }
    }
}

