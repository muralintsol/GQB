package com.gurukulaboard.content.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.R
import com.gurukulaboard.content.models.ContentType

class ContentCategoryAdapter(
    private val onFilterClick: (FilterChip) -> Unit
) : RecyclerView.Adapter<ContentCategoryAdapter.FilterChipViewHolder>() {
    
    val filters = mutableListOf<FilterChip>()
    
    fun setFilters(newFilters: List<FilterChip>) {
        filters.clear()
        filters.addAll(newFilters)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_chip, parent, false)
        return FilterChipViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FilterChipViewHolder, position: Int) {
        holder.bind(filters[position])
    }
    
    override fun getItemCount(): Int = filters.size
    
    inner class FilterChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipText: TextView = itemView.findViewById(R.id.tvFilterChip)
        
        fun bind(filter: FilterChip) {
            chipText.text = filter.label
            chipText.isSelected = filter.isSelected
            
            itemView.setOnClickListener {
                onFilterClick(filter)
            }
        }
    }
}

sealed class FilterChip(val label: String, var isSelected: Boolean = false) {
    class SubjectFilter(label: String, val subject: String, val classLevel: Int) : FilterChip(label)
    class TypeFilter(label: String, val contentType: ContentType) : FilterChip(label)
    class TagFilter(label: String, val tag: String) : FilterChip(label)
    class ClearFilter(label: String = "Clear Filters") : FilterChip(label)
}

