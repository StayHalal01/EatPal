package com.example.eatpal.data.repository

import android.util.Log
import com.example.eatpal.data.model.DiaryEntry
import com.example.eatpal.data.model.ExerciseItem
import com.example.eatpal.data.model.FoodItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CaloriesRepository {
    private val TAG = "CaloriesRepository"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val entriesByDate = mutableMapOf<String, DiaryEntry>()

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentDate = MutableStateFlow(Date())
    private val _currentEntry = MutableStateFlow(getOrCreateEntry(Date()))
    val currentEntry: StateFlow<DiaryEntry> = _currentEntry.asStateFlow()

    private val _dailyCalorieGoal = MutableStateFlow(2039)
    val dailyCalorieGoal: StateFlow<Int> = _dailyCalorieGoal.asStateFlow()

    init {
        // Load user's calorie goal from Firestore when repository is created
        loadUserCalorieGoal()
    }

    private fun loadUserCalorieGoal() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val calorieGoal = document.getLong("dailyCalorieGoal")?.toInt()
                    if (calorieGoal != null) {
                        _dailyCalorieGoal.value = calorieGoal
                        Log.d(TAG, "Loaded calorie goal: $calorieGoal")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user data", e)
            }
    }

    private fun getOrCreateEntry(date: Date): DiaryEntry {
        val dateKey = dateFormat.format(date)
        val entry = entriesByDate.getOrPut(dateKey) {
            DiaryEntry(date = date)
        }

        // Try to load from Firebase if we have a user and the entry isn't already loaded
        loadEntryFromFirebase(date)

        return entry
    }

    private fun loadEntryFromFirebase(date: Date) {
        val userId = auth.currentUser?.uid ?: return
        val dateKey = dateFormat.format(date)

        db.collection("users")
            .document(userId)
            .collection("diaryEntries")
            .document(dateKey)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                        // Parse food items
                        val foodItemsList = mutableListOf<FoodItem>()
                        val foodItems = document.get("foodItems") as? List<Map<String, Any>>
                        foodItems?.forEach { foodData ->
                            try {
                                val foodItem = FoodItem(
                                    id = foodData["id"] as? String ?: UUID.randomUUID().toString(),
                                    name = foodData["name"] as? String ?: "",
                                    calories = (foodData["calories"] as? Number)?.toInt() ?: 0,
                                    category = foodData["category"] as? String ?: "",
                                    servingSize = foodData["servingSize"] as? String ?: "",
                                    amount = (foodData["amount"] as? Number)?.toDouble() ?: 1.0
                                )
                                foodItemsList.add(foodItem)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing food item", e)
                            }
                        }

                        // Parse exercise items
                        val exercisesList = mutableListOf<ExerciseItem>()
                        val exercises = document.get("exercises") as? List<Map<String, Any>>
                        exercises?.forEach { exerciseData ->
                            try {
                                val exerciseItem = ExerciseItem(
                                    id = exerciseData["id"] as? String ?: UUID.randomUUID().toString(),
                                    name = exerciseData["name"] as? String ?: "",
                                    duration = (exerciseData["duration"] as? Number)?.toInt() ?: 0,
                                    caloriesBurned = (exerciseData["caloriesBurned"] as? Number)?.toInt() ?: 0
                                )
                                exercisesList.add(exerciseItem)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing exercise item", e)
                            }
                        }

                        // Get water intake
                        val waterIntake = document.getLong("waterIntake")?.toInt() ?: 0

                        // Create and store the entry
                        val entry = DiaryEntry(
                            date = date,
                            foodItems = foodItemsList,
                            exercises = exercisesList,
                            waterIntake = waterIntake
                        )

                        entriesByDate[dateKey] = entry

                        // Update current entry if this is for the current date
                        if (dateKey == dateFormat.format(_currentDate.value)) {
                            _currentEntry.value = entry
                        }

                        Log.d(TAG, "Loaded entry for date $dateKey: ${entry.foodItems.size} foods, ${entry.exercises.size} exercises")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing diary entry", e)
                    }
                } else {
                    Log.d(TAG, "No entry found for date $dateKey")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading diary entry", e)
            }
    }

    private fun updateCurrentEntry(updater: (DiaryEntry) -> DiaryEntry) {
        val currentDate = _currentDate.value
        val dateKey = dateFormat.format(currentDate)
        val updatedEntry = updater(_currentEntry.value)
        entriesByDate[dateKey] = updatedEntry
        _currentEntry.value = updatedEntry

        // Save to Firebase
        saveEntryToFirebase(updatedEntry)
    }

    private fun saveEntryToFirebase(entry: DiaryEntry) {
        val userId = auth.currentUser?.uid ?: return
        val dateKey = dateFormat.format(entry.date)

        // Convert food items to maps
        val foodItems = entry.foodItems.map { food ->
            mapOf(
                "id" to food.id,
                "name" to food.name,
                "calories" to food.calories,
                "category" to food.category,
                "servingSize" to food.servingSize,
                "amount" to food.amount
            )
        }

        // Convert exercises to maps
        val exercises = entry.exercises.map { exercise ->
            mapOf(
                "id" to exercise.id,
                "name" to exercise.name,
                "duration" to exercise.duration,
                "caloriesBurned" to exercise.caloriesBurned
            )
        }

        // Create entry document
        val entryData = hashMapOf(
            "date" to dateKey,
            "foodItems" to foodItems,
            "exercises" to exercises,
            "waterIntake" to entry.waterIntake,
            "lastUpdated" to FieldValue.serverTimestamp()
        )

        // Save to Firestore
        db.collection("users")
            .document(userId)
            .collection("diaryEntries")
            .document(dateKey)
            .set(entryData)
            .addOnSuccessListener {
                Log.d(TAG, "Entry saved successfully for date $dateKey")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving entry for date $dateKey", e)
            }
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

        // Save to user's document in Firestore
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .update("dailyCalorieGoal", goal)
            .addOnSuccessListener {
                Log.d(TAG, "Calorie goal updated successfully: $goal")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating calorie goal", e)
            }
    }
}