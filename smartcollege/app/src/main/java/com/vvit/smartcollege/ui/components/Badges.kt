package com.vvit.smartcollege.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.theme.*

enum class BadgeStyle {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL,
    PRIMARY
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    style: BadgeStyle = BadgeStyle.INFO
) {
    val (bgColor, textColor, borderColor) = when (style) {
        BadgeStyle.SUCCESS -> Triple(SuccessGreenLight, SuccessGreenDark, SuccessGreen.copy(alpha = 0.4f))
        BadgeStyle.WARNING -> Triple(WarningYellowLight, WarningYellow, WarningYellow.copy(alpha = 0.4f))
        BadgeStyle.ERROR -> Triple(ErrorRedLight, ErrorRedDark, ErrorRed.copy(alpha = 0.4f))
        BadgeStyle.INFO -> Triple(InfoBlueLight, InfoBlue, InfoBlue.copy(alpha = 0.4f))
        BadgeStyle.NEUTRAL -> Triple(SurfaceLight, TextSecondary, CardBorder)
        BadgeStyle.PRIMARY -> Triple(AccentOrangeLight, AccentOrangeDark, AccentOrange.copy(alpha = 0.4f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.8.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = Typography.labelSmall.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}
