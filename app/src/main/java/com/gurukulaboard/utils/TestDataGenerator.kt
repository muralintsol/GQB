package com.gurukulaboard.utils

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.gurukulaboard.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestDataGenerator @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "TestDataGenerator"
    }
    
    fun generateTestQuestions() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if test questions already exist
                val existingQuestions = firestore.collection(Constants.COLLECTION_QUESTIONS)
                    .limit(1)
                    .get()
                    .await()
                
                if (!existingQuestions.isEmpty) {
                    Log.d(TAG, "Test questions already exist, skipping generation")
                    return@launch
                }
                
                val testQuestions = createTestQuestions()
                
                // Add questions to Firestore
                testQuestions.forEach { question ->
                    firestore.collection(Constants.COLLECTION_QUESTIONS)
                        .add(question)
                        .await()
                }
                
                Log.d(TAG, "Generated ${testQuestions.size} test questions")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating test questions", e)
            }
        }
    }
    
    private fun createTestQuestions(): List<Question> {
        val now = Timestamp.now()
        val superAdminId = "test_super_admin"
        
        return listOf(
            // PU Board Questions
            Question(
                content = "What is the value of sin(30°)?",
                type = QuestionType.MCQ,
                subject = "Physics",
                `class` = 11,
                chapter = "Trigonometry",
                difficulty = Difficulty.EASY,
                examType = ExamType.PU_BOARD,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "1/2",
                options = listOf("1/2", "√3/2", "1", "0")
            ),
            Question(
                content = "Explain Newton's First Law of Motion with examples.",
                type = QuestionType.LONG_ANSWER,
                subject = "Physics",
                `class` = 11,
                chapter = "Laws of Motion",
                difficulty = Difficulty.MEDIUM,
                examType = ExamType.PU_BOARD,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "Newton's First Law states that an object at rest stays at rest, and an object in motion stays in motion with constant velocity, unless acted upon by an external force."
            ),
            Question(
                content = "What is the chemical formula of water?",
                type = QuestionType.MCQ,
                subject = "Chemistry",
                `class` = 11,
                chapter = "Basic Concepts",
                difficulty = Difficulty.EASY,
                examType = ExamType.PU_BOARD,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "H2O",
                options = listOf("H2O", "CO2", "O2", "H2SO4")
            ),
            Question(
                content = "Define photosynthesis and write its equation.",
                type = QuestionType.LONG_ANSWER,
                subject = "Biology",
                `class` = 11,
                chapter = "Plant Physiology",
                difficulty = Difficulty.MEDIUM,
                examType = ExamType.PU_BOARD,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.PENDING,
                createdBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "Photosynthesis is the process by which plants convert light energy into chemical energy. Equation: 6CO2 + 6H2O → C6H12O6 + 6O2"
            ),
            Question(
                content = "Solve: 2x + 5 = 15",
                type = QuestionType.MCQ,
                subject = "Mathematics",
                `class` = 11,
                chapter = "Linear Equations",
                difficulty = Difficulty.EASY,
                examType = ExamType.PU_BOARD,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "5",
                options = listOf("5", "10", "7", "8")
            ),
            
            // NEET Questions
            Question(
                content = "Which of the following is the SI unit of electric current?",
                type = QuestionType.MCQ,
                subject = "Physics",
                `class` = 12,
                chapter = "Electricity",
                difficulty = Difficulty.EASY,
                examType = ExamType.NEET,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "Ampere",
                options = listOf("Ampere", "Volt", "Ohm", "Watt")
            ),
            Question(
                content = "What is the valency of carbon?",
                type = QuestionType.MCQ,
                subject = "Chemistry",
                `class` = 12,
                chapter = "Chemical Bonding",
                difficulty = Difficulty.MEDIUM,
                examType = ExamType.NEET,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "4",
                options = listOf("4", "2", "3", "1")
            ),
            Question(
                content = "Which organelle is known as the powerhouse of the cell?",
                type = QuestionType.MCQ,
                subject = "Biology",
                `class` = 12,
                chapter = "Cell Biology",
                difficulty = Difficulty.EASY,
                examType = ExamType.NEET,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "Mitochondria",
                options = listOf("Mitochondria", "Nucleus", "Ribosome", "Golgi Apparatus")
            ),
            
            // JEE Questions
            Question(
                content = "Find the derivative of f(x) = x² + 3x + 2",
                type = QuestionType.MCQ,
                subject = "Mathematics",
                `class` = 12,
                chapter = "Calculus",
                difficulty = Difficulty.MEDIUM,
                examType = ExamType.JEE,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "2x + 3",
                options = listOf("2x + 3", "x + 3", "2x", "x² + 3")
            ),
            Question(
                content = "What is the value of ∫(2x + 1)dx?",
                type = QuestionType.MCQ,
                subject = "Mathematics",
                `class` = 12,
                chapter = "Integration",
                difficulty = Difficulty.HARD,
                examType = ExamType.JEE,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "x² + x + C",
                options = listOf("x² + x + C", "2x² + x + C", "x² + C", "2x + C")
            ),
            Question(
                content = "Calculate the force between two charges of 2μC and 3μC separated by 1m in vacuum.",
                type = QuestionType.LONG_ANSWER,
                subject = "Physics",
                `class` = 12,
                chapter = "Electrostatics",
                difficulty = Difficulty.HARD,
                examType = ExamType.JEE,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.PENDING,
                createdBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "Using Coulomb's law: F = k(q1*q2)/r² = 9×10⁹ × (2×10⁻⁶ × 3×10⁻⁶)/1² = 54×10⁻³ N = 0.054 N"
            ),
            
            // K-CET Questions
            Question(
                content = "What is the pH of a neutral solution?",
                type = QuestionType.MCQ,
                subject = "Chemistry",
                `class` = 12,
                chapter = "Acids and Bases",
                difficulty = Difficulty.EASY,
                examType = ExamType.K_CET,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "7",
                options = listOf("7", "0", "14", "1")
            ),
            Question(
                content = "Which of the following is a vector quantity?",
                type = QuestionType.MCQ,
                subject = "Physics",
                `class` = 12,
                chapter = "Vectors",
                difficulty = Difficulty.MEDIUM,
                examType = ExamType.K_CET,
                source = QuestionSource.MANUAL,
                sourceDetails = "Test Data",
                status = QuestionStatus.APPROVED,
                createdBy = superAdminId,
                approvedBy = superAdminId,
                createdAt = now,
                updatedAt = now,
                answer = "Velocity",
                options = listOf("Velocity", "Mass", "Temperature", "Energy")
            )
        )
    }
}

