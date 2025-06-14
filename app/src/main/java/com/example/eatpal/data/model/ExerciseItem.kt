package com.example.eatpal.data.model

import java.util.UUID

data class ExerciseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val duration: Int, // in minutes
    val caloriesBurned: Int
)