package com.vvit.smartcollege.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.theme.*

@Composable
fun VVITSegmentedTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = if (ThemeState.isDarkMode.value) Color(0xFF2563EB) else Color(0xFF1A2A49),
    unselectedColor: Color = Color.Transparent
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariantLight)
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) selectedColor else unselectedColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = Typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
