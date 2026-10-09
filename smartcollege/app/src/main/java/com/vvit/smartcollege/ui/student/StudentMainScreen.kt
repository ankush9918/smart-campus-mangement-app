package com.vvit.smartcollege.ui.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vvit.smartcollege.ui.components.BottomNavItem
import com.vvit.smartcollege.ui.components.VVITBottomNavigation

@Composable
fun StudentMainScreen(
    viewModel: StudentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
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
        BottomNavItem("attendance", "Attendance", Icons.Default.CheckCircle),
        BottomNavItem("timetable", "Timetable", Icons.Default.DateRange),
        BottomNavItem("notices_fees", "Notices", Icons.Default.Notifications),
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
                "home" -> StudentDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSection = { currentTab = it }
                )
                "attendance" -> StudentAttendanceScreen(viewModel = viewModel)
                "timetable" -> StudentTimetableScreen(viewModel = viewModel)
                "academics" -> StudentAcademicsScreen(viewModel = viewModel)
                "results" -> StudentResultsScreen(viewModel = viewModel)
                "notices_fees" -> StudentNoticesFeesScreen(viewModel = viewModel)
                "profile" -> StudentProfileScreen(
                    viewModel = viewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
