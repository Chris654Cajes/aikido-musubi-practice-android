package com.aikido.musubi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ─────────────────────────────────────────────────────────────────
val IndigoBlue        = Color(0xFF3949AB)
val IndigoBlueLight   = Color(0xFF6F74DD)
val IndigoBlueDark    = Color(0xFF00227B)
val IndigoAccent      = Color(0xFF536DFE)
val IndigoSoft        = Color(0xFF8C9EFF)

val Onyx              = Color(0xFF0D0D0D)
val CharcoalDark      = Color(0xFF1A1A2E)
val CharcoalMid       = Color(0xFF16213E)
val CharcoalSurface   = Color(0xFF1F2041)
val SurfaceVariant    = Color(0xFF252547)

val PureWhite         = Color(0xFFFFFFFF)
val OffWhite          = Color(0xFFF5F5F7)
val WhiteMuted        = Color(0xFFBDBDBD)

val SuccessGreen      = Color(0xFF00E676)
val ErrorRed          = Color(0xFFFF1744)
val WarnAmber         = Color(0xFFFFAB00)

// ── Color Schemes ────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = IndigoBlue,
    onPrimary          = PureWhite,
    primaryContainer   = IndigoBlueDark,
    onPrimaryContainer = IndigoSoft,
    secondary          = IndigoAccent,
    onSecondary        = PureWhite,
    tertiary           = IndigoBlueLight,
    background         = Onyx,
    onBackground       = PureWhite,
    surface            = CharcoalDark,
    onSurface          = OffWhite,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = WhiteMuted,
    outline            = IndigoBlue,
    error              = ErrorRed,
    onError            = PureWhite,
)

private val LightColorScheme = lightColorScheme(
    primary            = IndigoBlue,
    onPrimary          = PureWhite,
    primaryContainer   = Color(0xFFE8EAF6),
    onPrimaryContainer = IndigoBlueDark,
    secondary          = IndigoAccent,
    onSecondary        = PureWhite,
    tertiary           = IndigoBlueLight,
    background         = OffWhite,
    onBackground       = Onyx,
    surface            = PureWhite,
    onSurface          = Onyx,
    surfaceVariant     = Color(0xFFEEEEF5),
    onSurfaceVariant   = Color(0xFF444466),
    outline            = IndigoBlue,
    error              = ErrorRed,
    onError            = PureWhite,
)

@Composable
fun AikidoMusubiTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
