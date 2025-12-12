package com.gurukulaboard.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class HeaderFooterConfig(
    val schoolName: String = "",
    val date: String = "",
    val subject: String = "",
    val classLevel: String = "",
    val examType: String = ""
)

data class QuestionPaper(
    @DocumentId
    var id: String = "",
    val title: String = "",
    val examType: ExamType = ExamType.PU_BOARD,
    val subject: String = "",
    val `class`: Int = 11,
    val questions: List<String> = emptyList(), // Question IDs
    val difficultyDistribution: Map<Difficulty, Int> = emptyMap(),
    val totalMarks: Int = 0,
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val headerFooter: HeaderFooterConfig = HeaderFooterConfig()
) {
    companion object {
        const val COLLECTION_NAME = "questionPapers"
    }
}

