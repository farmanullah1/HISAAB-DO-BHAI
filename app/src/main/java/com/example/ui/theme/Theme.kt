package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Clay Light Color Scheme (Primary / Default)
private val ClayLightColorScheme = lightColorScheme(
    primary = ClayAccentLimeDeep,
    onPrimary = ClayInkDeep,
    primaryContainer = ClayAccentLime,
    onPrimaryContainer = ClayInkDeep,
    secondary = ClayTextSecondary,
    onSecondary = ClayTextPrimary,
    tertiary = SemanticIncome,
    background = ClayBackground,
    onBackground = ClayTextPrimary,
    surface = ClaySurfaceElevated,
    onSurface = ClayTextPrimary,
    surfaceVariant = ClaySurface,
    onSurfaceVariant = ClayTextSecondary,
    outline = ClayBorderHairline,
    error = SemanticExpense,
    onError = ClaySurfaceElevated
)

// Clay Dark Color Scheme
private val ClayDarkColorScheme = darkColorScheme(
    primary = ClayAccentLime,
    onPrimary = ClayInkDeep,
    primaryContainer = ClayAccentLimeDeep,
    onPrimaryContainer = ClayInkDeep,
    secondary = DarkClayTextSecondary,
    onSecondary = DarkClayTextPrimary,
    tertiary = SemanticIncome,
    background = DarkClayBackground,
    onBackground = DarkClayTextPrimary,
    surface = DarkClaySurfaceElevated,
    onSurface = DarkClayTextPrimary,
    surfaceVariant = DarkClaySurface,
    onSurfaceVariant = DarkClayTextSecondary,
    outline = DarkClayBorderHairline,
    error = SemanticExpense,
    onError = DarkClayTextPrimary
)

@Composable
fun HisaabTheme(
    darkTheme: Boolean = false, // Light is default for Clay design!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ClayDarkColorScheme else ClayLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
