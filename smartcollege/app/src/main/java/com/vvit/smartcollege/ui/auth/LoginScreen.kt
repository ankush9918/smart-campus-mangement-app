package com.vvit.smartcollege.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.vvit.smartcollege.ui.theme.ThemeState
import com.vvit.smartcollege.ui.theme.Typography

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val systemDark = isSystemInDarkTheme()
    DisposableEffect(systemDark) {
        ThemeState.isDarkMode.value = systemDark
        onDispose {}
    }
    val isDark = ThemeState.isDarkMode.value

    // Dynamic Palette adapting smoothly to Light / Dark Mode
    val screenBg = if (isDark) Color(0xFF0B132B) else Color(0xFFF4F6F9)
    val cardBg = if (isDark) Color(0xFF131D31) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1E2D4A) else Color(0xFFE2E8EF)
    val inputBg = if (isDark) Color(0xFF0D1527) else Color(0xFFFFFFFF)
    val inputBorder = if (isDark) Color(0xFF1E2D4A) else Color(0xFFCBD5E1)
    val inputFocusedBorder = if (isDark) Color(0xFF2563EB) else Color(0xFF1A2A49)
    val primaryText = if (isDark) Color.White else Color(0xFF1A2A49)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF708095)
    val roleSelectorBg = if (isDark) Color(0xFF0D1527) else Color(0xFFECEFF4)
    val roleSelectedBg = if (isDark) Color(0xFF2563EB) else Color(0xFF1A2A49)
    val actionButtonBg = if (isDark) Color(0xFF2563EB) else Color(0xFF1A2A49)
    val fieldLabelColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1A2A49)
    val contactTextColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1A2A49)

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
            .background(screenBg)
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
            // --- COLLEGE LOGO ---
            Image(
                painter = painterResource(id = R.drawable.college_logo),
                contentDescription = "College Logo",
                modifier = Modifier
                    .size(86.dp)
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(14.dp))

            // --- COLLEGE NAME ---
            Text(
                text = "Lokmata Devi Ahilya Bai Holkar Rajkiya Engineering College, Mainpuri",
                style = Typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // --- SUBTITLE ---
            Text(
                text = "Smart Campus Portal",
                style = Typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN LOGIN CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // Card Title: Sign In
                    Text(
                        text = "Sign In",
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            fontSize = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Select your role and enter your college credentials",
                        style = Typography.bodySmall.copy(
                            color = secondaryText,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role Selector: Student | Teacher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(roleSelectorBg)
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
                                    .background(if (isSelected) roleSelectedBg else Color.Transparent)
                                    .clickable(enabled = !isLoading) {
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
                                        tint = if (isSelected) Color.White else secondaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        style = Typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else secondaryText,
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "College Email ID",
                            style = Typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = fieldLabelColor,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = secondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBg,
                                unfocusedContainerColor = inputBg,
                                disabledContainerColor = inputBg,
                                focusedBorderColor = inputFocusedBorder,
                                unfocusedBorderColor = inputBorder,
                                errorBorderColor = Color(0xFFD8524F),
                                focusedTextColor = primaryText,
                                unfocusedTextColor = primaryText,
                                cursorColor = inputFocusedBorder
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Password",
                            style = Typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = fieldLabelColor,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = secondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) "Hide password" else "Show password",
                                        tint = secondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = inputBg,
                                unfocusedContainerColor = inputBg,
                                disabledContainerColor = inputBg,
                                focusedBorderColor = inputFocusedBorder,
                                unfocusedBorderColor = inputBorder,
                                errorBorderColor = Color(0xFFD8524F),
                                focusedTextColor = primaryText,
                                unfocusedTextColor = primaryText,
                                cursorColor = inputFocusedBorder
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (!isLoading) {
                                        viewModel.login(onLoginSuccess)
                                    }
                                }
                            )
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = Typography.bodySmall.copy(color = Color(0xFFF87171), fontSize = 12.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign In Button
                    Button(
                        onClick = { viewModel.login(onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionButtonBg,
                            contentColor = Color.White,
                            disabledContainerColor = if (isDark) Color(0xFF1E2D4A) else Color(0xFFCBD5E1),
                            disabledContentColor = secondaryText
                        )
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Signing in...",
                                    style = Typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = "Sign In as ${roleTabs[selectedRoleIndex]}",
                                style = Typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
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
                        color = secondaryText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "helpdesk@recmainpuri.ac.in | Ext: 404",
                    style = Typography.labelSmall.copy(
                        color = contactTextColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
