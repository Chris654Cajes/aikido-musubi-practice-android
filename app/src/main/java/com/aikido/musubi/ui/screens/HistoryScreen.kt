package com.aikido.musubi.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikido.musubi.domain.model.ExerciseType
import com.aikido.musubi.domain.model.PracticeSession
import com.aikido.musubi.domain.model.PracticeStats
import com.aikido.musubi.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    sessions: List<PracticeSession>,
    stats: PracticeStats?,
    onBack: () -> Unit,
    onClearHistory: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Onyx, CharcoalDark, CharcoalMid))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
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
                        .background(SurfaceVariant)
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = PureWhite)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Practice History", style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.SemiBold)
                    Text("${sessions.size} recorded sessions", style = MaterialTheme.typography.bodySmall, color = WhiteMuted)
                }
                if (sessions.isNotEmpty()) {
                    IconButton(
                        onClick  = { showClearDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ErrorRed.copy(alpha = 0.15f))
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, "Clear", tint = ErrorRed)
                    }
                }
            }

            if (sessions.isEmpty()) {
                EmptyHistoryState()
            } else {
                // Stats summary
                if (stats != null) {
                    StatsSection(stats = stats)
                }

                // Sessions list
                LazyColumn(
                    modifier              = Modifier.fillMaxSize(),
                    contentPadding        = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement   = Arrangement.spacedBy(10.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        SessionCard(session = session)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }

        // Clear confirmation dialog
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                containerColor   = CharcoalSurface,
                titleContentColor = PureWhite,
                textContentColor  = WhiteMuted,
                title = { Text("Clear All History") },
                text  = { Text("This will permanently delete all ${sessions.size} recorded sessions. This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onClearHistory()
                            showClearDialog = false
                        }
                    ) {
                        Text("Delete All", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", color = IndigoSoft)
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.HistoryEdu,
                contentDescription = null,
                tint               = SurfaceVariant,
                modifier           = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text      = "No Sessions Yet",
                style     = MaterialTheme.typography.headlineSmall,
                color     = WhiteMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "Complete your first practice session and your results will appear here.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = WhiteMuted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatsSection(stats: PracticeStats) {
    val successPct = (stats.successRate * 100).toInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("OVERVIEW", style = MaterialTheme.typography.labelMedium, color = IndigoSoft, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewItem("Total", "${stats.totalSessions}", PureWhite)
                OverviewItem("Correct", "${stats.totalCorrect}", SuccessGreen)
                OverviewItem("Incorrect", "${stats.totalIncorrect}", ErrorRed)
                OverviewItem("Success", "$successPct%", IndigoAccent)
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Progress bar
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Success Rate", style = MaterialTheme.typography.bodySmall, color = WhiteMuted)
                    Text("$successPct%", style = MaterialTheme.typography.bodySmall, color = IndigoAccent, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress    = { stats.successRate },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color       = IndigoAccent,
                    trackColor  = CharcoalDark
                )
            }
        }
    }
}

@Composable
private fun OverviewItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteMuted)
    }
}

@Composable
private fun SessionCard(session: PracticeSession) {
    val accentColor = if (session.isCorrect) SuccessGreen else ErrorRed
    val bgBrush     = Brush.horizontalGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.08f),
            CharcoalSurface
        )
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CharcoalSurface
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier         = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (session.isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint               = accentColor,
                    modifier           = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = session.exerciseType.displayName,
                    style      = MaterialTheme.typography.bodyMedium,
                    color      = PureWhite,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = session.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "${(session.score * 100).toInt()}%",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = session.formattedDuration,
                    style = MaterialTheme.typography.labelSmall,
                    color = WhiteMuted
                )
            }
        }
    }
}
