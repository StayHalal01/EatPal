package com.example.eatpal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatpal.presentation.ui.dialogs.AddChoiceDialog
import com.example.eatpal.presentation.ui.screens.*
import com.example.eatpal.presentation.viewmodel.AuthViewModel
import com.example.eatpal.presentation.viewmodel.CaloriesTrackerViewModel
import com.example.eatpal.ui.theme.EatPalTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatPalTheme {
                val authViewModel: AuthViewModel = viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()
                val authState by authViewModel.authState.collectAsState()

                var showRegisterScreen by remember { mutableStateOf(false) }
                var showWelcomeScreen by remember { mutableStateOf(true) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                // Handle auth state changes
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthViewModel.AuthState.Success -> {
                            errorMessage = null
                        }
                        is AuthViewModel.AuthState.Error -> {
                            errorMessage = (authState as AuthViewModel.AuthState.Error).message
                            Toast.makeText(
                                this@MainActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            errorMessage = null
                        }
                    }
                }

                when {
                    currentUser != null -> {
                        // User is logged in
                        CaloriesTrackerApp(
                            onLogout = {
                                authViewModel.logout()
                                showWelcomeScreen = true
                            }
                        )
                    }
                    showRegisterScreen -> {
                        RegisterScreen(
                            onRegisterClick = { name, email, password, height, weight ->
                                if (authViewModel.validateEmail(email) && authViewModel.validatePassword(password)) {
                                    authViewModel.register(name, email, password, height, weight)
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Please check your email and password format",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onLoginClick = {
                                showRegisterScreen = false
                            },
                            isLoading = authState is AuthViewModel.AuthState.Loading,
                            errorMessage = errorMessage
                        )
                    }
                    showWelcomeScreen -> {
                        WelcomeScreen(
                            onStartJourneyClick = {
                                showWelcomeScreen = false
                            }
                        )
                    }
                    else -> {
                        LoginScreen(
                            onLoginClick = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onSignUpClick = {
                                showRegisterScreen = true
                            },
                            isLoading = authState is AuthViewModel.AuthState.Loading,
                            errorMessage = errorMessage
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaloriesTrackerApp(onLogout: () -> Unit) {
    val viewModel: CaloriesTrackerViewModel = viewModel()
    var currentScreen by remember { mutableStateOf("diary") }
    var showAddFood by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }
    var showAddChoiceDialog by remember { mutableStateOf(false) }
    var selectedFoodCategory by remember { mutableStateOf("Breakfast") }

    if (showAddFood) {
        AddFoodScreen(
            onDismiss = { showAddFood = false },
            onAddFood = { food ->
                viewModel.addFoodItem(food)
                showAddFood = false
            },
            defaultCategory = selectedFoodCategory
        )
    } else if (showAddExercise) {
        AddExerciseScreen(
            onDismiss = { showAddExercise = false },
            onAddExercise = { exercise ->
                viewModel.addExercise(exercise)
                showAddExercise = false
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.White,
                    modifier = Modifier.height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomNavItem(
                            icon = Icons.Default.DateRange,
                            label = "Diary",
                            isSelected = currentScreen == "diary"
                        ) { currentScreen = "diary" }

                        FloatingActionButton(
                            onClick = { showAddChoiceDialog = true },
                            containerColor = Color(0xFF4CAF50),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                        }

                        BottomNavItem(
                            icon = Icons.Default.Person,
                            label = "Account",
                            isSelected = currentScreen == "account"
                        ) { currentScreen = "account" }
                    }
                }
            }
        ) { paddingValues ->
            when (currentScreen) {
                "diary" -> DiaryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues),
                    onAddFood = { category ->
                        selectedFoodCategory = category
                        showAddFood = true
                    },
                    onAddExercise = { showAddExercise = true }
                )
                "account" -> AccountScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues),
                    onLogout = onLogout
                )
            }

            if (showAddChoiceDialog) {
                AddChoiceDialog(
                    onDismiss = { showAddChoiceDialog = false },
                    onAddFood = {
                        showAddChoiceDialog = false
                        selectedFoodCategory = "Breakfast"
                        showAddFood = true
                    },
                    onAddExercise = {
                        showAddChoiceDialog = false
                        showAddExercise = true
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF4CAF50) else Color.Gray
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF4CAF50) else Color.Gray
        )
    }
}