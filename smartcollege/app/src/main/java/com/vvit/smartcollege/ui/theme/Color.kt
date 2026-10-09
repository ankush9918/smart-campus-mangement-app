package com.vvit.smartcollege.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary Colors: In Light mode, PrimaryNavy is deep dark navy (0xFF1A2A49).
// In Dark mode, when used as text/title/icon/tint color, it is bright and high contrast (0xFFF1F5F9)!
val PrimaryNavy: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFFF1F5F9) else Color(0xFF1A2A49)

val PrimaryNavyDark: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFFE2E8F0) else Color(0xFF101B30)

val PrimaryNavyLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF2563EB) else Color(0xFF273D68)

val TopBarBackground: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF1E293B) else Color(0xFF1A2A49)

// Accent Colors
val AccentOrange = Color(0xFFE8721A)
val AccentOrangeLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF3B2416) else Color(0xFFFFF4EC)

val AccentOrangeDark: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFFFB923C) else Color(0xFFC75B0F)

// Success Colors
val SuccessGreen = Color(0xFF2D8A56)
val SuccessGreenLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF142E20) else Color(0xFFEBF8F1)

val SuccessGreenDark: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF4ADE80) else Color(0xFF1F633E)

// Error Colors
val ErrorRed = Color(0xFFD8524F)
val ErrorRedLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF381818) else Color(0xFFFDEEEC)

val ErrorRedDark: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFFF87171) else Color(0xFFA83633)

// Warning Colors
val WarningYellow = Color(0xFFE59400)
val WarningYellowLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF382C10) else Color(0xFFFFF8E6)

// Info Colors
val InfoBlue = Color(0xFF2B78E4)
val InfoBlueLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF162746) else Color(0xFFEBF3FC)

// Surface & Card Colors
val SurfaceLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF0F172A) else Color(0xFFF4F6F9)

val SurfaceVariantLight: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF243248) else Color(0xFFECEFF4)

val CardWhite: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF1E293B) else Color(0xFFFFFFFF)

val CardBorder: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF334155) else Color(0xFFE2E8EF)

// Text Colors
val TextPrimary: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFFF8FAFC) else Color(0xFF1A2A49)

val TextSecondary: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF94A3B8) else Color(0xFF708095)

val TextMuted: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF64748B) else Color(0xFF9AA7B6)

val TextLight = Color(0xFFFFFFFF)

val DividerColor: Color
    @Composable
    get() = if (ThemeState.isDarkMode.value) Color(0xFF334155) else Color(0xFFE9EEF3)
