package com.gurukulaboard.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Question(
    @DocumentId
    var id: String = "",
    val content: String = "",
    val type: QuestionType = QuestionType.MCQ,
    val subject: String = "",
    val `class`: Int = 11, // Using backtick to avoid keyword conflict
    val chapter: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val examType: ExamType = ExamType.PU_BOARD,
    val source: QuestionSource = QuestionSource.MANUAL,
    val sourceDetails: String? = null,
    val status: QuestionStatus = QuestionStatus.PENDING,
    val createdBy: String = "",
    val approvedBy: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val answer: String? = null,
    val options: List<String>? = null
) {
    companion object {
        const val COLLECTION_NAME = "questions"
    }
}

