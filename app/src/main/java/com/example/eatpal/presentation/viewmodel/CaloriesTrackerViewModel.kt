package com.example.eatpal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatpal.data.model.DiaryEntry
import com.example.eatpal.data.model.ExerciseItem
import com.example.eatpal.data.model.FoodItem
import com.example.eatpal.data.repository.CaloriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class CaloriesTrackerViewModel(
    private val repository: CaloriesRepository = CaloriesRepository()
) : ViewModel() {

    private val _currentDate = MutableStateFlow(Date())
    val currentDate: StateFlow<Date> = _currentDate.asStateFlow()

    val currentEntry: StateFlow<DiaryEntry> = repository.currentEntry
    val dailyCalorieGoal: StateFlow<Int> = repository.dailyCalorieGoal

    private val _uiState = MutableStateFlow(CaloriesTrackerUiState())
    val uiState: StateFlow<CaloriesTrackerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                currentEntry,
                dailyCalorieGoal
            ) { entry, goal ->
                val totalCaloriesEaten = entry.foodItems.sumOf { it.calories }
                val totalCaloriesBurned = entry.exercises.sumOf { it.caloriesBurned }
                val remainingCalories = goal - totalCaloriesEaten + totalCaloriesBurned

                CaloriesTrackerUiState(
                    totalCaloriesEaten = totalCaloriesEaten,
                    totalCaloriesBurned = totalCaloriesBurned,
                    remainingCalories = remainingCalories,
                    dailyGoal = goal
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addFoodItem(food: FoodItem) {
        repository.addFoodItem(food)
    }

    fun removeFoodItem(foodId: String) {
        repository.removeFoodItem(foodId)
    }

    fun addExercise(exercise: ExerciseItem) {
        repository.addExercise(exercise)
    }

    fun updateWaterIntake(amount: Int) {
        repository.updateWaterIntake(amount)
    }

    fun navigateDay(forward: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value
        calendar.add(Calendar.DAY_OF_MONTH, if (forward) 1 else -1)
        val newDate = calendar.time
        _currentDate.value = newDate
        repository.updateEntryDate(newDate)
    }
}

data class CaloriesTrackerUiState(
    val totalCaloriesEaten: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val remainingCalories: Int = 0,
    val dailyGoal: Int = 2039
)