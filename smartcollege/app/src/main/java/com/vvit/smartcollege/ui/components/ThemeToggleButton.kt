package com.vvit.smartcollege.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.theme.*

@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val context = LocalContext.current
    val isDark = ThemeState.isDarkMode.value

    val buttonBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFF334155) else SurfaceVariantLight,
        animationSpec = tween(250),
        label = "btnBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isDark) AccentOrange else PrimaryNavy,
        animationSpec = tween(250),
        label = "iconColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDark) AccentOrange.copy(alpha = 0.6f) else CardBorder,
        animationSpec = tween(250),
        label = "border"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = buttonBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                ThemeState.toggleDarkMode(context)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (isDark) "Switch to Light Theme" else "Switch to Dark Theme",
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            if (!compact) {
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isDark) "Dark" else "Light",
                    style = Typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFF1F5F9) else TextPrimary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
