package com.gurukulaboard.paper.templates

import com.gurukulaboard.models.ExamType
import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionType

object PUBoardTemplate {
    
    /**
     * Apply PU Board format to questions
     * PU Board format typically has:
     * - Section A: MCQ (1 mark each)
     * - Section B: Short Answer (2 marks each)
     * - Section C: Long Answer (5 marks each)
     */
    fun formatPaper(questions: List<Question>, totalMarks: Int): FormattedPaper {
        val mcqQuestions = questions.filter { it.type == QuestionType.MCQ }
        val shortAnswerQuestions = questions.filter { it.type == QuestionType.SHORT_ANSWER }
        val longAnswerQuestions = questions.filter { it.type == QuestionType.LONG_ANSWER }
        
        // PU Board format distribution
        val sectionA = mcqQuestions.take(totalMarks / 10) // ~10% MCQ
        val sectionB = shortAnswerQuestions.take(totalMarks / 5) // ~20% Short Answer
        val sectionC = longAnswerQuestions.take(totalMarks / 10) // ~10% Long Answer
        
        return FormattedPaper(
            sectionA = sectionA,
            sectionB = sectionB,
            sectionC = sectionC,
            totalMarks = totalMarks
        )
    }
}

data class FormattedPaper(
    val sectionA: List<Question>,
    val sectionB: List<Question>,
    val sectionC: List<Question>,
    val totalMarks: Int
) {
    val allQuestions: List<Question>
        get() = sectionA + sectionB + sectionC
}

