package com.gurukulaboard.sync

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Database(entities = [QuestionEntity::class], version = 1, exportSchema = false)
abstract class OfflineCacheDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    
    companion object {
        @Volatile
        private var INSTANCE: OfflineCacheDatabase? = null
        
        fun getDatabase(context: Context): OfflineCacheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineCacheDatabase::class.java,
                    "offline_cache_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)
    
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionEntity?
    
    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>
    
    @Query("SELECT * FROM questions WHERE subject = :subject AND class_level = :classLevel")
    suspend fun getQuestionsBySubjectAndClass(subject: String, classLevel: Int): List<QuestionEntity>
    
    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestion(questionId: String)
    
    @Query("DELETE FROM questions")
    suspend fun clearAll()
}

@Singleton
class OfflineCache @Inject constructor(
    private val database: OfflineCacheDatabase
) {
    private val questionDao: QuestionDao = database.questionDao()
    
    suspend fun cacheQuestions(questions: List<com.gurukulaboard.models.Question>) {
        val entities = questions.map { question ->
            QuestionEntity.fromQuestion(question)
        }
        questionDao.insertQuestions(entities)
    }
    
    suspend fun getCachedQuestions(): List<com.gurukulaboard.models.Question> {
        val entities = questionDao.getAllQuestions()
        return entities.map { it.toQuestion() }
    }
    
    suspend fun getCachedQuestionById(questionId: String): com.gurukulaboard.models.Question? {
        val entity = questionDao.getQuestionById(questionId)
        return entity?.toQuestion()
    }
    
    suspend fun getCachedQuestionsBySubjectAndClass(subject: String, classLevel: Int): List<com.gurukulaboard.models.Question> {
        val entities = questionDao.getQuestionsBySubjectAndClass(subject, classLevel)
        return entities.map { it.toQuestion() }
    }
    
    suspend fun deleteCachedQuestion(questionId: String) {
        questionDao.deleteQuestion(questionId)
    }
    
    suspend fun clearCache() {
        questionDao.clearAll()
    }
}

