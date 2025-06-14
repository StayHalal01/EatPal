package com.example.eatpal.data.repository

import com.example.eatpal.data.model.DiaryEntry
import com.example.eatpal.data.model.ExerciseItem
import com.example.eatpal.data.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class CaloriesRepository {
    private val _currentEntry = MutableStateFlow(DiaryEntry(date = Date()))
    val currentEntry: StateFlow<DiaryEntry> = _currentEntry.asStateFlow()

    private val _dailyCalorieGoal = MutableStateFlow(2039)
    val dailyCalorieGoal: StateFlow<Int> = _dailyCalorieGoal.asStateFlow()

    fun addFoodItem(food: FoodItem) {
        _currentEntry.value = _currentEntry.value.copy(
            foodItems = _currentEntry.value.foodItems + food
        )
    }

    fun removeFoodItem(foodId: String) {
        _currentEntry.value = _currentEntry.value.copy(
            foodItems = _currentEntry.value.foodItems.filter { it.id != foodId }
        )
    }

    fun addExercise(exercise: ExerciseItem) {
        _currentEntry.value = _currentEntry.value.copy(
            exercises = _currentEntry.value.exercises + exercise
        )
    }

    fun removeExerciseItem(exerciseId: String) {
        _currentEntry.value = _currentEntry.value.copy(
            exercises = _currentEntry.value.exercises.filter { it.id != exerciseId }
        )
    }

    fun updateWaterIntake(amount: Int) {
        _currentEntry.value = _currentEntry.value.copy(
            waterIntake = amount.coerceAtLeast(0)
        )
    }

    fun updateEntryDate(date: Date) {
        _currentEntry.value = _currentEntry.value.copy(date = date)
    }

    fun updateDailyCalorieGoal(goal: Int) {
        _dailyCalorieGoal.value = goal
    }
}
