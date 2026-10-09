import os

base_dir = "app/src/main/java/com/vvit/smartcollege/ui"
os.makedirs(f"{base_dir}/theme", exist_ok=True)
os.makedirs(f"{base_dir}/components", exist_ok=True)

color_kt = """package com.vvit.smartcollege.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryNavy = Color(0xFF1A2A49)
val PrimaryNavyDark = Color(0xFF101B30)
val PrimaryNavyLight = Color(0xFF273D68)

val AccentOrange = Color(0xFFE8721A)
val AccentOrangeLight = Color(0xFFFFF4EC)
val AccentOrangeDark = Color(0xFFC75B0F)

val SuccessGreen = Color(0xFF2D8A56)
val SuccessGreenLight = Color(0xFFEBF8F1)
val SuccessGreenDark = Color(0xFF1F633E)

val ErrorRed = Color(0xFFD8524F)
val ErrorRedLight = Color(0xFFFDEEEC)
val ErrorRedDark = Color(0xFFA83633)

val WarningYellow = Color(0xFFE59400)
val WarningYellowLight = Color(0xFFFFF8E6)

val InfoBlue = Color(0xFF2B78E4)
val InfoBlueLight = Color(0xFFEBF3FC)

val SurfaceLight = Color(0xFFF4F6F9)
val SurfaceVariantLight = Color(0xFFECEFF4)
val CardWhite = Color(0xFFFFFFFF)
val CardBorder = Color(0xFFE2E8EF)

val TextPrimary = Color(0xFF1A2A49)
val TextSecondary = Color(0xFF708095)
val TextMuted = Color(0xFF9AA7B6)
val TextLight = Color(0xFFFFFFFF)

val DividerColor = Color(0xFFE9EEF3)
"""

type_kt = """package com.vvit.smartcollege.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = TextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
"""

shape_kt = """package com.vvit.smartcollege.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
"""

theme_kt = """package com.vvit.smartcollege.ui.theme

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
"""

with open(f"{base_dir}/theme/Color.kt", "w", encoding="utf-8") as f:
    f.write(color_kt)
with open(f"{base_dir}/theme/Type.kt", "w", encoding="utf-8") as f:
    f.write(type_kt)
with open(f"{base_dir}/theme/Shape.kt", "w", encoding="utf-8") as f:
    f.write(shape_kt)
with open(f"{base_dir}/theme/Theme.kt", "w", encoding="utf-8") as f:
    f.write(theme_kt)

print("Theme files created.")
