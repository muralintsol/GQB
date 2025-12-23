package com.gurukulaboard.content.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentSection(
    val id: String,
    val title: String,
    val type: SectionType,
    val startPage: Int,
    val endPage: Int,
    val pageRange: String, // "5-8" format
    val preview: String, // First few lines of content
    var isSelected: Boolean = false
) : Parcelable {
    companion object {
        fun createId(type: SectionType, index: Int): String {
            return "${type.name}_$index"
        }
    }
}

enum class SectionType {
    SUBTOPIC,
    EXERCISE,
    CHAPTER,
    SUMMARY,
    OTHER
}

