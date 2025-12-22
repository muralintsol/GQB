package com.gurukulaboard.ncert

import com.gurukulaboard.models.Difficulty
import com.gurukulaboard.models.Question
import com.gurukulaboard.models.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NCERTMCQGenerator @Inject constructor() {
    
    data class MCQGenerationRequest(
        val content: String,
        val chapterName: String,
        val topicName: String? = null,
        val subtopicName: String? = null,
        val numberOfQuestions: Int = 5,
        val difficulty: Difficulty = Difficulty.MEDIUM
    )
    
    data class GeneratedMCQ(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int, // Index of correct option (0-based)
        val explanation: String? = null
    )
    
    /**
     * Generate MCQs from NCERT content using rule-based approach
     * Extracts key concepts and creates questions
     */
    suspend fun generateMCQs(request: MCQGenerationRequest): Result<List<GeneratedMCQ>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val mcqs = mutableListOf<GeneratedMCQ>()
            
            // Extract key sentences and concepts from content
            val sentences = extractSentences(request.content)
            val keyConcepts = extractKeyConcepts(request.content)
            
            // Generate questions based on key concepts
            for (i in 0 until minOf(request.numberOfQuestions, keyConcepts.size)) {
                val concept = keyConcepts[i]
                val mcq = generateMCQFromConcept(concept, sentences, request.difficulty)
                mcqs.add(mcq)
            }
            
            // If not enough questions, generate from sentences
            while (mcqs.size < request.numberOfQuestions && sentences.isNotEmpty()) {
                val sentence = sentences.random()
                val mcq = generateMCQFromSentence(sentence, sentences, request.difficulty)
                mcqs.add(mcq)
            }
            
            Result.success(mcqs.take(request.numberOfQuestions))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extract sentences from content
     */
    private fun extractSentences(content: String): List<String> {
        return content.split(Regex("[.!?]+"))
            .map { it.trim() }
            .filter { it.length > 20 && it.length < 200 } // Filter reasonable sentence length
            .filter { !it.matches(Regex("^\\d+[.)]\\s*$")) } // Filter numbered lists
    }
    
    /**
     * Extract key concepts (terms, definitions, important phrases)
     */
    private fun extractKeyConcepts(content: String): List<String> {
        val concepts = mutableListOf<String>()
        
        // Extract definitions (patterns like "X is Y", "X refers to Y")
        val definitionPattern = Regex("""([A-Z][a-z]+(?:\s+[A-Z][a-z]+)*)\s+(?:is|are|refers to|means|defined as)\s+(.+?)(?:[.,]|$)""", RegexOption.IGNORE_CASE)
        definitionPattern.findAll(content).forEach { match ->
            concepts.add("${match.groupValues[1]}: ${match.groupValues[2]}")
        }
        
        // Extract important terms (capitalized words/phrases)
        val termPattern = Regex("""\b([A-Z][a-z]+(?:\s+[A-Z][a-z]+){0,2})\b""")
        termPattern.findAll(content).forEach { match ->
            val term = match.groupValues[1]
            if (term.length > 3 && !term.matches(Regex("^(The|This|That|These|Those|Chapter|Unit|Page)"))) {
                concepts.add(term)
            }
        }
        
        return concepts.distinct().take(20) // Limit to top 20 concepts
    }
    
    /**
     * Generate MCQ from a concept
     */
    private fun generateMCQFromConcept(
        concept: String,
        sentences: List<String>,
        difficulty: Difficulty
    ): GeneratedMCQ {
        val parts = concept.split(":").map { it.trim() }
        val term = parts.getOrNull(0) ?: concept
        val definition = parts.getOrNull(1) ?: ""
        
        // Create question
        val question = when (difficulty) {
            Difficulty.EASY -> "What is $term?"
            Difficulty.MEDIUM -> "Which of the following best describes $term?"
            Difficulty.HARD -> "What is the significance of $term in the context of this chapter?"
        }
        
        // Generate options
        val correctOption = if (definition.isNotEmpty()) {
            definition.take(100) // Limit length
        } else {
            sentences.find { it.contains(term, ignoreCase = true) }?.take(100) ?: "The correct answer"
        }
        
        val wrongOptions = generateWrongOptions(term, sentences, correctOption)
        val allOptions = (listOf(correctOption) + wrongOptions).shuffled()
        val correctIndex = allOptions.indexOf(correctOption)
        
        return GeneratedMCQ(
            question = question,
            options = allOptions,
            correctAnswer = correctIndex,
            explanation = "Based on the concept: $concept"
        )
    }
    
    /**
     * Generate MCQ from a sentence
     */
    private fun generateMCQFromSentence(
        sentence: String,
        allSentences: List<String>,
        difficulty: Difficulty
    ): GeneratedMCQ {
        // Extract key term from sentence
        val words = sentence.split(" ").filter { it.length > 4 }
        val keyTerm = words.randomOrNull() ?: "concept"
        
        // Create question
        val question = "According to the text, which statement is correct?"
        
        // Use sentence as correct option
        val correctOption = sentence.take(120)
        
        // Generate wrong options from other sentences
        val wrongOptions = allSentences
            .filter { it != sentence }
            .shuffled()
            .take(3)
            .map { it.take(120) }
        
        val allOptions = (listOf(correctOption) + wrongOptions).shuffled()
        val correctIndex = allOptions.indexOf(correctOption)
        
        return GeneratedMCQ(
            question = question,
            options = allOptions,
            correctAnswer = correctIndex,
            explanation = "This is directly stated in the text."
        )
    }
    
    /**
     * Generate wrong/distractor options
     */
    private fun generateWrongOptions(
        term: String,
        sentences: List<String>,
        correctOption: String
    ): List<String> {
        val wrongOptions = mutableListOf<String>()
        
        // Get random sentences that don't contain the term
        val otherSentences = sentences
            .filter { !it.contains(term, ignoreCase = true) && it != correctOption }
            .shuffled()
            .take(3)
        
        wrongOptions.addAll(otherSentences.map { it.take(100) })
        
        // Add generic wrong options if needed
        while (wrongOptions.size < 3) {
            wrongOptions.add("This is not mentioned in the text.")
        }
        
        return wrongOptions.take(3)
    }
    
    /**
     * Convert GeneratedMCQ to Question model
     */
    fun toQuestion(
        generatedMCQ: GeneratedMCQ,
        subject: String,
        classLevel: Int,
        chapter: String,
        topic: String? = null,
        createdBy: String
    ): Question {
        return Question(
            content = generatedMCQ.question,
            type = QuestionType.MCQ,
            subject = subject,
            `class` = classLevel,
            chapter = chapter,
            difficulty = Difficulty.MEDIUM, // Can be enhanced
            examType = com.gurukulaboard.models.ExamType.PU_BOARD,
            source = com.gurukulaboard.models.QuestionSource.MANUAL,
            sourceDetails = "Generated from NCERT: ${topic ?: chapter}",
            status = com.gurukulaboard.models.QuestionStatus.PENDING,
            createdBy = createdBy,
            answer = generatedMCQ.options[generatedMCQ.correctAnswer],
            options = generatedMCQ.options
        )
    }
}

