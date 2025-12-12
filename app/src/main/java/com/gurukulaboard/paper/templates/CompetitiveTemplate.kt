package com.gurukulaboard.paper.templates

import com.gurukulaboard.models.ExamType
import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionType

object CompetitiveTemplate {
    
    /**
     * Apply competitive exam format (NEET, JEE, K-CET)
     * Competitive exams typically have only MCQ questions
     */
    fun formatPaper(
        questions: List<Question>,
        examType: ExamType,
        totalMarks: Int
    ): List<Question> {
        // Competitive exams only have MCQ
        val mcqQuestions = questions.filter { it.type == QuestionType.MCQ }
        
        // Return questions based on total marks (assuming 4 marks per question for MCQ)
        val numberOfQuestions = totalMarks / 4
        return mcqQuestions.take(numberOfQuestions)
    }
    
    fun getTotalQuestionsForMarks(examType: ExamType, totalMarks: Int): Int {
        // Most competitive exams: 4 marks per question, -1 for wrong answer
        return when (examType) {
            ExamType.NEET -> totalMarks / 4 // 180 questions for 720 marks
            ExamType.JEE -> totalMarks / 4 // Varies
            ExamType.K_CET -> totalMarks / 1 // 1 mark per question typically
            else -> totalMarks / 4
        }
    }
}

