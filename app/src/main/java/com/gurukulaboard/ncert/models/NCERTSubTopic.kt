package com.gurukulaboard.ncert.models

data class NCERTSubTopic(
    val name: String,
    val startPage: Int,
    val endPage: Int,
    val order: Int = 0
)

