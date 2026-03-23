package com.aikido.musubi.domain.model

import java.text.SimpleDateFormat
import java.util.*

// ── Exercise types ───────────────────────────────────────────────────────────
enum class ExerciseType(
    val displayName: String,
    val description: String,
    val focusPoints: List<String>
) {
    FINGER_CONNECTION(
        displayName  = "Finger-by-Finger Connection",
        description  = "Apply pressure sequentially through each finger to create a deep, unified link with your partner's energy.",
        focusPoints  = listOf(
            "Extend arm forward at shoulder height",
            "Keep wrist relaxed, not rigid",
            "Fingers slightly spread",
            "Shoulder dropped, not raised"
        )
    ),
    KINETIC_CHAIN(
        displayName  = "Kinetic Chain Drill",
        description  = "Feel the connection travel from wrist → elbow → shoulder → back → hips → feet.",
        focusPoints  = listOf(
            "Stand upright with feet shoulder-width apart",
            "Arms naturally at sides",
            "Spine erect, hips neutral",
            "Weight balanced on both feet"
        )
    ),
    BAMBOO_STICK(
        displayName  = "Bamboo Stick Exploration",
        description  = "Move in sync with your partner while maintaining the stick's position without gripping.",
        focusPoints  = listOf(
            "Arms extended forward",
            "Soft elbow bend (not locked)",
            "Level shoulder line",
            "Forward-facing stance"
        )
    ),
    SOLO_CENTER(
        displayName  = "Solo Center (Hara) Practice",
        description  = "Move from your Hara — the center just below the navel — rather than from the shoulders.",
        focusPoints  = listOf(
            "Upright posture, chin parallel to floor",
            "Shoulders relaxed and back",
            "Lower belly slightly engaged",
            "Feet rooted, weight in heels"
        )
    )
}

// ── Posture feedback ─────────────────────────────────────────────────────────
data class PostureFeedback(
    val overallScore: Float,        // 0.0 – 1.0
    val isCorrect: Boolean,
    val primaryIssues: List<String>,
    val positivePoints: List<String>,
    val exerciseType: ExerciseType
) {
    val grade: String get() = when {
        overallScore >= 0.85f -> "Excellent"
        overallScore >= 0.70f -> "Good"
        overallScore >= 0.55f -> "Fair"
        else                  -> "Needs Work"
    }
}

// ── Session model ────────────────────────────────────────────────────────────
data class PracticeSession(
    val id: Long,
    val exerciseType: ExerciseType,
    val isCorrect: Boolean,
    val score: Float,
    val feedbackNotes: String,
    val durationMs: Long,
    val timestampMs: Long
) {
    val formattedDate: String get() {
        val sdf = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    val formattedDuration: String get() {
        val secs = durationMs / 1000
        return if (secs < 60) "${secs}s" else "${secs / 60}m ${secs % 60}s"
    }
}

// ── Stats model ──────────────────────────────────────────────────────────────
data class PracticeStats(
    val totalSessions: Int,
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val successRate: Float,
    val exerciseBreakdown: Map<ExerciseType, Float>   // type -> avg score
)
