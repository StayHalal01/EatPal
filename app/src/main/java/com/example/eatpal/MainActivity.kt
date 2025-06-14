// MainActivity.kt
package com.example.eatpal

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatpal.presentation.ui.dialogs.AddExerciseDialog
import com.example.eatpal.presentation.ui.dialogs.AddFoodDialog
import com.example.eatpal.presentation.ui.screens.DiaryScreen
import com.example.eatpal.presentation.viewmodel.CaloriesTrackerViewModel
import com.example.eatpal.ui.theme.EatPalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatPalTheme {
                CaloriesTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaloriesTrackerApp() {
    val viewModel: CaloriesTrackerViewModel = viewModel()
    var currentScreen by remember { mutableStateOf("diary") }
    var showAddFood by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }

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
                        onClick = { showAddFood = true },
                        containerColor = Color(0xFF4CAF50),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Food", tint = Color.White)
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
                onAddFood = { showAddFood = true },
                onAddExercise = { showAddExercise = true }
            )
        }

        if (showAddFood) {
            AddFoodDialog(
                onDismiss = { showAddFood = false },
                onAddFood = { food ->
                    viewModel.addFoodItem(food)
                    showAddFood = false
                }
            )
        }

        if (showAddExercise) {
            AddExerciseDialog(
                onDismiss = { showAddExercise = false },
                onAddExercise = { exercise ->
                    viewModel.addExercise(exercise)
                    showAddExercise = false
                }
            )
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
