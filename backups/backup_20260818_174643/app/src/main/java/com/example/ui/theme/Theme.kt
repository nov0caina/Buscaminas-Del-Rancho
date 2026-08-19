package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RanchoNightPrimary,
    onPrimary = RanchoNightOnPrimary,
    primaryContainer = RanchoNightPrimaryContainer,
    secondary = RanchoNightCactusSecondary,
    secondaryContainer = RanchoNightCactusContainer,
    tertiary = RanchoNightTerracottaTertiary,
    background = RanchoNightBackground,
    surface = RanchoNightSurface,
    surfaceVariant = RanchoNightSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = RanchoOchrePrimary,
    onPrimary = RanchoOchreOnPrimary,
    primaryContainer = RanchoGoldContainer,
    onPrimaryContainer = RanchoGoldOnContainer,
    secondary = RanchoCactusSecondary,
    secondaryContainer = RanchoCactusContainer,
    onSecondaryContainer = RanchoCactusOnContainer,
    tertiary = RanchoTerracottaTertiary,
    tertiaryContainer = RanchoTerracottaContainer,
    background = RanchoSunBackground,
    surface = RanchoWoodSurface,
    surfaceVariant = RanchoSandSurfaceVariant
)

@Composable
fun RanchoTheme(
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
