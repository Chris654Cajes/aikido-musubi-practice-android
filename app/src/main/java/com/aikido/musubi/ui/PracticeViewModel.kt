package com.aikido.musubi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aikido.musubi.data.repository.PracticeRepository
import com.aikido.musubi.domain.model.ExerciseType
import com.aikido.musubi.domain.model.PostureFeedback
import com.aikido.musubi.domain.model.PracticeSession
import com.aikido.musubi.domain.model.PracticeStats
import com.aikido.musubi.domain.usecase.PoseAnalyzer
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── UI State ─────────────────────────────────────────────────────────────────
data class PracticeUiState(
    val selectedExercise: ExerciseType = ExerciseType.SOLO_CENTER,
    val isAnalyzing: Boolean           = false,
    val currentFeedback: PostureFeedback? = null,
    val sessionStartMs: Long           = 0L,
    val recentSessions: List<PracticeSession> = emptyList(),
    val stats: PracticeStats? = null,
    val showFeedbackOverlay: Boolean   = false,
    val sessionJustSaved: Boolean      = false
)

class PracticeViewModel(
    private val repository: PracticeRepository
) : ViewModel() {

    private val poseAnalyzer = PoseAnalyzer()

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        loadRecentSessions()
        loadStats()
    }

    private fun loadRecentSessions() {
        viewModelScope.launch {
            repository.getRecentSessions(20).collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getStats().collect { stats ->
                _uiState.update { it.copy(stats = stats) }
            }
        }
    }

    fun selectExercise(type: ExerciseType) {
        _uiState.update {
            it.copy(
                selectedExercise    = type,
                currentFeedback     = null,
                showFeedbackOverlay = false,
                sessionJustSaved    = false
            )
        }
    }

    fun startSession() {
        _uiState.update {
            it.copy(
                isAnalyzing      = true,
                sessionStartMs   = System.currentTimeMillis(),
                currentFeedback  = null,
                showFeedbackOverlay = false,
                sessionJustSaved = false
            )
        }
    }

    fun stopSession() {
        _uiState.update { it.copy(isAnalyzing = false) }
    }

    fun onPoseDetected(pose: Pose) {
        val state = _uiState.value
        if (!state.isAnalyzing) return

        val feedback = poseAnalyzer.analyze(pose, state.selectedExercise)
        _uiState.update { it.copy(currentFeedback = feedback, showFeedbackOverlay = true) }
    }

    fun saveCurrentSession() {
        val state    = _uiState.value
        val feedback = state.currentFeedback ?: return
        val duration = System.currentTimeMillis() - state.sessionStartMs

        viewModelScope.launch {
            repository.saveSession(
                exerciseType  = state.selectedExercise,
                isCorrect     = feedback.isCorrect,
                score         = feedback.overallScore,
                feedbackNotes = buildFeedbackNotes(feedback),
                durationMs    = duration
            )
            _uiState.update {
                it.copy(
                    isAnalyzing         = false,
                    showFeedbackOverlay = false,
                    sessionJustSaved    = true
                )
            }
        }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(showFeedbackOverlay = false) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearAll() }
    }

    private fun buildFeedbackNotes(feedback: PostureFeedback): String {
        val sb = StringBuilder()
        if (feedback.positivePoints.isNotEmpty()) {
            sb.append("✓ ").append(feedback.positivePoints.joinToString("; "))
        }
        if (feedback.primaryIssues.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append(" | ")
            sb.append("✗ ").append(feedback.primaryIssues.joinToString("; "))
        }
        return sb.toString()
    }

    // ── Factory ────────────────────────────────────────────────────────────────
    class Factory(private val repository: PracticeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PracticeViewModel(repository) as T
    }
}
