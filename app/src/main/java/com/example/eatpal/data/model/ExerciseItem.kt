package com.example.eatpal.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

data class Exercise(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val caloriesPerHour: Int // Base calories burned per 60 minutes
)

data class ExerciseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val duration: Int, // in minutes
    val caloriesBurned: Int
)
