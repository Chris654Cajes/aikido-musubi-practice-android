package com.aikido.musubi.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Entity ───────────────────────────────────────────────────────────────────
@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseType: String,       // e.g. "FINGER_CONNECTION", "KINETIC_CHAIN", etc.
    val isCorrect: Boolean,
    val score: Float,               // 0.0 – 1.0 posture score
    val feedbackNotes: String,
    val durationMs: Long,
    val timestampMs: Long = System.currentTimeMillis()
)

// ── DAO ──────────────────────────────────────────────────────────────────────
@Dao
interface PracticeSessionDao {

    @Insert
    suspend fun insert(session: PracticeSessionEntity): Long

    @Query("SELECT * FROM practice_sessions ORDER BY timestampMs DESC")
    fun getAllSessions(): Flow<List<PracticeSessionEntity>>

    @Query("SELECT * FROM practice_sessions WHERE exerciseType = :type ORDER BY timestampMs DESC")
    fun getSessionsByType(type: String): Flow<List<PracticeSessionEntity>>

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE isCorrect = 1")
    fun getTotalCorrect(): Flow<Int>

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE isCorrect = 0")
    fun getTotalIncorrect(): Flow<Int>

    @Query("SELECT COUNT(*) FROM practice_sessions")
    fun getTotalSessions(): Flow<Int>

    @Query("SELECT AVG(score) FROM practice_sessions WHERE exerciseType = :type")
    fun getAverageScoreForType(type: String): Flow<Float?>

    @Query("SELECT * FROM practice_sessions ORDER BY timestampMs DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 20): Flow<List<PracticeSessionEntity>>

    @Query("DELETE FROM practice_sessions")
    suspend fun clearAll()
}

// ── Database ─────────────────────────────────────────────────────────────────
@Database(
    entities = [PracticeSessionEntity::class],
    version  = 1,
    exportSchema = false
)
abstract class AikidoDatabase : RoomDatabase() {
    abstract fun practiceSessionDao(): PracticeSessionDao

    companion object {
        const val DATABASE_NAME = "aikido_musubi.db"
    }
}
