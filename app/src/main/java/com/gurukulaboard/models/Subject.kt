package com.gurukulaboard.models

data class Subject(
    val name: String,
    val examType: ExamType,
    val classes: List<Int> = listOf(11, 12)
) {
    companion object {
        // PU Board subjects
        val PU_BOARD_SUBJECTS = listOf(
            Subject("Physics", ExamType.PU_BOARD),
            Subject("Chemistry", ExamType.PU_BOARD),
            Subject("Mathematics", ExamType.PU_BOARD),
            Subject("Biology", ExamType.PU_BOARD),
            Subject("Kannada", ExamType.PU_BOARD),
            Subject("Hindi", ExamType.PU_BOARD),
            Subject("English", ExamType.PU_BOARD)
        )
        
        // NEET subjects
        val NEET_SUBJECTS = listOf(
            Subject("Physics", ExamType.NEET),
            Subject("Chemistry", ExamType.NEET),
            Subject("Biology", ExamType.NEET)
        )
        
        // JEE subjects
        val JEE_SUBJECTS = listOf(
            Subject("Physics", ExamType.JEE),
            Subject("Chemistry", ExamType.JEE),
            Subject("Mathematics", ExamType.JEE)
        )
        
        // K-CET subjects
        val K_CET_SUBJECTS = listOf(
            Subject("Physics", ExamType.K_CET),
            Subject("Chemistry", ExamType.K_CET),
            Subject("Mathematics", ExamType.K_CET),
            Subject("Biology", ExamType.K_CET)
        )
        
        fun getSubjectsForExamType(examType: ExamType): List<Subject> {
            return when (examType) {
                ExamType.PU_BOARD -> PU_BOARD_SUBJECTS
                ExamType.NEET -> NEET_SUBJECTS
                ExamType.JEE -> JEE_SUBJECTS
                ExamType.K_CET -> K_CET_SUBJECTS
            }
        }
    }
}

