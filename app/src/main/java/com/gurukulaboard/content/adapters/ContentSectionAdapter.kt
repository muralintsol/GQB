package com.gurukulaboard.content.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.R
import com.gurukulaboard.content.models.ContentSection
import com.gurukulaboard.content.models.SectionType

class ContentSectionAdapter(
    private val onSelectionChanged: (ContentSection, Boolean) -> Unit
) : ListAdapter<ContentSection, ContentSectionAdapter.SectionViewHolder>(SectionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_section, parent, false)
        return SectionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkboxSection)
        private val titleView: TextView = itemView.findViewById(R.id.tvSectionTitle)
        private val typeView: TextView = itemView.findViewById(R.id.tvSectionType)
        private val pageRangeView: TextView = itemView.findViewById(R.id.tvPageRange)
        private val previewView: TextView = itemView.findViewById(R.id.tvPreview)
        
        fun bind(section: ContentSection) {
            checkbox.isChecked = section.isSelected
            titleView.text = section.title
            typeView.text = section.type.name
            pageRangeView.text = "Pages ${section.pageRange}"
            previewView.text = section.preview
            
            // Set type badge color
            val typeColor = when (section.type) {
                SectionType.SUBTOPIC -> itemView.context.getColor(R.color.holo_blue_dark)
                SectionType.EXERCISE -> itemView.context.getColor(R.color.holo_green_dark)
                else -> itemView.context.getColor(R.color.holo_orange_dark)
            }
            typeView.setTextColor(typeColor)
            
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                section.isSelected = isChecked
                onSelectionChanged(section, isChecked)
            }
            
            itemView.setOnClickListener {
                checkbox.isChecked = !checkbox.isChecked
            }
        }
    }
    
    class SectionDiffCallback : DiffUtil.ItemCallback<ContentSection>() {
        override fun areItemsTheSame(oldItem: ContentSection, newItem: ContentSection): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ContentSection, newItem: ContentSection): Boolean {
            return oldItem == newItem
        }
    }
}

