package com.vvit.smartcollege.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = CardWhite,
    primaryContainer = PrimaryNavyLight,
    onPrimaryContainer = CardWhite,
    secondary = AccentOrange,
    onSecondary = CardWhite,
    secondaryContainer = AccentOrangeLight,
    onSecondaryContainer = AccentOrangeDark,
    tertiary = SuccessGreen,
    onTertiary = CardWhite,
    tertiaryContainer = SuccessGreenLight,
    onTertiaryContainer = SuccessGreenDark,
    error = ErrorRed,
    onError = CardWhite,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun SmartCollegeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
