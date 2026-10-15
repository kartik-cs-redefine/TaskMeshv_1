package com.taskmesh.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.taskmesh.app.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkPrimaryAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkMainText,
    surface = DarkSurface,
    onSurface = DarkMainText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkDivider,
    outlineVariant = DarkDivider
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryAccent,
    onPrimary = LightSurface,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondaryAccent,
    onSecondary = LightSurface,
    background = LightBackground,
    onBackground = LightMainText,
    surface = LightSurface,
    onSurface = LightMainText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightMutedText,
    outline = LightDivider,
    outlineVariant = LightDivider
)

@Composable
fun TaskMeshTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
