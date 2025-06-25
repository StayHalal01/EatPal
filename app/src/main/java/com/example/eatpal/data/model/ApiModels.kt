package com.example.eatpal.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for search results from Nutritionix API
 */
data class SearchResultResponse(
    @SerializedName("common")
    val commonFoods: List<SearchResultItem> = emptyList(),

    @SerializedName("branded")
    val brandedFoods: List<SearchResultItem> = emptyList()
)

/**
 * Model for a food item in search results
 */
data class SearchResultItem(
    @SerializedName("food_name")
    val foodName: String = "",

    @SerializedName("serving_qty")
    val servingQty: Double = 1.0,

    @SerializedName("serving_unit")
    val servingUnit: String = "serving",

    @SerializedName("serving_weight_grams")
    val servingWeightGrams: Double = 100.0,

    @SerializedName("photo")
    val photo: PhotoInfo? = null,

    @SerializedName("nix_item_id")
    val nixItemId: String? = null,

    @SerializedName("nf_calories")
    val calories: Double = 0.0,

    // Additional nutrition fields that might be available in search
    @SerializedName("nf_total_fat")
    val totalFat: Double? = null,

    @SerializedName("nf_protein")
    val protein: Double? = null,

    @SerializedName("nf_total_carbohydrate")
    val totalCarbs: Double? = null,

    @SerializedName("nf_dietary_fiber")
    val fiber: Double? = null,

    @SerializedName("nf_sugars")
    val sugars: Double? = null,

    @SerializedName("nf_sodium")
    val sodium: Double? = null,

    // Used to determine if it's a common or branded food
    val isBranded: Boolean = false
) {
    // Convert to our app's FoodDatabase model with available nutrition
    fun toFoodDatabase(): FoodDatabase {
        val nutritionData = if (protein != null || totalFat != null || totalCarbs != null) {
            // If we have some nutrition data from search, use it
            val multiplier = if (servingWeightGrams > 0) 100.0 / servingWeightGrams else 1.0
            NutritionInfo(
                protein = (protein ?: 0.0) * multiplier,
                carbs = (totalCarbs ?: 0.0) * multiplier,
                fat = (totalFat ?: 0.0) * multiplier,
                fiber = (fiber ?: 0.0) * multiplier,
                sugar = (sugars ?: 0.0) * multiplier,
                sodium = (sodium ?: 0.0) * multiplier * 1000, // Convert to mg
                cholesterol = 0.0,
                vitamins = emptyMap()
            )
        } else {
            // No nutrition data available in search, will need to fetch details
            NutritionInfo()
        }

        return FoodDatabase(
            id = nixItemId ?: foodName.hashCode().toString(),
            name = foodName,
            caloriesPer100g = if (servingWeightGrams > 0)
                (calories * 100 / servingWeightGrams).toInt()
            else 0,
            servingSizes = listOf(
                ServingSize(servingUnit, servingWeightGrams)
            ),
            nutritionPer100g = nutritionData,
            category = if (isBranded) "Branded" else "Common",
            photoUrl = photo?.thumb
        )
    }
}

/**
 * Model for food photo information
 */
data class PhotoInfo(
    @SerializedName("thumb")
    val thumb: String? = null,

    @SerializedName("highres")
    val highres: String? = null
)

/**
 * Response model for detailed nutrition information
 */
data class NutritionApiResponse(
    @SerializedName("item_id")
    val itemId: String = "",

    @SerializedName("item_name")
    val itemName: String = "",

    @SerializedName("brand_id")
    val brandId: String? = null,

    @SerializedName("brand_name")
    val brandName: String? = null,

    @SerializedName("nf_calories")
    val calories: Double = 0.0,

    @SerializedName("nf_total_fat")
    val totalFat: Double = 0.0,

    @SerializedName("nf_saturated_fat")
    val saturatedFat: Double = 0.0,

    @SerializedName("nf_cholesterol")
    val cholesterol: Double = 0.0,

    @SerializedName("nf_sodium")
    val sodium: Double = 0.0,

    @SerializedName("nf_total_carbohydrate")
    val totalCarbs: Double = 0.0,

    @SerializedName("nf_dietary_fiber")
    val fiber: Double = 0.0,

    @SerializedName("nf_sugars")
    val sugars: Double = 0.0,

    @SerializedName("nf_protein")
    val protein: Double = 0.0,

    @SerializedName("nf_potassium")
    val potassium: Double = 0.0,

    @SerializedName("nf_serving_size_qty")
    val servingSizeQty: Double = 1.0,

    @SerializedName("nf_serving_size_unit")
    val servingSizeUnit: String = "serving",

    @SerializedName("nf_serving_weight_grams")
    val servingWeightGrams: Double = 100.0,

    // Additional nutrients that might be available
    @SerializedName("nf_calcium")
    val calcium: Double = 0.0,

    @SerializedName("nf_iron")
    val iron: Double = 0.0,

    @SerializedName("nf_vitamin_a")
    val vitaminA: Double = 0.0,

    @SerializedName("nf_vitamin_c")
    val vitaminC: Double = 0.0
) {
    // Convert to our app's NutritionInfo model
    fun toNutritionInfo(): NutritionInfo {
        return NutritionInfo(
            protein = protein,
            carbs = totalCarbs,
            fat = totalFat,
            fiber = fiber,
            sugar = sugars,
            sodium = sodium * 1000, // Convert to mg for consistency
            cholesterol = cholesterol * 1000, // Convert to mg
            vitamins = mapOf(
                "Potassium" to potassium,
                "Calcium" to calcium,
                "Iron" to iron,
                "Vitamin A" to vitaminA,
                "Vitamin C" to vitaminC
            ).filter { it.value > 0 } // Only include vitamins with values > 0
        )
    }

    // Convert to our app's FoodDatabase model
    fun toFoodDatabase(): FoodDatabase {
        val servingSizes = mutableListOf<ServingSize>()

        // Add the primary serving size from API
        servingSizes.add(ServingSize(servingSizeUnit, servingWeightGrams))

        // Add common serving sizes if not already present
        if (servingSizeUnit != "cup" && servingWeightGrams != 240.0) {
            servingSizes.add(ServingSize("cup", 240.0))
        }
        if (servingSizeUnit != "piece" && servingWeightGrams != 100.0) {
            servingSizes.add(ServingSize("piece", 100.0))
        }
        if (servingSizeUnit != "tbsp" && servingWeightGrams != 15.0) {
            servingSizes.add(ServingSize("tbsp", 15.0))
        }

        // Convert nutrition values to per-100g basis
        val multiplier = if (servingWeightGrams > 0) 100.0 / servingWeightGrams else 1.0

        // Get base nutrition info using the existing function
        val baseNutrition = toNutritionInfo()

        // Scale it to per-100g values
        val nutritionPer100g = NutritionInfo(
            protein = baseNutrition.protein * multiplier,
            carbs = baseNutrition.carbs * multiplier,
            fat = baseNutrition.fat * multiplier,
            fiber = baseNutrition.fiber * multiplier,
            sugar = baseNutrition.sugar * multiplier,
            sodium = baseNutrition.sodium * multiplier,
            cholesterol = baseNutrition.cholesterol * multiplier,
            vitamins = baseNutrition.vitamins.mapValues { it.value * multiplier }
        )

        return FoodDatabase(
            id = itemId,
            name = itemName,
            caloriesPer100g = if (servingWeightGrams > 0)
                (calories * 100 / servingWeightGrams).toInt()
            else 0,
            servingSizes = servingSizes,
            nutritionPer100g = nutritionPer100g,
            category = brandName ?: "Food",
            dateAdded = System.currentTimeMillis()
        )
    }
}