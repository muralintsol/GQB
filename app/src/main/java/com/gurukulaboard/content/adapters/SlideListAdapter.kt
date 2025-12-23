package com.gurukulaboard.content.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.R
import com.gurukulaboard.content.models.SlideData

class SlideListAdapter(
    private val onSlideClick: (Int) -> Unit,
    private val onSlideDelete: (Int) -> Unit
) : ListAdapter<SlideData, SlideListAdapter.SlideViewHolder>(SlideDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slide, parent, false)
        return SlideViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    
    inner class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val slideNumberView: TextView = itemView.findViewById(R.id.tvSlideNumber)
        private val slideTitleView: TextView = itemView.findViewById(R.id.tvSlideTitle)
        private val slideTypeView: TextView = itemView.findViewById(R.id.tvSlideType)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteSlide)
        
        fun bind(slide: SlideData, position: Int) {
            slideNumberView.text = "Slide ${slide.slideNumber}"
            slideTitleView.text = slide.title.ifBlank { "Untitled" }
            slideTypeView.text = slide.slideType.name
            
            itemView.setOnClickListener {
                onSlideClick(position)
            }
            
            deleteButton.setOnClickListener {
                onSlideDelete(position)
            }
        }
    }
    
    class SlideDiffCallback : DiffUtil.ItemCallback<SlideData>() {
        override fun areItemsTheSame(oldItem: SlideData, newItem: SlideData): Boolean {
            return oldItem.slideNumber == newItem.slideNumber
        }
        
        override fun areContentsTheSame(oldItem: SlideData, newItem: SlideData): Boolean {
            return oldItem == newItem
        }
    }
}

