package com.gurukulaboard.paper

import com.gurukulaboard.models.*
import com.gurukulaboard.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaperGenerator @Inject constructor() {
    
    fun generatePaper(
        examType: ExamType,
        subject: String,
        classLevel: Int,
        availableQuestions: List<Question>,
        totalMarks: Int,
        difficultyDistribution: Map<Difficulty, Int> = getDefaultDistribution(),
        questionTypes: List<QuestionType> = emptyList(),
        chapters: List<String> = emptyList()
    ): Result<List<Question>> {
        return try {
            // Filter questions
            var filteredQuestions = availableQuestions.filter { it.status == QuestionStatus.APPROVED }
            
            // Filter by chapters if provided
            if (chapters.isNotEmpty()) {
                filteredQuestions = filteredQuestions.filter { it.chapter in chapters }
            }
            
            // Filter by question types if provided
            if (questionTypes.isNotEmpty()) {
                filteredQuestions = filteredQuestions.filter { it.type in questionTypes }
            }
            
            // Apply difficulty distribution
            val selectedQuestions = selectQuestionsByDifficulty(filteredQuestions, difficultyDistribution, totalMarks)
            
            if (selectedQuestions.isEmpty()) {
                Result.failure(Exception("Not enough questions available"))
            } else {
                Result.success(selectedQuestions)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun selectQuestionsByDifficulty(
        questions: List<Question>,
        distribution: Map<Difficulty, Int>,
        totalMarks: Int
    ): List<Question> {
        val selected = mutableListOf<Question>()
        val shuffled = questions.shuffled()
        
        // Calculate number of questions per difficulty
        val easyCount = (totalMarks * distribution[Difficulty.EASY]!! / 100).toInt()
        val mediumCount = (totalMarks * distribution[Difficulty.MEDIUM]!! / 100).toInt()
        val hardCount = totalMarks - easyCount - mediumCount
        
        // Select easy questions
        selected.addAll(shuffled.filter { it.difficulty == Difficulty.EASY }.take(easyCount))
        
        // Select medium questions
        selected.addAll(shuffled.filter { it.difficulty == Difficulty.MEDIUM }.take(mediumCount))
        
        // Select hard questions
        selected.addAll(shuffled.filter { it.difficulty == Difficulty.HARD }.take(hardCount))
        
        return selected.shuffled() // Shuffle final selection
    }
    
    companion object {
        fun getDefaultDistribution(): Map<Difficulty, Int> {
            return mapOf(
                Difficulty.EASY to Constants.DEFAULT_EASY_PERCENT,
                Difficulty.MEDIUM to Constants.DEFAULT_MEDIUM_PERCENT,
                Difficulty.HARD to Constants.DEFAULT_HARD_PERCENT
            )
        }
    }
}

