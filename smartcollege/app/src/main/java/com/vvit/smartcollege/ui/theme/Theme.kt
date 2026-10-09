package com.vvit.smartcollege.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A2A49),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF273D68),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFE8721A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFF4EC),
    onSecondaryContainer = Color(0xFFC75B0F),
    tertiary = Color(0xFF2D8A56),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEBF8F1),
    onTertiaryContainer = Color(0xFF1F633E),
    error = Color(0xFFD8524F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFDEEEC),
    onErrorContainer = Color(0xFFA83633),
    background = Color(0xFFF4F6F9),
    onBackground = Color(0xFF1A2A49),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A2A49),
    surfaceVariant = Color(0xFFECEFF4),
    onSurfaceVariant = Color(0xFF708095),
    outline = Color(0xFFE2E8EF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFFE8721A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2E1F18),
    onSecondaryContainer = Color(0xFFE8721A),
    tertiary = Color(0xFF2D8A56),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF142E20),
    onTertiaryContainer = Color(0xFF2D8A56),
    error = Color(0xFFD8524F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF331A1A),
    onErrorContainer = Color(0xFFD8524F),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF243248),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155)
)

@Composable
fun SmartCollegeTheme(
    darkTheme: Boolean = ThemeState.isDarkMode.value,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
