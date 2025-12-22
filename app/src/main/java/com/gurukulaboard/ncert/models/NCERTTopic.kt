package com.gurukulaboard.ncert.models

data class NCERTTopic(
    val name: String,
    val startPage: Int,
    val endPage: Int,
    val order: Int = 0,
    val subtopics: List<NCERTSubTopic> = emptyList()
)

