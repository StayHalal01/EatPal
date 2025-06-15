package com.example.eatpal.data.model

import java.util.UUID

data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Int,
    val category: String,
    val servingSize: String = "1 serving",
    val amount: Double = 1.0,
    val nutritionInfo: NutritionInfo = NutritionInfo(),
    val dateAdded: Long = System.currentTimeMillis() // Add this field
)

data class NutritionInfo(
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val cholesterol: Double = 0.0,
    val vitamins: Map<String, Double> = emptyMap()
)

data class FoodDatabase(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val caloriesPer100g: Int,
    val servingSizes: List<ServingSize> = listOf(ServingSize("each", 44.0)),
    val nutritionPer100g: NutritionInfo = NutritionInfo(),
    val category: String = "Food",
    val dateAdded: Long = System.currentTimeMillis()
)

data class ServingSize(
    val name: String,
    val grams: Double
)
