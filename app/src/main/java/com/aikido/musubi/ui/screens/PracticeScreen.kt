package com.aikido.musubi.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikido.musubi.domain.model.ExerciseType
import com.aikido.musubi.domain.model.PostureFeedback
import com.aikido.musubi.ui.PracticeUiState
import com.aikido.musubi.ui.components.CameraPreviewWithPose
import com.aikido.musubi.ui.theme.*
import com.google.mlkit.vision.pose.Pose

@Composable
fun PracticeScreen(
    uiState: PracticeUiState,
    onBack: () -> Unit,
    onStartSession: () -> Unit,
    onStopSession: () -> Unit,
    onPoseDetected: (Pose) -> Unit,
    onSaveSession: () -> Unit,
    onDismissFeedback: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
    ) {
        // Camera preview (always visible when active)
        if (uiState.isAnalyzing) {
            CameraPreviewWithPose(
                modifier       = Modifier.fillMaxSize(),
                onPoseDetected = onPoseDetected
            )
            // Dark overlay so UI is readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            PracticeTopBar(
                exercise   = uiState.selectedExercise,
                isAnalyzing = uiState.isAnalyzing,
                onBack     = onBack
            )

            if (!uiState.isAnalyzing) {
                // Instruction panel when not analyzing
                InstructionPanel(exercise = uiState.selectedExercise)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Feedback overlay (animated)
            AnimatedVisibility(
                visible = uiState.showFeedbackOverlay && uiState.currentFeedback != null,
                enter   = slideInVertically { it / 2 } + fadeIn(),
                exit    = slideOutVertically { it / 2 } + fadeOut()
            ) {
                uiState.currentFeedback?.let { feedback ->
                    LiveFeedbackPanel(
                        feedback      = feedback,
                        onSave        = onSaveSession,
                        onDismiss     = onDismissFeedback
                    )
                }
            }

            // Control buttons
            PracticeControls(
                isAnalyzing    = uiState.isAnalyzing,
                onStart        = onStartSession,
                onStop         = onStopSession
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Saved confirmation snackbar
        AnimatedVisibility(
            visible  = uiState.sessionJustSaved,
            enter    = slideInVertically { -it } + fadeIn(),
            exit     = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = SuccessGreen.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Session saved!", style = MaterialTheme.typography.labelLarge, color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun PracticeTopBar(
    exercise: ExerciseType,
    isAnalyzing: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 12.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isAnalyzing) Color.Black.copy(0.6f) else SurfaceVariant)
                .size(44.dp)
        ) {
            Icon(Icons.Default.ArrowBack, "Back", tint = PureWhite)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text      = exercise.displayName,
                style     = MaterialTheme.typography.titleMedium,
                color     = PureWhite,
                fontWeight = FontWeight.SemiBold
            )
            if (isAnalyzing) {
                RecordingBadge()
            }
        }
    }
}

@Composable
private fun RecordingBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "rec")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "blink"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer { this.alpha = alpha }
                .clip(CircleShape)
                .background(ErrorRed)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("ANALYZING", style = MaterialTheme.typography.labelSmall, color = WhiteMuted, letterSpacing = 1.sp)
    }
}

@Composable
private fun InstructionPanel(exercise: ExerciseType) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text  = "FOCUS POINTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = IndigoSoft,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                exercise.focusPoints.forEach { point ->
                    Row(
                        modifier          = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(IndigoAccent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(point, style = MaterialTheme.typography.bodySmall, color = OffWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = IndigoBlueDark.copy(alpha = 0.6f)
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Videocam, null, tint = IndigoSoft, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Position yourself so your full body is visible to the front camera",
                    style = MaterialTheme.typography.bodySmall,
                    color = IndigoSoft
                )
            }
        }
    }
}

@Composable
private fun LiveFeedbackPanel(
    feedback: PostureFeedback,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val bgColor = if (feedback.isCorrect)
        SuccessGreen.copy(alpha = 0.12f)
    else
        ErrorRed.copy(alpha = 0.10f)

    val accentColor = if (feedback.isCorrect) SuccessGreen else ErrorRed

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0D0D1A).copy(alpha = 0.96f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Grade row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (feedback.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = feedback.grade,
                        style      = MaterialTheme.typography.titleMedium,
                        color      = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text  = "${(feedback.overallScore * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score bar
            LinearProgressIndicator(
                progress          = { feedback.overallScore },
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color             = accentColor,
                trackColor        = SurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Issues
            if (feedback.primaryIssues.isNotEmpty()) {
                Text("IMPROVE", style = MaterialTheme.typography.labelSmall, color = ErrorRed, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                feedback.primaryIssues.forEach { issue ->
                    Row(
                        modifier          = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ArrowRight, null, tint = WarnAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(issue, style = MaterialTheme.typography.bodySmall, color = OffWhite)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Positives
            if (feedback.positivePoints.isNotEmpty()) {
                Text("GOOD", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                feedback.positivePoints.take(2).forEach { point ->
                    Row(
                        modifier          = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(point, style = MaterialTheme.typography.bodySmall, color = OffWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick   = onDismiss,
                    modifier  = Modifier.weight(1f),
                    shape     = RoundedCornerShape(14.dp),
                    border    = BorderStroke(1.dp, SurfaceVariant)
                ) {
                    Text("Dismiss", color = WhiteMuted, style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick  = onSave,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = IndigoBlue)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun PracticeControls(
    isAnalyzing: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (!isAnalyzing) {
            Button(
                onClick  = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = IndigoBlue
                )
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Analysis", style = MaterialTheme.typography.labelLarge, fontSize = 16.sp)
            }
        } else {
            Button(
                onClick  = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.85f)
                )
            ) {
                Icon(Icons.Default.Stop, null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop", style = MaterialTheme.typography.labelLarge, fontSize = 16.sp)
            }
        }
    }
}
