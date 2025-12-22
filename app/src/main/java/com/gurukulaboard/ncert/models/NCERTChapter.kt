package com.gurukulaboard.ncert.models

data class NCERTChapter(
    val name: String,
    val number: Int,
    val startPage: Int,
    val endPage: Int,
    val order: Int = 0,
    val topics: List<NCERTTopic> = emptyList()
)

