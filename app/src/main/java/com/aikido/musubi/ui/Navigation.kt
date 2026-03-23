package com.aikido.musubi.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.aikido.musubi.domain.model.ExerciseType
import com.aikido.musubi.ui.screens.HistoryScreen
import com.aikido.musubi.ui.screens.HomeScreen
import com.aikido.musubi.ui.screens.PracticeScreen

sealed class Screen {
    object Home     : Screen()
    object Practice : Screen()
    object History  : Screen()
}

@Composable
fun AikidoNavHost(viewModel: PracticeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    when (currentScreen) {
        is Screen.Home -> HomeScreen(
            stats               = uiState.stats,
            onExerciseSelected  = { type ->
                viewModel.selectExercise(type)
                currentScreen = Screen.Practice
            },
            onHistoryClick = { currentScreen = Screen.History }
        )

        is Screen.Practice -> PracticeScreen(
            uiState         = uiState,
            onBack          = { currentScreen = Screen.Home },
            onStartSession  = viewModel::startSession,
            onStopSession   = viewModel::stopSession,
            onPoseDetected  = viewModel::onPoseDetected,
            onSaveSession   = viewModel::saveCurrentSession,
            onDismissFeedback = viewModel::dismissFeedback
        )

        is Screen.History -> HistoryScreen(
            sessions       = uiState.recentSessions,
            stats          = uiState.stats,
            onBack         = { currentScreen = Screen.Home },
            onClearHistory = viewModel::clearHistory
        )
    }
}