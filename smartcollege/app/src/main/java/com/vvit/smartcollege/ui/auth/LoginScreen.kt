package com.vvit.smartcollege.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vvit.smartcollege.R
import com.vvit.smartcollege.data.model.UserRole
import com.vvit.smartcollege.ui.components.VVITPrimaryButton
import com.vvit.smartcollege.ui.components.VVITTextField
import com.vvit.smartcollege.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedRole by viewModel.selectedRole.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showPassword by remember { mutableStateOf(false) }

    val roleTabs = listOf("Student", "Teacher")
    val selectedRoleIndex = when (selectedRole) {
        UserRole.STUDENT -> 0
        UserRole.TEACHER -> 1
        UserRole.ADMIN -> 0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- HEADER / INSTITUTIONAL BRANDING ---
            Image(
                painter = painterResource(id = R.drawable.college_logo),
                contentDescription = "College Logo",
                modifier = Modifier
                    .size(84.dp)
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Lokmata Devi Ahilya Bai Holkar Rajkiya Engineering College, Mainpuri",
                style = Typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Smart Campus Portal",
                style = Typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN LOGIN CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // Card Title
                    Text(
                        text = "Sign In",
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            fontSize = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Select your role and enter your college credentials",
                        style = Typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role Selector: Student | Teacher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariantLight)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        roleTabs.forEachIndexed { index, title ->
                            val isSelected = index == selectedRoleIndex
                            val roleIcon = if (index == 0) Icons.Default.School else Icons.Default.Person

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryNavy else Color.Transparent)
                                    .clickable {
                                        when (index) {
                                            0 -> viewModel.selectRole(UserRole.STUDENT)
                                            1 -> viewModel.selectRole(UserRole.TEACHER)
                                        }
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = roleIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) CardWhite else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        style = Typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CardWhite else TextSecondary,
                                            fontSize = 13.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // College Email ID Field
                    VVITTextField(
                        value = email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = "College Email ID",
                        placeholder = "",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    VVITTextField(
                        value = password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = "Password",
                        placeholder = "",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.login(onLoginSuccess) }
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = Typography.bodySmall.copy(color = ErrorRed, fontSize = 12.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dynamic Sign In Button
                    VVITPrimaryButton(
                        text = if (isLoading) "Signing in..." else "Sign In as ${roleTabs[selectedRoleIndex]}",
                        onClick = { viewModel.login(onLoginSuccess) },
                        enabled = !isLoading,
                        containerColor = PrimaryNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- IT HELPDESK FOOTER ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Need help accessing your account? Contact IT Desk",
                    style = Typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "helpdesk@recmainpuri.ac.in | Ext: 404",
                    style = Typography.labelSmall.copy(
                        color = PrimaryNavy,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
