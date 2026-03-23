package com.aikido.musubi.data.repository
import com.aikido.musubi.data.database.AikidoDatabase
import com.aikido.musubi.data.database.PracticeSessionEntity
import com.aikido.musubi.domain.model.*
import kotlinx.coroutines.flow.*

class PracticeRepository(db: AikidoDatabase) {
    private val dao = db.practiceSessionDao()

    suspend fun saveSession(exerciseType: ExerciseType, isCorrect: Boolean, score: Float, feedbackNotes: String, durationMs: Long) =
        dao.insert(PracticeSessionEntity(exerciseType = exerciseType.name, isCorrect = isCorrect, score = score, feedbackNotes = feedbackNotes, durationMs = durationMs))

    fun getAllSessions(): Flow<List<PracticeSession>> = dao.getAllSessions().map { it.map { e ->
        PracticeSession(e.id, runCatching { ExerciseType.valueOf(e.exerciseType) }.getOrDefault(ExerciseType.SOLO_CENTER),
            e.isCorrect, e.score, e.feedbackNotes, e.durationMs, e.timestampMs)
    }}

    fun getStats(): Flow<PracticeStats> = combine(dao.getTotalSessions(), dao.getTotalCorrect(), dao.getTotalIncorrect()) { t, c, i ->
        PracticeStats(t, c, i, if (t > 0) c.toFloat() / t else 0f)
    }

    suspend fun clearAll() = dao.clearAll()
}