package com.vvit.smartcollege.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vvit.smartcollege.data.model.UserRole
import com.vvit.smartcollege.ui.auth.AuthViewModel
import com.vvit.smartcollege.ui.auth.LoginScreen
import com.vvit.smartcollege.ui.student.StudentMainScreen
import com.vvit.smartcollege.ui.teacher.TeacherMainScreen

@Composable
fun RoleNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    when (role) {
                        UserRole.STUDENT, UserRole.ADMIN -> navController.navigate(Screen.StudentRoot.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        UserRole.TEACHER -> navController.navigate(Screen.TeacherRoot.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.StudentRoot.route) {
            StudentMainScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TeacherRoot.route) {
            TeacherMainScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
