package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentSage,
    onPrimary = DarkGround,
    secondary = DarkSecondaryInk,
    onSecondary = DarkPrimaryInk,
    tertiary = IncomeGreen,
    onTertiary = DarkGround,
    background = DarkGround,
    onBackground = DarkPrimaryInk,
    surface = DarkSurface,
    onSurface = DarkPrimaryInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondaryInk,
    outline = DarkHairline,
    error = ExpenseTerracotta,
    onError = DarkPrimaryInk
)

private val LightColorScheme = lightColorScheme(
    primary = AccentSage,
    onPrimary = LightSurface,
    secondary = LightSecondaryInk,
    onSecondary = LightPrimaryInk,
    tertiary = IncomeGreen,
    onTertiary = LightSurface,
    background = LightGround,
    onBackground = LightPrimaryInk,
    surface = LightSurface,
    onSurface = LightPrimaryInk,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightSecondaryInk,
    outline = LightHairline,
    error = ExpenseTerracotta,
    onError = LightSurface
)

@Composable
fun HisaabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
