package com.example.eatpal.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatpal.presentation.viewmodel.CaloriesTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: CaloriesTrackerViewModel,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditCaloriesDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF8F9FA)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                // Header
                Text(
                    text = "Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Profile Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Picture
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF4CAF50),
                                            Color(0xFF66BB6A)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "John Doe",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        Text(
                            text = "john.doe@example.com",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickStatItem(
                                title = "Goal",
                                value = "${uiState.dailyGoal}",
                                unit = "kcal",
                                color = Color(0xFF4CAF50)
                            )
                            QuickStatItem(
                                title = "Today",
                                value = "${uiState.totalCaloriesEaten}",
                                unit = "kcal",
                                color = Color(0xFF2196F3)
                            )
                            QuickStatItem(
                                title = "Burned",
                                value = "${uiState.totalCaloriesBurned}",
                                unit = "kcal",
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Goals & Settings Section
                SectionCard(
                    title = "Goals & Settings",
                    icon = Icons.Default.Settings
                ) {
                    AccountSettingItem(
                        icon = Icons.Default.Star,
                        title = "Daily Calorie Goal",
                        subtitle = "${uiState.dailyGoal} kcal",
                        onClick = { showEditCaloriesDialog = true },
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Weight Goal",
                        subtitle = "Maintain current weight",
                        onClick = { /* TODO */ },
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.Person,
                        title = "Activity Level",
                        subtitle = "Moderately active",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Settings Section
                SectionCard(
                    title = "App Settings",
                    icon = Icons.Default.Settings
                ) {
                    AccountSettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Daily reminders enabled",
                        onClick = { /* TODO */ },
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.Settings,
                        title = "Theme",
                        subtitle = "Light mode",
                        onClick = { /* TODO */ },
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.Settings,
                        title = "Language",
                        subtitle = "English",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Data & Privacy Section
                SectionCard(
                    title = "Data & Privacy",
                    icon = Icons.Default.Lock
                ) {
                    AccountSettingItem(
                        icon = Icons.Default.Add,
                        title = "Export Data",
                        subtitle = "Download your data",
                        onClick = { /* TODO */ },
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.Delete,
                        title = "Clear Data",
                        subtitle = "Reset all progress",
                        onClick = { /* TODO */ },
                        textColor = Color(0xFFFF5722),
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.Lock,
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About Section
                SectionCard(
                    title = "About",
                    icon = Icons.Default.Info
                ) {
                    AccountSettingItem(
                        icon = Icons.Default.Info,
                        title = "Help & Support",
                        subtitle = "Get help and contact us",
                        onClick = { /* TODO */ },
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.Star,
                        title = "Rate EatPal",
                        subtitle = "Share your feedback",
                        onClick = { /* TODO */ },
                        showDivider = true
                    )

                    AccountSettingItem(
                        icon = Icons.Default.Info,
                        title = "About EatPal",
                        subtitle = "Version 1.0.0",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign Out Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit Calories Dialog
    if (showEditCaloriesDialog) {
        EditCaloriesDialog(
            currentGoal = uiState.dailyGoal,
            onDismiss = { showEditCaloriesDialog = false },
            onSave = { newGoal ->
                viewModel.updateDailyCalorieGoal(newGoal)
                showEditCaloriesDialog = false
            }
        )
    }
}

@Composable
fun QuickStatItem(
    title: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            content()
        }
    }
}

@Composable
fun AccountSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = Color(0xFF1A1A1A),
    showDivider: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (textColor == Color(0xFFFF5722)) textColor else Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color(0xFF999999),
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = Color(0xFFF0F0F0),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun EditCaloriesDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var calorieGoal by remember { mutableStateOf(currentGoal.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Daily Calorie Goal",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Set your daily calorie target to help you reach your health goals.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = calorieGoal,
                    onValueChange = {
                        calorieGoal = it
                        isError = it.toIntOrNull() == null || it.toIntOrNull()!! <= 0
                    },
                    label = { Text("Daily Calorie Goal") },
                    suffix = { Text("kcal", color = Color(0xFF666666)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isError) Color(0xFFFF5722) else Color(0xFF4CAF50),
                        unfocusedBorderColor = if (isError) Color(0xFFFF5722) else Color(0xFFCCCCCC)
                    ),
                    isError = isError
                )

                if (isError) {
                    Text(
                        text = "Please enter a valid number greater than 0",
                        color = Color(0xFFFF5722),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newGoal = calorieGoal.toIntOrNull()
                    if (newGoal != null && newGoal > 0) {
                        onSave(newGoal)
                    }
                },
                enabled = !isError && calorieGoal.isNotBlank()
            ) {
                Text(
                    text = "Save",
                    color = if (!isError && calorieGoal.isNotBlank()) Color(0xFF4CAF50) else Color(
                        0xFFCCCCCC
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF666666)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
