package com.vvit.smartcollege.ui.teacher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vvit.smartcollege.ui.components.BottomNavItem
import com.vvit.smartcollege.ui.components.VVITBottomNavigation

@Composable
fun TeacherMainScreen(
    viewModel: TeacherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf("home") }
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    val navItems = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("schedule", "Classes", Icons.Default.DateRange),
        BottomNavItem("attendance", "Attendance", Icons.Default.CheckCircle),
        BottomNavItem("assignments", "Assignments", Icons.AutoMirrored.Filled.Assignment),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            VVITBottomNavigation(
                items = navItems,
                currentRoute = currentTab,
                onItemClick = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                "home" -> TeacherDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSection = { currentTab = it }
                )
                "schedule" -> TeacherProfileScreen(viewModel = viewModel, onLogout = onLogout)
                "attendance" -> TeacherAttendanceDeskScreen(viewModel = viewModel)
                "assignments" -> TeacherAssignmentsHubScreen(viewModel = viewModel)
                "academics" -> TeacherAcademicsDeskScreen(viewModel = viewModel)
                "notices_leave" -> TeacherNoticesLeaveScreen(viewModel = viewModel)
                "profile" -> TeacherProfileScreen(viewModel = viewModel, onLogout = onLogout)
            }
        }
    }
}
