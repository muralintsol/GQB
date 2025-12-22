package com.gurukulaboard.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurukulaboard.models.*

/**
 * Room entity for offline caching of questions.
 * Separate from Firestore Question model to avoid conflicts.
 */
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey
    var id: String = "",
    var content: String = "",
    var type: String = "", // QuestionType enum as string
    var subject: String = "",
    @ColumnInfo(name = "class_level")
    var classLevel: Int = 11, // Renamed from `class` to avoid keyword conflict
    var chapter: String = "",
    var difficulty: String = "", // Difficulty enum as string
    var examType: String = "", // ExamType enum as string
    var source: String = "", // QuestionSource enum as string
    var sourceDetails: String? = null,
    var status: String = "", // QuestionStatus enum as string
    var createdBy: String = "",
    var approvedBy: String? = null,
    var createdAt: Long? = null, // Timestamp as Long
    var updatedAt: Long? = null, // Timestamp as Long
    var answer: String? = null,
    var options: String? = null // List<String> serialized as JSON string
) {
    companion object {
        private val gson = Gson()
        
        fun fromQuestion(question: Question): QuestionEntity {
            return QuestionEntity(
                id = question.id,
                content = question.content,
                type = question.type.name,
                subject = question.subject,
                classLevel = question.`class`,
                chapter = question.chapter,
                difficulty = question.difficulty.name,
                examType = question.examType.name,
                source = question.source.name,
                sourceDetails = question.sourceDetails,
                status = question.status.name,
                createdBy = question.createdBy,
                approvedBy = question.approvedBy,
                createdAt = question.createdAt?.seconds,
                updatedAt = question.updatedAt?.seconds,
                answer = question.answer,
                options = question.options?.let { gson.toJson(it) }
            )
        }
    }
    
    fun toQuestion(): Question {
        val optionsList = options?.let {
            try {
                val listType = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(it, listType)
            } catch (e: Exception) {
                null
            }
        }
        
        return Question(
            id = id,
            content = content,
            type = QuestionType.valueOf(type),
            subject = subject,
            `class` = classLevel,
            chapter = chapter,
            difficulty = Difficulty.valueOf(difficulty),
            examType = ExamType.valueOf(examType),
            source = QuestionSource.valueOf(source),
            sourceDetails = sourceDetails,
            status = QuestionStatus.valueOf(status),
            createdBy = createdBy,
            approvedBy = approvedBy,
            createdAt = createdAt?.let { Timestamp(it, 0) },
            updatedAt = updatedAt?.let { Timestamp(it, 0) },
            answer = answer,
            options = optionsList
        )
    }
}

