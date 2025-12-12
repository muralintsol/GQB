package com.gurukulaboard.models

data class Chapter(
    val id: String = "",
    val name: String,
    val subject: String,
    val examType: ExamType,
    val classLevel: Int,
    val order: Int = 0
)

