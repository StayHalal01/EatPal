package com.example.eatpal.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.eatpal.presentation.ui.components.CaloriesTrackerCard
import com.example.eatpal.presentation.ui.components.DateSelector
import com.example.eatpal.presentation.ui.sections.DiarySection
import com.example.eatpal.presentation.ui.dialogs.DatePickerDialog
import com.example.eatpal.presentation.viewmodel.CaloriesTrackerViewModel

@Composable
fun DiaryScreen(
    viewModel: CaloriesTrackerViewModel,
    modifier: Modifier = Modifier,
    onAddFood: () -> Unit,
    onAddExercise: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentEntry by viewModel.currentEntry.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CaloriesTrackerCard(
                eaten = uiState.totalCaloriesEaten,
                burned = uiState.totalCaloriesBurned,
                remaining = uiState.remainingCalories,
                goal = uiState.dailyGoal
            )
        }

        item {
            DateSelector(
                currentDate = currentDate,
                onNavigateDay = viewModel::navigateDay,
                onDateClick = { showDatePicker = true }
            )
        }

        item {
            DiarySection(
                foodItems = currentEntry.foodItems,
                exercises = currentEntry.exercises,
                waterIntake = currentEntry.waterIntake,
                onAddFood = onAddFood,
                onAddExercise = onAddExercise,
                onUpdateWater = viewModel::updateWaterIntake,
                onRemoveFood = viewModel::removeFoodItem,
                onRemoveExercise = viewModel::removeExerciseItem
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            currentDate = currentDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                viewModel.setSpecificDate(selectedDate)
                showDatePicker = false
            }
        )
    }
}
