package com.example.eatpal.data.model

import java.util.UUID

data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Int,
    val category: String,
    val servingSize: String = "1 serving"
)