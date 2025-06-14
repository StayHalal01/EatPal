package com.example.eatpal.data.model

import java.util.Date
import java.util.UUID

data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: Date,
    val foodItems: List<FoodItem> = emptyList(),
    val exercises: List<ExerciseItem> = emptyList(),
    val waterIntake: Int = 0 // in fl oz
)