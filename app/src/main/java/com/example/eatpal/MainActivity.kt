// MainActivity.kt
package com.example.eatpal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatpal.ui.theme.EatPalTheme
import java.text.SimpleDateFormat
import java.util.*

// ... existing code ...

// Data Classes
data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Int,
    val category: String,
    val servingSize: String = "1 serving"
)

data class ExerciseItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val duration: Int, // in minutes
    val caloriesBurned: Int
)

data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: Date,
    val foodItems: List<FoodItem> = emptyList(),
    val exercises: List<ExerciseItem> = emptyList(),
    val waterIntake: Int = 0 // in fl oz
)

// ViewModel
class CaloriesTrackerViewModel : ViewModel() {
    private val _currentDate = mutableStateOf(Date())
    val currentDate: State<Date> = _currentDate

    private val _currentEntry = mutableStateOf(DiaryEntry(date = Date()))
    val currentEntry: State<DiaryEntry> = _currentEntry

    private val _dailyCalorieGoal = mutableStateOf(2039)
    val dailyCalorieGoal: State<Int> = _dailyCalorieGoal

    val totalCaloriesEaten: Int
        get() = _currentEntry.value.foodItems.sumOf { it.calories }

    val totalCaloriesBurned: Int
        get() = _currentEntry.value.exercises.sumOf { it.caloriesBurned }

    val remainingCalories: Int
        get() = _dailyCalorieGoal.value - totalCaloriesEaten + totalCaloriesBurned

    fun addFoodItem(food: FoodItem) {
        _currentEntry.value = _currentEntry.value.copy(
            foodItems = _currentEntry.value.foodItems + food
        )
    }

    fun removeFoodItem(foodId: String) {
        _currentEntry.value = _currentEntry.value.copy(
            foodItems = _currentEntry.value.foodItems.filter { it.id != foodId }
        )
    }

    fun addExercise(exercise: ExerciseItem) {
        _currentEntry.value = _currentEntry.value.copy(
            exercises = _currentEntry.value.exercises + exercise
        )
    }

    fun updateWaterIntake(amount: Int) {
        _currentEntry.value = _currentEntry.value.copy(
            waterIntake = amount.coerceAtLeast(0)
        )
    }

    fun navigateDay(forward: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.time = _currentDate.value
        calendar.add(Calendar.DAY_OF_MONTH, if (forward) 1 else -1)
        _currentDate.value = calendar.time
        _currentEntry.value = _currentEntry.value.copy(date = calendar.time)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatPalTheme {
                CaloriesTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaloriesTrackerApp() {
    val viewModel: CaloriesTrackerViewModel = viewModel()
    var currentScreen by remember { mutableStateOf("diary") }
    var showAddFood by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                modifier = Modifier.height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.Default.DateRange,
                        label = "Diary",
                        isSelected = currentScreen == "diary"
                    ) { currentScreen = "diary" }

                    FloatingActionButton(
                        onClick = { showAddFood = true },
                        containerColor = Color(0xFF4CAF50),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Food", tint = Color.White)
                    }

                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Account",
                        isSelected = currentScreen == "account"
                    ) { currentScreen = "account" }
                }
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            "diary" -> DiaryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues),
                onAddFood = { showAddFood = true },
                onAddExercise = { showAddExercise = true }
            )
        }

        if (showAddFood) {
            AddFoodDialog(
                onDismiss = { showAddFood = false },
                onAddFood = { food ->
                    viewModel.addFoodItem(food)
                    showAddFood = false
                }
            )
        }

        if (showAddExercise) {
            AddExerciseDialog(
                onDismiss = { showAddExercise = false },
                onAddExercise = { exercise ->
                    viewModel.addExercise(exercise)
                    showAddExercise = false
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF4CAF50) else Color.Gray
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF4CAF50) else Color.Gray
        )
    }
}

@Composable
fun DiaryScreen(
    viewModel: CaloriesTrackerViewModel,
    modifier: Modifier = Modifier,
    onAddFood: () -> Unit,
    onAddExercise: () -> Unit
) {
    val entry by viewModel.currentEntry
    val goal by viewModel.dailyCalorieGoal

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CaloriesTrackerCard(
                eaten = viewModel.totalCaloriesEaten,
                burned = viewModel.totalCaloriesBurned,
                remaining = viewModel.remainingCalories,
                goal = goal
            )
        }

        item {
            DateSelector(viewModel)
        }

        item {
            FoodDiarySection(
                foodItems = entry.foodItems,
                waterIntake = entry.waterIntake,
                onAddFood = onAddFood,
                onUpdateWater = { viewModel.updateWaterIntake(it) },
                onRemoveFood = { viewModel.removeFoodItem(it) }
            )
        }
    }
}

@Composable
fun CaloriesTrackerCard(
    eaten: Int,
    burned: Int,
    remaining: Int,
    goal: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFFFF9800)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Calories Tracker",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main content with calories info and circular indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Eaten calories (left)
                    CalorieInfo(eaten.toString(), "Eaten", Color.White)

                    // Circular Progress Indicator (center)
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = if (goal > 0) (eaten.toFloat() / goal.toFloat()).coerceAtMost(
                                1f
                            ) else 0f,
                            modifier = Modifier.size(120.dp),
                            strokeWidth = 8.dp,
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                remaining.toString(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "/$goal",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Text(
                                "Remaining",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Burned calories (right)
                    CalorieInfo(burned.toString(), "Burned", Color.White)
                }
            }
        }
    }
}

@Composable
fun CalorieInfo(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = color,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DateSelector(viewModel: CaloriesTrackerViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Previous day",
                modifier = Modifier.clickable { viewModel.navigateDay(false) })
            Text(
                "Today, ${
                    SimpleDateFormat(
                        "d MMMM yyyy",
                        Locale.getDefault()
                    ).format(viewModel.currentDate.value)
                }",
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Next day",
                modifier = Modifier.clickable { viewModel.navigateDay(true) })
        }
    }
}

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
                Text("Add Categories")
            }
        }
    }
}

@Composable
fun ExerciseDiarySection(
    exercises: List<ExerciseItem>,
    onAddExercise: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Exercise",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            exercises.forEach { exercise ->
                DiaryItem(
                    icon = Icons.Default.PlayArrow,
                    title = exercise.name,
                    subtitle = "${exercise.duration} minutes",
                    calories = exercise.caloriesBurned,
                    onAdd = null
                )
            }

            if (exercises.isEmpty()) {
                TextButton(
                    onClick = onAddExercise,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Categories")
                }
            }
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
fun AddItemSection(
    onAddFood: () -> Unit,
    onAddExercise: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AddButton(
            icon = Icons.Default.Add,
            text = "Add Food",
            backgroundColor = Color(0xFF4CAF50),
            onClick = onAddFood
        )

        AddButton(
            icon = Icons.Default.Add,
            text = "Add Exercise",
            backgroundColor = Color(0xFFFF9800),
            onClick = onAddExercise
        )
    }
}

@Composable
fun AddButton(
    icon: ImageVector,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier
            .height(50.dp)
            .width(150.dp)
    ) {
        Icon(
            icon,
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onAddFood: (FoodItem) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Breakfast") }
    val categories = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food") },
        text = {
            Column {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Category:")
                categories.forEach { cat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = category == cat,
                            onClick = { category = cat }
                        )
                        Text(cat)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (foodName.isNotBlank() && calories.isNotBlank()) {
                        onAddFood(
                            FoodItem(
                                name = foodName,
                                calories = calories.toIntOrNull() ?: 0,
                                category = category
                            )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onAddExercise: (ExerciseItem) -> Unit
) {
    var exerciseName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var caloriesBurned by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exercise") },
        text = {
            Column {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = caloriesBurned,
                    onValueChange = { caloriesBurned = it },
                    label = { Text("Calories Burned") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (exerciseName.isNotBlank() && duration.isNotBlank() && caloriesBurned.isNotBlank()) {
                        onAddExercise(
                            ExerciseItem(
                                name = exerciseName,
                                duration = duration.toIntOrNull() ?: 0,
                                caloriesBurned = caloriesBurned.toIntOrNull() ?: 0
                            )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
