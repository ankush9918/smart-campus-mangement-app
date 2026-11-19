package com.vvit.smartcollege.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
fun TeacherNoticesLeaveScreen(
    viewModel: TeacherViewModel,
    modifier: Modifier = Modifier
) {
    val notices by viewModel.notices.collectAsState()
    var noticeTitle by remember { mutableStateOf("") }
    var noticeDescription by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        VVITHeader(
            title = "Announcements Desk",
            subtitle = "Broadcast circulars & class updates"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "Publish New Announcement")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        VVITTextField(
                            value = noticeTitle,
                            onValueChange = { noticeTitle = it },
                            label = "Notice Title"
                        )

                        VVITTextField(
                            value = noticeDescription,
                            onValueChange = { noticeDescription = it },
                            label = "Announcement Details",
                            singleLine = false,
                            maxLines = 4
                        )

                        VVITPrimaryButton(
                            text = "Broadcast Notice",
                            onClick = {
                                if (noticeTitle.isNotBlank()) {
                                    viewModel.publishNotice(noticeTitle, noticeDescription)
                                    noticeTitle = ""
                                    noticeDescription = ""
                                }
                            }
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "Published Notices")
            }

            items(notices) { notice ->
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
                            StatusBadge(text = notice.category, style = if (notice.isUrgent) BadgeStyle.ERROR else BadgeStyle.PRIMARY)
                            Text(text = notice.timeAgo, style = Typography.labelSmall.copy(color = TextSecondary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notice.title,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryNavy)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notice.displayDescription,
                            style = Typography.bodySmall.copy(color = TextSecondary)
                        )
                        if (notice.publishedBy.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimaryNavy,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "By: ${notice.publishedBy}",
                                    style = Typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryNavy,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
