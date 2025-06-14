package com.example.eatpal.presentation.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eatpal.data.model.ExerciseItem
import com.example.eatpal.data.model.FoodItem

@Composable
fun DiarySection(
    foodItems: List<FoodItem>,
    exercises: List<ExerciseItem>,
    waterIntake: Int,
    onAddFood: () -> Unit,
    onAddExercise: () -> Unit,
    onUpdateWater: (Int) -> Unit,
    onRemoveFood: (String) -> Unit,
    onRemoveExercise: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("EAT") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TabItem(
                    text = "EAT",
                    isSelected = selectedTab == "EAT",
                    onClick = { selectedTab = "EAT" }
                )
                Spacer(modifier = Modifier.width(8.dp))
                TabItem(
                    text = "EXERCISE",
                    isSelected = selectedTab == "EXERCISE",
                    onClick = { selectedTab = "EXERCISE" }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                "EAT" -> FoodContent(
                    foodItems = foodItems,
                    waterIntake = waterIntake,
                    onAddFood = onAddFood,
                    onUpdateWater = onUpdateWater,
                    onRemoveFood = onRemoveFood
                )

                "EXERCISE" -> ExerciseContent(
                    exercises = exercises,
                    onAddExercise = onAddExercise,
                    onRemoveExercise = onRemoveExercise
                )
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FoodContent(
    foodItems: List<FoodItem>,
    waterIntake: Int,
    onAddFood: () -> Unit,
    onUpdateWater: (Int) -> Unit,
    onRemoveFood: (String) -> Unit
) {
    Column {
        // Water intake
        DiaryItem(
            icon = Icons.Default.Home,
            title = "Water",
            subtitle = "$waterIntake / 64 fl oz",
            calories = null,
            onAdd = { onUpdateWater(waterIntake + 8) },
            onRemove = if (waterIntake > 0) {
                { onUpdateWater(waterIntake - 8) }
            } else null
        )

        // Breakfast
        val breakfastItems = foodItems.filter { it.category == "Breakfast" }
        DiaryItem(
            icon = Icons.Default.Star,
            title = "Breakfast",
            subtitle = if (breakfastItems.isNotEmpty())
                breakfastItems.joinToString(", ") { it.name }
            else "Add breakfast items",
            calories = breakfastItems.sumOf { it.calories }.takeIf { it > 0 },
            onAdd = onAddFood,
            onRemove = null
        )

        // Show individual breakfast items
        breakfastItems.forEach { food ->
            FoodItemRow(
                food = food,
                onRemove = { onRemoveFood(food.id) }
            )
        }

        // Lunch
        val lunchItems = foodItems.filter { it.category == "Lunch" }
        DiaryItem(
            icon = Icons.Default.FavoriteBorder,
            title = "Lunch",
            subtitle = if (lunchItems.isNotEmpty())
                lunchItems.joinToString(", ") { it.name }
            else "Add lunch items",
            calories = lunchItems.sumOf { it.calories }.takeIf { it > 0 },
            onAdd = onAddFood,
            onRemove = null
        )

        // Show individual lunch items
        lunchItems.forEach { food ->
            FoodItemRow(
                food = food,
                onRemove = { onRemoveFood(food.id) }
            )
        }

        // Dinner
        val dinnerItems = foodItems.filter { it.category == "Dinner" }
        DiaryItem(
            icon = Icons.Default.Place,
            title = "Dinner",
            subtitle = if (dinnerItems.isNotEmpty())
                dinnerItems.joinToString(", ") { it.name }
            else "Add dinner items",
            calories = dinnerItems.sumOf { it.calories }.takeIf { it > 0 },
            onAdd = onAddFood,
            onRemove = null
        )

        // Show individual dinner items
        dinnerItems.forEach { food ->
            FoodItemRow(
                food = food,
                onRemove = { onRemoveFood(food.id) }
            )
        }

        // Snack
        val snackItems = foodItems.filter { it.category == "Snack" }
        DiaryItem(
            icon = Icons.Default.Favorite,
            title = "Snack",
            subtitle = if (snackItems.isNotEmpty())
                snackItems.joinToString(", ") { it.name }
            else "Add snack items",
            calories = snackItems.sumOf { it.calories }.takeIf { it > 0 },
            onAdd = onAddFood,
            onRemove = null
        )

        // Show individual snack items
        snackItems.forEach { food ->
            FoodItemRow(
                food = food,
                onRemove = { onRemoveFood(food.id) }
            )
        }

        // Add more button
        TextButton(
            onClick = onAddFood,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Food")
        }
    }
}

@Composable
fun ExerciseContent(
    exercises: List<ExerciseItem>,
    onAddExercise: () -> Unit,
    onRemoveExercise: (String) -> Unit
) {
    Column {
        exercises.forEach { exercise ->
            DiaryItem(
                icon = Icons.Default.PlayArrow,
                title = exercise.name,
                subtitle = "${exercise.duration} minutes",
                calories = exercise.caloriesBurned,
                onAdd = null,
                onRemove = { onRemoveExercise(exercise.id) }
            )
        }

        if (exercises.isEmpty()) {
            Text(
                text = "No exercises recorded",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Add exercise button
        TextButton(
            onClick = onAddExercise,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Exercise")
        }
    }
}

// Keep the original FoodDiarySection for backward compatibility
@Composable
fun FoodDiarySection(
    foodItems: List<FoodItem>,
    waterIntake: Int,
    onAddFood: () -> Unit,
    onUpdateWater: (Int) -> Unit,
    onRemoveFood: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "EAT",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FoodContent(
                foodItems = foodItems,
                waterIntake = waterIntake,
                onAddFood = onAddFood,
                onUpdateWater = onUpdateWater,
                onRemoveFood = onRemoveFood
            )
        }
    }
}

@Composable
fun DiaryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    calories: Int?,
    onAdd: (() -> Unit)?,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
            calories?.let {
                Text("$it kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        onAdd?.let {
            IconButton(
                onClick = it,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        onRemove?.let {
            IconButton(
                onClick = it,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Red, CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FoodItemRow(
    food: FoodItem,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            food.name,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove ${food.name}",
                tint = Color.Red
            )
        }
    }
}
