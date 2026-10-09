package com.vvit.smartcollege.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.ui.components.*
import com.vvit.smartcollege.ui.theme.*

@Composable
fun StudentNoticesFeesScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier
) {
    val notices by viewModel.notices.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "All Notices",
            subtitle = "Campus communications & official circulars"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "All Notices & Circulars (${notices.size})")
            }

            items(notices) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(text = item.category, style = if (item.isUrgent) BadgeStyle.ERROR else BadgeStyle.PRIMARY)
                            Text(text = item.timeAgo, style = Typography.labelSmall.copy(color = TextSecondary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
