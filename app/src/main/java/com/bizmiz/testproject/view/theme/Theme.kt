package com.bizmiz.testproject.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    onSurface = DefaultBackgroundDarkColor,
    onPrimary = DefaultDarkTextColor,
    onSecondary = DefaultIconDarkColor,
    primaryContainer = DefaultContainerDarkColor,
    surfaceTint = DefaultLocationIconDarkColor,
    secondaryContainer = UnselectedTextDarkColor
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    onSurface = DefaultBackgroundLightColor,
    onPrimary = DefaultLightTextColor,
    onSecondary = DefaultIconLightColor,
    primaryContainer = DefaultContainerLightColor,
    surfaceTint = DefaultLocationIconLightColor,
    secondaryContainer = UnselectedTextLightColor
)

@Composable
fun TestProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}