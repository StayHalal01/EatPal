package com.example.eatpal.data.repository

import com.example.eatpal.data.model.FoodDatabase
import com.example.eatpal.data.model.NutritionInfo
import com.example.eatpal.data.model.ServingSize

class FoodRepository {

    // Sample food database - this will be replaced with actual API calls later
    private val foodDatabase = listOf(
        FoodDatabase(
            name = "Makanan",
            caloriesPer100g = 167,
            servingSizes = listOf(
                ServingSize("each", 44.0),
                ServingSize("cup", 100.0),
                ServingSize("piece", 50.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 24.5,
                carbs = 0.7,
                fat = 6.5,
                fiber = 2.3,
                sugar = 1.2,
                sodium = 0.8,
                cholesterol = 0.5
            )
        ),
        FoodDatabase(
            name = "Rice",
            caloriesPer100g = 130,
            servingSizes = listOf(
                ServingSize("cup", 200.0),
                ServingSize("bowl", 150.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 2.7,
                carbs = 28.0,
                fat = 0.3,
                fiber = 0.4,
                sugar = 0.1,
                sodium = 0.001,
                cholesterol = 0.0
            )
        ),
        FoodDatabase(
            name = "Chicken Breast",
            caloriesPer100g = 165,
            servingSizes = listOf(
                ServingSize("piece", 120.0),
                ServingSize("slice", 30.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 31.0,
                carbs = 0.0,
                fat = 3.6,
                fiber = 0.0,
                sugar = 0.0,
                sodium = 0.074,
                cholesterol = 0.085
            )
        ),
        FoodDatabase(
            name = "Apple",
            caloriesPer100g = 52,
            servingSizes = listOf(
                ServingSize("medium", 182.0),
                ServingSize("large", 223.0),
                ServingSize("slice", 22.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 0.3,
                carbs = 14.0,
                fat = 0.2,
                fiber = 2.4,
                sugar = 10.4,
                sodium = 0.001,
                cholesterol = 0.0
            )
        ),
        FoodDatabase(
            name = "Banana",
            caloriesPer100g = 89,
            servingSizes = listOf(
                ServingSize("medium", 118.0),
                ServingSize("large", 136.0),
                ServingSize("slice", 9.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 1.1,
                carbs = 23.0,
                fat = 0.3,
                fiber = 2.6,
                sugar = 12.2,
                sodium = 0.001,
                cholesterol = 0.0
            )
        )
    )

    // Mutable list to track favorites
    private val _favorites = mutableSetOf<String>()

    fun searchFoods(query: String): List<FoodDatabase> {
        return if (query.isEmpty()) {
            foodDatabase
        } else {
            foodDatabase.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    fun getAllFoods(): List<FoodDatabase> {
        return foodDatabase
    }

    fun getFavorites(): List<FoodDatabase> {
        return foodDatabase.filter { _favorites.contains(it.name) }
    }

    fun getCustomFoods(): List<FoodDatabase> {
        // Mock custom foods - empty for now
        return emptyList()
    }

    fun isFavorite(food: FoodDatabase): Boolean {
        return _favorites.contains(food.name)
    }

    fun addToFavorites(food: FoodDatabase) {
        _favorites.add(food.name)
    }

    fun removeFromFavorites(food: FoodDatabase) {
        _favorites.remove(food.name)
    }
}
