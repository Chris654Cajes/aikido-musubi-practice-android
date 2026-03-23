package com.aikido.musubi.domain.model
import java.text.SimpleDateFormat
import java.util.*

enum class ExerciseType(val displayName: String, val description: String, val focusPoints: String) {
    FINGER_CONNECTION(
        "Finger-by-Finger Connection",
        "Apply pressure sequentially through each finger to create a deep, unified link.",
        "\u2022 Extend arm forward at shoulder height\n\u2022 Keep wrist relaxed, not rigid\n\u2022 Fingers slightly spread\n\u2022 Shoulder dropped, not raised"
    ),
    KINETIC_CHAIN(
        "Kinetic Chain Drill",
        "Feel the connection travel from wrist to elbow to shoulder to hips to feet.",
        "\u2022 Stand upright, feet shoulder-width apart\n\u2022 Arms naturally at sides\n\u2022 Spine erect, hips neutral\n\u2022 Weight balanced on both feet"
    ),
    BAMBOO_STICK(
        "Bamboo Stick Exploration",
        "Move in sync while maintaining the stick position without gripping.",
        "\u2022 Arms extended forward\n\u2022 Soft elbow bend, not locked\n\u2022 Level shoulder line\n\u2022 Forward-facing stance"
    ),
    SOLO_CENTER(
        "Solo Center (Hara) Practice",
        "Move from your Hara, the center just below the navel.",
        "\u2022 Upright posture, chin parallel to floor\n\u2022 Shoulders relaxed and back\n\u2022 Lower belly slightly engaged\n\u2022 Feet rooted, weight in heels"
    )
}

data class PostureFeedback(
    val overallScore: Float,
    val isCorrect: Boolean,
    val primaryIssues: List<String>,
    val positivePoints: List<String>,
    val exerciseType: ExerciseType
) {
    val grade: String get() = when {
        overallScore >= 0.85f -> "Excellent"
        overallScore >= 0.70f -> "Good"
        overallScore >= 0.55f -> "Fair"
        else -> "Needs Work"
    }
}

data class PracticeSession(
    val id: Long, val exerciseType: ExerciseType, val isCorrect: Boolean,
    val score: Float, val feedbackNotes: String, val durationMs: Long, val timestampMs: Long
) {
    val formattedDate: String get() = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault()).format(Date(timestampMs))
    val formattedDuration: String get() { val s = durationMs/1000; return if (s < 60) "${s}s" else "${s/60}m ${s%60}s" }
}

data class PracticeStats(val totalSessions: Int, val totalCorrect: Int, val totalIncorrect: Int, val successRate: Float)