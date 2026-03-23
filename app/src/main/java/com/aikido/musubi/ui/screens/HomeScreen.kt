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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikido.musubi.domain.model.ExerciseType
import com.aikido.musubi.domain.model.PracticeStats
import com.aikido.musubi.ui.theme.*

@Composable
fun HomeScreen(
    stats: PracticeStats?,
    onExerciseSelected: (ExerciseType) -> Unit,
    onHistoryClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Onyx, CharcoalDark, CharcoalMid)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            // Header
            HomeHeader(stats = stats, onHistoryClick = onHistoryClick)

            Spacer(modifier = Modifier.height(24.dp))

            // Stats row
            if (stats != null && stats.totalSessions > 0) {
                StatsRow(stats = stats)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section title
            Text(
                text       = "CHOOSE YOUR PRACTICE",
                style      = MaterialTheme.typography.labelMedium,
                color      = IndigoSoft,
                letterSpacing = 3.sp,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Exercise cards
            ExerciseType.values().forEachIndexed { index, type ->
                ExerciseCard(
                    exerciseType = type,
                    index        = index,
                    onClick      = { onExerciseSelected(type) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Principles banner
            PrinciplesBanner()
        }
    }
}

@Composable
private fun HomeHeader(
    stats: PracticeStats?,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Column {
            Text(
                text  = "合気道",
                style = MaterialTheme.typography.titleSmall,
                color = IndigoSoft,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text  = "Musubi",
                style = MaterialTheme.typography.displaySmall,
                color = PureWhite,
                fontWeight = FontWeight.Black
            )
            Text(
                text  = "Connection Practice",
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteMuted
            )
        }

        IconButton(
            onClick  = onHistoryClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceVariant)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History",
                tint = IndigoSoft,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatsRow(stats: PracticeStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(
            label  = "Sessions",
            value  = "${stats.totalSessions}",
            icon   = Icons.Default.FitnessCenter,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            label  = "Correct",
            value  = "${stats.totalCorrect}",
            icon   = Icons.Default.CheckCircle,
            tint   = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            label  = "Rate",
            value  = "${(stats.successRate * 100).toInt()}%",
            icon   = Icons.Default.TrendingUp,
            tint   = IndigoAccent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color = IndigoBlueLight,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        color     = SurfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseCard(
    exerciseType: ExerciseType,
    index: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow_$index")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue   = 0.3f,
        targetValue    = 0.6f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000 + index * 300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label          = "glow"
    )

    val (icon, accentColor) = when (exerciseType) {
        ExerciseType.FINGER_CONNECTION -> Icons.Default.Gesture      to IndigoAccent
        ExerciseType.KINETIC_CHAIN     -> Icons.Default.AccountTree   to IndigoBlueLight
        ExerciseType.BAMBOO_STICK      -> Icons.Default.LinearScale   to IndigoSoft
        ExerciseType.SOLO_CENTER       -> Icons.Default.SelfImprovement to SuccessGreen
    }

    Card(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation        = 8.dp,
                shape            = RoundedCornerShape(20.dp),
                ambientColor     = accentColor.copy(alpha = glowAlpha),
                spotColor        = accentColor.copy(alpha = glowAlpha)
            ),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CharcoalSurface)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon bubble
            Box(
                modifier            = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f),
                                IndigoBlueDark.copy(alpha = 0.5f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = accentColor,
                    modifier           = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = exerciseType.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = exerciseType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteMuted,
                    maxLines   = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = accentColor
            )
        }
    }
}

@Composable
private fun PrinciplesBanner() {
    val principles = listOf(
        "Musubi" to "Blend with energy",
        "Kuzushi" to "Off-balance",
        "Leading" to "Guide the flow",
        "Non-Resistance" to "Soften the touch"
    )

    Surface(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(20.dp),
        color     = SurfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "CORE PRINCIPLES",
                style = MaterialTheme.typography.labelMedium,
                color = IndigoSoft,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            principles.forEachIndexed { i, (kanji, meaning) ->
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(kanji,   style = MaterialTheme.typography.titleSmall, color = PureWhite, fontWeight = FontWeight.SemiBold)
                    Text(meaning, style = MaterialTheme.typography.bodySmall,  color = WhiteMuted)
                }
                if (i < principles.lastIndex) HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}
