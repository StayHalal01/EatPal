package com.example.eatpal.data.repository

import com.example.eatpal.data.model.DiaryEntry
import com.example.eatpal.data.model.ExerciseItem
import com.example.eatpal.data.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaloriesRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val entriesByDate = mutableMapOf<String, DiaryEntry>()

    private val _currentDate = MutableStateFlow(Date())
    private val _currentEntry = MutableStateFlow(getOrCreateEntry(Date()))
    val currentEntry: StateFlow<DiaryEntry> = _currentEntry.asStateFlow()

    private val _dailyCalorieGoal = MutableStateFlow(2039)
    val dailyCalorieGoal: StateFlow<Int> = _dailyCalorieGoal.asStateFlow()

    private fun getOrCreateEntry(date: Date): DiaryEntry {
        val dateKey = dateFormat.format(date)
        return entriesByDate.getOrPut(dateKey) {
            DiaryEntry(date = date)
        }
    }

    private fun updateCurrentEntry(updater: (DiaryEntry) -> DiaryEntry) {
        val currentDate = _currentDate.value
        val dateKey = dateFormat.format(currentDate)
        val updatedEntry = updater(_currentEntry.value)
        entriesByDate[dateKey] = updatedEntry
        _currentEntry.value = updatedEntry
    }

    fun addFoodItem(food: FoodItem) {
        updateCurrentEntry { entry ->
            entry.copy(foodItems = entry.foodItems + food)
        }
    }

    fun removeFoodItem(foodId: String) {
        updateCurrentEntry { entry ->
            entry.copy(foodItems = entry.foodItems.filter { it.id != foodId })
        }
    }

    fun addExercise(exercise: ExerciseItem) {
        updateCurrentEntry { entry ->
            entry.copy(exercises = entry.exercises + exercise)
        }
    }

    fun removeExerciseItem(exerciseId: String) {
        updateCurrentEntry { entry ->
            entry.copy(exercises = entry.exercises.filter { it.id != exerciseId })
        }
    }

    fun updateWaterIntake(amount: Int) {
        updateCurrentEntry { entry ->
            entry.copy(waterIntake = amount.coerceAtLeast(0))
        }
    }

    fun updateEntryDate(date: Date) {
        _currentDate.value = date
        _currentEntry.value = getOrCreateEntry(date)
    }

    fun updateDailyCalorieGoal(goal: Int) {
        _dailyCalorieGoal.value = goal
    }
}
