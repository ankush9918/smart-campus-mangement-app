package com.vvit.smartcollege.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.theme.*

@Composable
fun AttendanceGaugeCard(
    percentage: Int,
    modifier: Modifier = Modifier,
    statusText: String = "Satisfactory Margin",
    descriptionText: String = "Your performance is safely above the required university norm of 75%.",
    periodText: String = "October 2026"
) {
    val progress = (percentage.toFloat() / 100f).coerceIn(0f, 1f)
    val gaugeColor = when {
        percentage >= 75 -> SuccessGreen
        percentage >= 65 -> WarningYellow
        else -> ErrorRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = CardBorder,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = gaugeColor,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Status",
                        style = Typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                    Text(
                        text = periodText,
                        style = Typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = statusText,
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = descriptionText,
                    style = Typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    items: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    maxVal: Float = 10f,
    barColor: Color = PrimaryNavy
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { (label, value) ->
                val barFraction = (value / maxVal).coerceIn(0.1f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = String.format("%.1f", value),
                        style = Typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .fillMaxHeight(barFraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = Typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LinearProgressWithLabels(
    title: String,
    percentage: Int,
    modifier: Modifier = Modifier,
    targetLabel: String? = null,
    statusBadge: String? = null,
    isAlert: Boolean = false
) {
    val progress = (percentage.toFloat() / 100f).coerceIn(0f, 1f)
    val color = if (isAlert) ErrorRed else SuccessGreen

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = Typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            )
            Text(
                text = "$percentage%",
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 13.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CardBorder,
            strokeCap = StrokeCap.Round
        )

        if (targetLabel != null || statusBadge != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (targetLabel != null) {
                    Text(
                        text = targetLabel,
                        style = Typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
                if (statusBadge != null) {
                    Text(
                        text = statusBadge,
                        style = Typography.labelSmall.copy(
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
