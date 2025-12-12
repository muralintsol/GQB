package com.gurukulaboard.pdf.models

import com.gurukulaboard.models.Difficulty
import com.gurukulaboard.models.QuestionType

data class ExtractedQuestion(
    val content: String,
    val type: QuestionType = QuestionType.MCQ,
    val options: List<String>? = null,
    val answer: String? = null,
    val difficulty: Difficulty? = null,
    val pageNumber: Int = 0,
    val extractedAt: Long = System.currentTimeMillis()
)

