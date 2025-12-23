package com.gurukulaboard.content.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.R
import com.gurukulaboard.content.models.ContentType
import com.gurukulaboard.content.models.TeachingContent
import java.text.SimpleDateFormat
import java.util.*

class ContentListAdapter(
    private val onItemClick: (TeachingContent) -> Unit,
    private val onFavoriteClick: (TeachingContent) -> Unit
) : ListAdapter<TeachingContent, ContentListAdapter.ContentViewHolder>(ContentDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content, parent, false)
        return ContentViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.ivContentIcon)
        private val titleView: TextView = itemView.findViewById(R.id.tvContentTitle)
        private val subjectView: TextView = itemView.findViewById(R.id.tvSubject)
        private val classView: TextView = itemView.findViewById(R.id.tvClass)
        private val chapterView: TextView = itemView.findViewById(R.id.tvChapter)
        private val typeBadge: TextView = itemView.findViewById(R.id.tvContentType)
        private val tagsView: TextView = itemView.findViewById(R.id.tvTags)
        private val downloadCountView: TextView = itemView.findViewById(R.id.tvDownloadCount)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.ivFavorite)
        private val dateView: TextView = itemView.findViewById(R.id.tvDate)
        
        fun bind(content: TeachingContent) {
            titleView.text = content.title
            subjectView.text = content.subject
            classView.text = "Class ${content.classLevel}"
            chapterView.text = content.chapter ?: "General"
            typeBadge.text = content.contentType.name
            tagsView.text = content.tags.joinToString(", ")
            downloadCountView.text = "${content.downloadCount} downloads"
            
            // Set icon based on content type
            val iconRes = when (content.contentType) {
                ContentType.NOTES -> android.R.drawable.ic_menu_edit
                ContentType.WORKSHEET -> android.R.drawable.ic_menu_agenda
                ContentType.PRESENTATION -> android.R.drawable.ic_menu_view
                ContentType.LESSON_PLAN -> android.R.drawable.ic_menu_recent_history
                ContentType.REFERENCE -> android.R.drawable.ic_menu_info_details
                ContentType.OTHER -> android.R.drawable.ic_menu_help
            }
            iconView.setImageResource(iconRes)
            
            // Set favorite icon
            favoriteIcon.setImageResource(
                if (content.isFavorite) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )
            
            // Set date
            content.createdAt?.toDate()?.let { date ->
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                dateView.text = dateFormat.format(date)
            } ?: run {
                dateView.text = ""
            }
            
            // Click listeners
            itemView.setOnClickListener {
                onItemClick(content)
            }
            
            favoriteIcon.setOnClickListener {
                onFavoriteClick(content)
            }
        }
    }
    
    class ContentDiffCallback : DiffUtil.ItemCallback<TeachingContent>() {
        override fun areItemsTheSame(oldItem: TeachingContent, newItem: TeachingContent): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: TeachingContent, newItem: TeachingContent): Boolean {
            return oldItem == newItem
        }
    }
}

