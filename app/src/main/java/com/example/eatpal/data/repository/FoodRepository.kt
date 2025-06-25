package com.example.eatpal.data.repository

import android.util.Log
import com.example.eatpal.data.model.FoodDatabase
import com.example.eatpal.data.model.NutritionInfo
import com.example.eatpal.data.model.ServingSize
import com.example.eatpal.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodRepository {
    private val TAG = "FoodRepository"

    companion object {
        // Static favorites set that persists across repository instances
        private val _favorites = mutableSetOf<String>()

        // Track when foods were added to diary (food name -> timestamp)
        private val _recentlyAddedToDiary = mutableMapOf<String, Long>()

        // Cache for searched foods
        private val _cachedFoods = mutableMapOf<String, List<FoodDatabase>>()

        // Cache for individual food details
        private val _foodDetailsCache = mutableMapOf<String, FoodDatabase>()

        @Volatile
        private var INSTANCE: FoodRepository? = null

        fun getInstance(): FoodRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FoodRepository().also { INSTANCE = it }
            }
        }
    }

    // Enhanced fallback foods with complete nutrition data (10+ foods with good protein, carbs, and fiber)
    private val fallbackFoods = listOf(
        // HIGH PROTEIN FOODS
        FoodDatabase(
            id = "chicken_breast_fallback",
            name = "Chicken Breast",
            caloriesPer100g = 165,
            servingSizes = listOf(
                ServingSize("piece", 120.0),
                ServingSize("slice", 30.0),
                ServingSize("oz", 28.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 31.0,
                carbs = 0.0,
                fat = 3.6,
                fiber = 0.0,
                sugar = 0.0,
                sodium = 74.0,
                cholesterol = 85.0,
                vitamins = mapOf(
                    "Potassium" to 256.0,
                    "Calcium" to 15.0,
                    "Iron" to 0.9
                )
            )
        ),
        FoodDatabase(
            id = "salmon_fallback",
            name = "Salmon",
            caloriesPer100g = 208,
            servingSizes = listOf(
                ServingSize("fillet", 150.0),
                ServingSize("slice", 40.0),
                ServingSize("oz", 28.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 25.4,
                carbs = 0.0,
                fat = 12.4,
                fiber = 0.0,
                sugar = 0.0,
                sodium = 59.0,
                cholesterol = 70.0,
                vitamins = mapOf(
                    "Potassium" to 363.0,
                    "Calcium" to 12.0,
                    "Iron" to 0.8,
                    "Vitamin A" to 58.0
                )
            )
        ),
        FoodDatabase(
            id = "tofu_fallback",
            name = "Tofu",
            caloriesPer100g = 76,
            servingSizes = listOf(
                ServingSize("cube", 60.0),
                ServingSize("slice", 30.0),
                ServingSize("block", 120.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 8.1,
                carbs = 1.9,
                fat = 4.8,
                fiber = 0.3,
                sugar = 0.6,
                sodium = 7.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Calcium" to 350.0,
                    "Iron" to 5.4,
                    "Potassium" to 121.0
                )
            )
        ),
        FoodDatabase(
            id = "egg_fallback",
            name = "Egg",
            caloriesPer100g = 155,
            servingSizes = listOf(
                ServingSize("large", 50.0),
                ServingSize("medium", 44.0),
                ServingSize("small", 38.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 13.0,
                carbs = 1.1,
                fat = 11.0,
                fiber = 0.0,
                sugar = 1.1,
                sodium = 124.0,
                cholesterol = 373.0,
                vitamins = mapOf(
                    "Vitamin A" to 140.0,
                    "Calcium" to 50.0,
                    "Iron" to 1.2,
                    "Potassium" to 126.0
                )
            )
        ),

        // HIGH CARB FOODS
        FoodDatabase(
            id = "brown_rice_fallback",
            name = "Brown Rice",
            caloriesPer100g = 111,
            servingSizes = listOf(
                ServingSize("cup", 195.0),
                ServingSize("bowl", 150.0),
                ServingSize("tbsp", 15.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 2.6,
                carbs = 23.0,
                fat = 0.9,
                fiber = 1.8,
                sugar = 0.4,
                sodium = 5.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Iron" to 0.4,
                    "Potassium" to 43.0,
                    "Calcium" to 10.0
                )
            )
        ),
        FoodDatabase(
            id = "quinoa_fallback",
            name = "Quinoa",
            caloriesPer100g = 120,
            servingSizes = listOf(
                ServingSize("cup", 185.0),
                ServingSize("bowl", 150.0),
                ServingSize("tbsp", 20.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 4.4,
                carbs = 22.0,
                fat = 1.9,
                fiber = 2.8,
                sugar = 0.9,
                sodium = 7.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Iron" to 1.5,
                    "Potassium" to 172.0,
                    "Calcium" to 17.0,
                    "Vitamin C" to 0.0
                )
            )
        ),
        FoodDatabase(
            id = "sweet_potato_fallback",
            name = "Sweet Potato",
            caloriesPer100g = 86,
            servingSizes = listOf(
                ServingSize("medium", 130.0),
                ServingSize("large", 180.0),
                ServingSize("cup cubed", 133.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 1.6,
                carbs = 20.1,
                fat = 0.1,
                fiber = 3.0,
                sugar = 4.2,
                sodium = 54.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Vitamin A" to 709.0,
                    "Vitamin C" to 2.4,
                    "Potassium" to 337.0,
                    "Calcium" to 30.0,
                    "Iron" to 0.6
                )
            )
        ),
        FoodDatabase(
            id = "oats_fallback",
            name = "Oats",
            caloriesPer100g = 389,
            servingSizes = listOf(
                ServingSize("cup", 81.0),
                ServingSize("bowl", 60.0),
                ServingSize("tbsp", 10.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 16.9,
                carbs = 66.3,
                fat = 6.9,
                fiber = 10.6,
                sugar = 0.0,
                sodium = 2.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Iron" to 4.7,
                    "Potassium" to 429.0,
                    "Calcium" to 54.0,
                    "Vitamin C" to 0.0
                )
            )
        ),

        // HIGH FIBER FOODS
        FoodDatabase(
            id = "broccoli_fallback",
            name = "Broccoli",
            caloriesPer100g = 34,
            servingSizes = listOf(
                ServingSize("cup", 91.0),
                ServingSize("floret", 12.0),
                ServingSize("stalk", 150.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 2.8,
                carbs = 7.0,
                fat = 0.4,
                fiber = 2.6,
                sugar = 1.5,
                sodium = 33.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Vitamin C" to 89.2,
                    "Vitamin A" to 31.0,
                    "Potassium" to 316.0,
                    "Calcium" to 47.0,
                    "Iron" to 0.7
                )
            )
        ),
        FoodDatabase(
            id = "apple_fallback",
            name = "Apple",
            caloriesPer100g = 52,
            servingSizes = listOf(
                ServingSize("medium", 182.0),
                ServingSize("large", 223.0),
                ServingSize("slice", 22.0),
                ServingSize("cup sliced", 125.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 0.3,
                carbs = 14.0,
                fat = 0.2,
                fiber = 2.4,
                sugar = 10.4,
                sodium = 1.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Vitamin C" to 4.6,
                    "Potassium" to 107.0,
                    "Calcium" to 6.0
                )
            )
        ),
        FoodDatabase(
            id = "banana_fallback",
            name = "Banana",
            caloriesPer100g = 89,
            servingSizes = listOf(
                ServingSize("medium", 118.0),
                ServingSize("large", 136.0),
                ServingSize("slice", 9.0),
                ServingSize("cup sliced", 150.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 1.1,
                carbs = 23.0,
                fat = 0.3,
                fiber = 2.6,
                sugar = 12.2,
                sodium = 1.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Vitamin C" to 8.7,
                    "Potassium" to 358.0,
                    "Calcium" to 5.0,
                    "Iron" to 0.3
                )
            )
        ),
        FoodDatabase(
            id = "avocado_fallback",
            name = "Avocado",
            caloriesPer100g = 160,
            servingSizes = listOf(
                ServingSize("medium", 150.0),
                ServingSize("half", 75.0),
                ServingSize("slice", 25.0),
                ServingSize("cup cubed", 150.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 2.0,
                carbs = 8.5,
                fat = 14.7,
                fiber = 6.7,
                sugar = 0.7,
                sodium = 7.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Vitamin C" to 10.0,
                    "Potassium" to 485.0,
                    "Calcium" to 12.0,
                    "Iron" to 0.6,
                    "Vitamin A" to 7.0
                )
            )
        ),
        FoodDatabase(
            id = "black_beans_fallback",
            name = "Black Beans",
            caloriesPer100g = 132,
            servingSizes = listOf(
                ServingSize("cup", 172.0),
                ServingSize("half cup", 86.0),
                ServingSize("tbsp", 15.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 8.9,
                carbs = 23.0,
                fat = 0.5,
                fiber = 8.7,
                sugar = 0.3,
                sodium = 2.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Iron" to 2.1,
                    "Potassium" to 355.0,
                    "Calcium" to 27.0,
                    "Vitamin C" to 0.0
                )
            )
        ),
        FoodDatabase(
            id = "spinach_fallback",
            name = "Spinach",
            caloriesPer100g = 23,
            servingSizes = listOf(
                ServingSize("cup", 30.0),
                ServingSize("handful", 25.0),
                ServingSize("bunch", 120.0),
                ServingSize("portion", 100.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 2.9,
                carbs = 3.6,
                fat = 0.4,
                fiber = 2.2,
                sugar = 0.4,
                sodium = 79.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Vitamin A" to 469.0,
                    "Vitamin C" to 28.1,
                    "Iron" to 2.7,
                    "Potassium" to 558.0,
                    "Calcium" to 99.0
                )
            )
        ),
        FoodDatabase(
            id = "almonds_fallback",
            name = "Almonds",
            caloriesPer100g = 579,
            servingSizes = listOf(
                ServingSize("ounce", 28.0),
                ServingSize("cup", 143.0),
                ServingSize("piece", 1.2),
                ServingSize("handful", 30.0)
            ),
            nutritionPer100g = NutritionInfo(
                protein = 21.2,
                carbs = 21.6,
                fat = 49.9,
                fiber = 12.5,
                sugar = 4.4,
                sodium = 1.0,
                cholesterol = 0.0,
                vitamins = mapOf(
                    "Vitamin C" to 0.0,
                    "Calcium" to 269.0,
                    "Iron" to 3.7,
                    "Potassium" to 733.0
                )
            )
        )
    )

    /**
     * Enhanced search for foods using the Nutritionix API
     */
    suspend fun searchFoods(query: String): List<FoodDatabase> = withContext(Dispatchers.IO) {
        if (query.isEmpty()) {
            return@withContext if (_cachedFoods.containsKey("popular")) {
                _cachedFoods["popular"] ?: fallbackFoods
            } else {
                try {
                    // Get popular foods from API (empty query returns popular items)
                    val response = NetworkModule.nutritionixApi.searchFood("")
                    val results = processSearchResults(response)
                    _cachedFoods["popular"] = results
                    results
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching popular foods", e)
                    fallbackFoods
                }
            }
        }

        // Check cache first
        if (_cachedFoods.containsKey(query)) {
            return@withContext _cachedFoods[query] ?: emptyList()
        }

        try {
            val response = NetworkModule.nutritionixApi.searchFood(query)
            val results = processSearchResults(response)

            // Cache the results
            _cachedFoods[query] = results

            results
        } catch (e: Exception) {
            Log.e(TAG, "Error searching foods", e)

            // Fall back to filtering local data
            fallbackFoods.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Process search results from the API with better nutrition handling
     */
    private fun processSearchResults(response: com.example.eatpal.data.model.SearchResultResponse): List<FoodDatabase> {
        val commonFoods = response.commonFoods.map { item ->
            val commonItem = item.copy(isBranded = false)
            val foodDb = commonItem.toFoodDatabase()

            // If no nutrition data from search, try to match with fallback data
            if (foodDb.nutritionPer100g.protein == 0.0 && foodDb.nutritionPer100g.carbs == 0.0) {
                val fallbackMatch = fallbackFoods.find {
                    it.name.equals(foodDb.name, ignoreCase = true) ||
                            foodDb.name.contains(it.name, ignoreCase = true)
                }
                if (fallbackMatch != null) {
                    foodDb.copy(
                        nutritionPer100g = fallbackMatch.nutritionPer100g,
                        servingSizes = fallbackMatch.servingSizes + foodDb.servingSizes
                    )
                } else {
                    foodDb
                }
            } else {
                foodDb
            }
        }

        val brandedFoods = response.brandedFoods.map { item ->
            val brandedItem = item.copy(isBranded = true)
            brandedItem.toFoodDatabase()
        }

        return commonFoods + brandedFoods
    }

    /**
     * Get detailed nutrition information for a specific food
     */
    suspend fun getFoodDetails(foodId: String): FoodDatabase? = withContext(Dispatchers.IO) {
        // Check cache first
        if (_foodDetailsCache.containsKey(foodId)) {
            return@withContext _foodDetailsCache[foodId]
        }

        try {
            Log.d(TAG, "Fetching details for food ID: $foodId")

            // Try to get details from Nutritionix API
            val response = NetworkModule.nutritionixApi.getFoodDetails(foodId)
            val foodDatabase = response.toFoodDatabase()

            Log.d(TAG, "Successfully fetched food details: ${foodDatabase.name}")
            Log.d(TAG, "Nutrition data - Protein: ${foodDatabase.nutritionPer100g.protein}g, Carbs: ${foodDatabase.nutritionPer100g.carbs}g")

            // Cache the result
            _foodDetailsCache[foodId] = foodDatabase

            foodDatabase
        } catch (e: Exception) {
            Log.e(TAG, "Error getting food details for ID: $foodId", e)

            // Try to find a match in our cached search results first
            val cachedMatch = _cachedFoods.values.flatten().find { it.id == foodId }
            if (cachedMatch != null) {
                Log.d(TAG, "Found cached match for food ID: $foodId")
                return@withContext cachedMatch
            }

            // Try to find a match in fallback foods
            val fallbackFood = fallbackFoods.find {
                it.id == foodId ||
                        it.name.contains(foodId, ignoreCase = true) ||
                        foodId.contains(it.name.replace(" ", ""), ignoreCase = true)
            }

            if (fallbackFood != null) {
                Log.d(TAG, "Found fallback match for food ID: $foodId")
                return@withContext fallbackFood
            }

            Log.w(TAG, "No match found for food ID: $foodId")
            null
        }
    }

    /**
     * Get all foods (popular foods or fallback list)
     */
    suspend fun getAllFoods(): List<FoodDatabase> = withContext(Dispatchers.IO) {
        if (_cachedFoods.containsKey("popular")) {
            return@withContext _cachedFoods["popular"] ?: fallbackFoods
        }

        try {
            val response = NetworkModule.nutritionixApi.searchFood("")
            val results = processSearchResults(response)
            _cachedFoods["popular"] = results
            results
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all foods", e)
            fallbackFoods
        }
    }

    /**
     * Check if a food is in favorites
     */
    fun isFavorite(food: FoodDatabase): Boolean {
        return _favorites.contains(food.id)
    }

    /**
     * Add a food to favorites
     */
    fun addToFavorites(food: FoodDatabase) {
        _favorites.add(food.id)
        Log.d(TAG, "Added ${food.name} to favorites")
    }

    /**
     * Remove a food from favorites
     */
    fun removeFromFavorites(food: FoodDatabase) {
        _favorites.remove(food.id)
        Log.d(TAG, "Removed ${food.name} from favorites")
    }

    /**
     * Mark a food as recently added to diary
     */
    fun markAsAddedToDiary(food: FoodDatabase) {
        _recentlyAddedToDiary[food.id] = System.currentTimeMillis()
        Log.d(TAG, "Marked ${food.name} as added to diary")
    }

    /**
     * Clear search cache (useful for testing)
     */
    fun clearCache() {
        _cachedFoods.clear()
        _foodDetailsCache.clear()
        Log.d(TAG, "Cache cleared")
    }

    /**
     * Get favorites for the tab functionality
     */
    fun getFavorites(): List<FoodDatabase> {
        return _cachedFoods.values.flatten().filter {
            _favorites.contains(it.id)
        }
    }
}