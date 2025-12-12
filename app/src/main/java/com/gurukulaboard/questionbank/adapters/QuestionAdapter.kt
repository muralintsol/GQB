package com.gurukulaboard.questionbank.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.databinding.ItemQuestionBinding
import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionStatus

class QuestionAdapter(
    private val onItemClick: (Question) -> Unit,
    private val onApproveClick: (Question) -> Unit,
    private val onRejectClick: (Question) -> Unit
) : ListAdapter<Question, QuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class QuestionViewHolder(
        private val binding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(question: Question) {
            binding.apply {
                tvQuestionContent.text = question.content
                tvSubject.text = question.subject
                tvClass.text = "Class ${question.`class`}"
                tvChapter.text = question.chapter
                tvDifficulty.text = question.difficulty.name
                tvType.text = question.type.name
                tvStatus.text = question.status.name
                
                // Status indicator color
                when (question.status) {
                    QuestionStatus.APPROVED -> {
                        tvStatus.setTextColor(
                            binding.root.context.getColor(android.R.color.holo_green_dark)
                        )
                    }
                    QuestionStatus.REJECTED -> {
                        tvStatus.setTextColor(
                            binding.root.context.getColor(android.R.color.holo_red_dark)
                        )
                    }
                    QuestionStatus.PENDING -> {
                        tvStatus.setTextColor(
                            binding.root.context.getColor(android.R.color.holo_orange_dark)
                        )
                    }
                }
                
                // Show/hide action buttons based on status
                if (question.status == QuestionStatus.PENDING) {
                    btnApprove.visibility = android.view.View.VISIBLE
                    btnReject.visibility = android.view.View.VISIBLE
                } else {
                    btnApprove.visibility = android.view.View.GONE
                    btnReject.visibility = android.view.View.GONE
                }
                
                root.setOnClickListener {
                    onItemClick(question)
                }
                
                btnApprove.setOnClickListener {
                    onApproveClick(question)
                }
                
                btnReject.setOnClickListener {
                    onRejectClick(question)
                }
            }
        }
    }
    
    class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }
}

