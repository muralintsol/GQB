package com.gurukulaboard.ncert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gurukulaboard.databinding.ItemNcertBookBinding
import com.gurukulaboard.ncert.models.NCERTBook
import java.text.SimpleDateFormat
import java.util.*

class NCERTBooksAdapter(
    private val onItemClick: (NCERTBook) -> Unit,
    private val onProcessClick: (NCERTBook) -> Unit
) : ListAdapter<NCERTBook, NCERTBooksAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNcertBookBinding.inflate(
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
        private val binding: ItemNcertBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(book: NCERTBook) {
            binding.tvSubject.text = book.subject
            binding.tvClass.text = "Class ${book.classLevel}"
            binding.tvFileName.text = book.fileName
            binding.tvLanguage.text = "Language: ${book.language}"
            binding.tvStatus.text = "Status: ${book.status.name}"
            
            book.uploadedAt?.let {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.tvUploadDate.text = "Uploaded: ${dateFormat.format(it.toDate())}"
            }
            
            binding.root.setOnClickListener {
                onItemClick(book)
            }
            
            binding.btnProcess.setOnClickListener {
                onProcessClick(book)
            }
            
            binding.btnProcess.isEnabled = book.status != NCERTBook.NCERTBookStatus.INDEXED
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<NCERTBook>() {
        override fun areItemsTheSame(oldItem: NCERTBook, newItem: NCERTBook): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: NCERTBook, newItem: NCERTBook): Boolean {
            return oldItem == newItem
        }
    }
}

