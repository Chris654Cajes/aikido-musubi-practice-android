package com.aikido.musubi.data.database
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseType: String, val isCorrect: Boolean, val score: Float,
    val feedbackNotes: String, val durationMs: Long,
    val timestampMs: Long = System.currentTimeMillis()
)

@Dao
interface PracticeSessionDao {
    @Insert suspend fun insert(session: PracticeSessionEntity): Long
    @Query("SELECT * FROM practice_sessions ORDER BY timestampMs DESC") fun getAllSessions(): Flow<List<PracticeSessionEntity>>
    @Query("SELECT COUNT(*) FROM practice_sessions") fun getTotalSessions(): Flow<Int>
    @Query("SELECT COUNT(*) FROM practice_sessions WHERE isCorrect = 1") fun getTotalCorrect(): Flow<Int>
    @Query("SELECT COUNT(*) FROM practice_sessions WHERE isCorrect = 0") fun getTotalIncorrect(): Flow<Int>
    @Query("DELETE FROM practice_sessions") suspend fun clearAll()
}

@Database(entities = [PracticeSessionEntity::class], version = 1, exportSchema = false)
abstract class AikidoDatabase : RoomDatabase() {
    abstract fun practiceSessionDao(): PracticeSessionDao
    companion object { const val DATABASE_NAME = "aikido_musubi.db" }
}