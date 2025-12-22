package com.gurukulaboard.ncert.models

data class NCERTIndex(
    val bookId: String,
    val chapters: List<NCERTChapter> = emptyList(),
    val totalPages: Int = 0
)

