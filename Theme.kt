package com.sherakbar.family.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoft,
    onPrimaryContainer = ForestGreenDark,
    secondary = RoyalGold,
    onSecondary = DeepText,
    secondaryContainer = RoyalGoldSoft,
    onSecondaryContainer = ForestGreenDark,
    tertiary = EmeraldAccent,
    onTertiary = Color.White,
    background = OffWhiteBackground,
    onBackground = DeepText,
    surface = IvoryCard,
    onSurface = DeepText,
    surfaceVariant = SoftParchment,
    onSurfaceVariant = MutedText,
    outline = HairlineLight,
    error = CrimsonAlert,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.White,
    primaryContainer = ForestGreen,
    onPrimaryContainer = RoyalGoldSoft,
    secondary = RoyalGold,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = RoyalGold,
    tertiary = RoyalGold,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = HairlineDark,
    error = Color(0xFFF87171),
    onError = DarkBackground
)

@Composable
fun SherAkbarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = if (darkTheme) DarkBackground.toArgb() else ForestGreen.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SherAkbarTypography,
        content = content
    )
}
