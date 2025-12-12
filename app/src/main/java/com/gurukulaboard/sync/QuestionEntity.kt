package com.gurukulaboard.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
)

