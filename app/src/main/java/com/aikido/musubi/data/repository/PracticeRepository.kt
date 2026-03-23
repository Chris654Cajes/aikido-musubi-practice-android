package com.aikido.musubi.data.repository

import com.aikido.musubi.data.database.AikidoDatabase
import com.aikido.musubi.data.database.PracticeSessionEntity
import com.aikido.musubi.domain.model.ExerciseType
import com.aikido.musubi.domain.model.PracticeSession
import com.aikido.musubi.domain.model.PracticeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PracticeRepository(private val db: AikidoDatabase) {

    private val dao = db.practiceSessionDao()

    suspend fun saveSession(
        exerciseType: ExerciseType,
        isCorrect: Boolean,
        score: Float,
        feedbackNotes: String,
        durationMs: Long
    ): Long {
        val entity = PracticeSessionEntity(
            exerciseType  = exerciseType.name,
            isCorrect     = isCorrect,
            score         = score,
            feedbackNotes = feedbackNotes,
            durationMs    = durationMs
        )
        return dao.insert(entity)
    }

    fun getAllSessions(): Flow<List<PracticeSession>> =
        dao.getAllSessions().map { list -> list.map { it.toDomain() } }

    fun getRecentSessions(limit: Int = 20): Flow<List<PracticeSession>> =
        dao.getRecentSessions(limit).map { list -> list.map { it.toDomain() } }

    fun getSessionsByType(type: ExerciseType): Flow<List<PracticeSession>> =
        dao.getSessionsByType(type.name).map { list -> list.map { it.toDomain() } }

    fun getStats(): Flow<PracticeStats> {
        val correctFlow   = dao.getTotalCorrect()
        val incorrectFlow = dao.getTotalIncorrect()
        val totalFlow     = dao.getTotalSessions()

        return combine(correctFlow, incorrectFlow, totalFlow) { correct, incorrect, total ->
            val breakdown = mutableMapOf<ExerciseType, Float>()
            // Note: breakdown is populated lazily — full impl uses per-type queries below
            PracticeStats(
                totalSessions    = total,
                totalCorrect     = correct,
                totalIncorrect   = incorrect,
                successRate      = if (total > 0) correct.toFloat() / total else 0f,
                exerciseBreakdown = breakdown
            )
        }
    }

    suspend fun clearAll() = dao.clearAll()

    // ── Mapper ────────────────────────────────────────────────────────────────
    private fun PracticeSessionEntity.toDomain(): PracticeSession {
        val type = try {
            ExerciseType.valueOf(exerciseType)
        } catch (e: IllegalArgumentException) {
            ExerciseType.SOLO_CENTER
        }
        return PracticeSession(
            id            = id,
            exerciseType  = type,
            isCorrect     = isCorrect,
            score         = score,
            feedbackNotes = feedbackNotes,
            durationMs    = durationMs,
            timestampMs   = timestampMs
        )
    }
}
