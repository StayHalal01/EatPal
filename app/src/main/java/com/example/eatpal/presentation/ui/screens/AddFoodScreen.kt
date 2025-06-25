package com.example.eatpal.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eatpal.data.model.FoodDatabase
import com.example.eatpal.data.model.FoodItem
import com.example.eatpal.data.repository.FoodRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onDismiss: () -> Unit,
    onAddFood: (FoodItem) -> Unit,
    defaultCategory: String = "Breakfast"
) {
    val coroutineScope = rememberCoroutineScope()
    val foodRepository = remember { FoodRepository.getInstance() }
    var currentView by remember { mutableStateOf("list") } // "list" or "detail"
    var selectedFood by remember { mutableStateOf<FoodDatabase?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(false) }
    var foods by remember { mutableStateOf<List<FoodDatabase>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Initial load of popular foods
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            foods = foodRepository.getAllFoods()
        } catch (e: Exception) {
            errorMessage = "Failed to load foods: ${e.message}"
            Log.e("AddFoodScreen", "Error loading foods", e)
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            Text(
                text = "Add Food",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(
                        Color(0xFF4CAF50),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (currentView == "list") {
            FoodListView(
                searchQuery = searchQuery,
                onSearchQueryChange = { newQuery ->
                    searchQuery = newQuery
                    // Search when query changes
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            foods = foodRepository.searchFoods(newQuery)
                        } catch (e: Exception) {
                            errorMessage = "Search failed: ${e.message}"
                            Log.e("AddFoodScreen", "Search error", e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it },
                foods = foods,
                isLoading = isLoading,
                onFoodSelect = { food ->
                    // THIS IS THE KEY PART - Load detailed food information
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            Log.d("AddFoodScreen", "Loading details for food: ${food.id} - ${food.name}")
                            // Call getFoodDetails to get complete nutrition information
                            val detailedFood = foodRepository.getFoodDetails(food.id)
                            if (detailedFood != null) {
                                Log.d("AddFoodScreen", "Detailed food loaded successfully")
                                selectedFood = detailedFood
                            } else {
                                Log.d("AddFoodScreen", "No detailed food found, using basic info")
                                selectedFood = food
                            }
                        } catch (e: Exception) {
                            Log.e("AddFoodScreen", "Error loading food details", e)
                            selectedFood = food  // Fallback to basic info
                            errorMessage = "Couldn't load complete nutrition data: ${e.message}"
                        } finally {
                            isLoading = false
                            currentView = "detail"
                        }
                    }
                }
            )
        } else if (currentView == "detail" && selectedFood != null) {
            FoodDetailView(
                food = selectedFood!!,
                defaultCategory = defaultCategory,
                onBack = { currentView = "list" },
                onAddToLog = onAddFood,
                foodRepository = foodRepository
            )
        }

        // Display error message if any
        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodListView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: String,
    onTabChange: (String) -> Unit,
    foods: List<FoodDatabase>,
    isLoading: Boolean,
    onFoodSelect: (FoodDatabase) -> Unit
) {
    var sortBy by remember { mutableStateOf("Most Relevant") }
    var showSortMenu by remember { mutableStateOf(false) }
    val sortOptions = listOf("Most Relevant", "Recently Added", "A-Z", "Z-A")

    Column {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search for foods...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs (All, Favorites)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("All", "Favourite").forEach { tab ->
                Text(
                    text = tab,
                    color = if (selectedTab == tab) Color(0xFF4CAF50) else Color.Gray,
                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { onTabChange(tab) }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort/Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${foods.size} Results",
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showSortMenu = true }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Sort")
                Text(sortBy, fontSize = 14.sp, color = Color.Gray)

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sortBy = option
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
        }

        // Food Items
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(foods) { food ->
                FoodItemRow(
                    food = food,
                    onClick = { onFoodSelect(food) }
                )
            }

            // Empty state
            if (foods.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "No foods found for '$searchQuery'"
                            else
                                "No foods available",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodItemRow(
    food: FoodDatabase,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food image
            food.photoUrl?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = food.name,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            } ?: run {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Fastfood,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Food info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = food.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${food.caloriesPer100g} kcal per 100g",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // Chevron icon - FIXED: Use AutoMirrored version
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "View details",
            tint = Color.Gray
        )
    }
    // FIXED: Use HorizontalDivider instead of Divider
    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailView(
    food: FoodDatabase,
    defaultCategory: String,
    onBack: () -> Unit,
    onAddToLog: (FoodItem) -> Unit,
    foodRepository: FoodRepository
) {
    var amount by remember { mutableStateOf("1") }
    var selectedServing by remember { mutableStateOf(food.servingSizes.firstOrNull()?.name ?: "Medium") }
    var selectedCategory by remember { mutableStateOf(defaultCategory) }
    var showNutritionExpanded by remember { mutableStateOf(true) }
    var showNutrientsExpanded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(foodRepository.isFavorite(food)) }

    val categories = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    // Find selected serving size in grams
    val selectedServingGrams = food.servingSizes.find { it.name == selectedServing }?.grams ?: 100.0

    // Calculate serving multiplier based on amount and serving size
    val servingMultiplier = (amount.toDoubleOrNull() ?: 1.0) * (selectedServingGrams / 100.0)
    val totalCalories = (food.caloriesPer100g * servingMultiplier).toInt()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with back button and favorite
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                // FIXED: Use AutoMirrored version
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = food.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    if (isFavorite) {
                        foodRepository.addToFavorites(food)
                    } else {
                        foodRepository.removeFromFavorites(food)
                    }
                }
            ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Add to favorite",
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }

        // Food image
        food.photoUrl?.let { photoUrl ->
            AsyncImage(
                model = photoUrl,
                contentDescription = food.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Serving Size Dropdown
                var servingExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = servingExpanded,
                    onExpandedChange = { servingExpanded = !servingExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedServing,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Serving Size") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servingExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = servingExpanded,
                        onDismissRequest = { servingExpanded = false }
                    ) {
                        food.servingSizes.forEach { serving ->
                            DropdownMenuItem(
                                text = { Text("${serving.name} (${serving.grams}g)") },
                                onClick = {
                                    selectedServing = serving.name
                                    servingExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Categories") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Nutrition Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Energy Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Energy Summary",
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { showNutritionExpanded = !showNutritionExpanded }
                            ) {
                                Icon(
                                    if (showNutritionExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                        }

                        if (showNutritionExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Calories Circle
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    // FIXED: Use lambda version of CircularProgressIndicator
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        color = Color(0xFF4CAF50),
                                        strokeWidth = 8.dp,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = totalCalories.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "kcal",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Macros
                                Column {
                                    MacroRow(
                                        name = "Protein",
                                        grams = food.nutritionPer100g.protein * servingMultiplier,
                                        totalCalories = totalCalories.toDouble(),
                                        color = Color(0xFF4CAF50)
                                    )
                                    MacroRow(
                                        name = "Net Carbs",
                                        grams = food.nutritionPer100g.carbs * servingMultiplier,
                                        totalCalories = totalCalories.toDouble(),
                                        color = Color(0xFF2196F3)
                                    )
                                    MacroRow(
                                        name = "Fat",
                                        grams = food.nutritionPer100g.fat * servingMultiplier,
                                        totalCalories = totalCalories.toDouble(),
                                        color = Color(0xFFFF9800)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Nutrients Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nutrients",
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { showNutrientsExpanded = !showNutrientsExpanded }
                            ) {
                                Icon(
                                    if (showNutrientsExpanded) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle"
                                )
                            }
                        }

                        if (showNutrientsExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))

                            NutrientRow(
                                "Fiber",
                                "${
                                    String.format(
                                        "%.1f",
                                        food.nutritionPer100g.fiber * servingMultiplier
                                    )
                                }g"
                            )
                            NutrientRow(
                                "Sugar",
                                "${
                                    String.format(
                                        "%.1f",
                                        food.nutritionPer100g.sugar * servingMultiplier
                                    )
                                }g"
                            )
                            NutrientRow(
                                "Sodium",
                                "${
                                    String.format(
                                        "%.1f",
                                        food.nutritionPer100g.sodium * servingMultiplier
                                    )
                                }mg"
                            )
                            NutrientRow(
                                "Cholesterol",
                                "${
                                    String.format(
                                        "%.1f",
                                        food.nutritionPer100g.cholesterol * servingMultiplier
                                    )
                                }mg"
                            )

                            // Display vitamins and minerals if available
                            food.nutritionPer100g.vitamins.forEach { (name, amount) ->
                                NutrientRow(
                                    name,
                                    "${String.format("%.1f", amount * servingMultiplier)}mg"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add to Diary Button
        Button(
            onClick = {
                val foodItem = FoodItem(
                    name = food.name,
                    calories = totalCalories,
                    category = selectedCategory,
                    servingSize = "$amount $selectedServing",
                    amount = amount.toDoubleOrNull() ?: 1.0,
                    nutritionInfo = food.nutritionPer100g.copy(
                        protein = food.nutritionPer100g.protein * servingMultiplier,
                        carbs = food.nutritionPer100g.carbs * servingMultiplier,
                        fat = food.nutritionPer100g.fat * servingMultiplier,
                        fiber = food.nutritionPer100g.fiber * servingMultiplier,
                        sugar = food.nutritionPer100g.sugar * servingMultiplier,
                        sodium = food.nutritionPer100g.sodium * servingMultiplier,
                        cholesterol = food.nutritionPer100g.cholesterol * servingMultiplier,
                        vitamins = food.nutritionPer100g.vitamins.mapValues { it.value * servingMultiplier }
                    )
                )
                // Mark as recently added to diary
                foodRepository.markAsAddedToDiary(food)
                onAddToLog(foodItem)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text("Add To Diary", color = Color.White, fontSize = 16.sp)
        }
    }
}

fun calculateMacroPercentage(macroGrams: Double, macroType: String, totalCalories: Double): String {
    if (totalCalories <= 0 || macroGrams <= 0) return "0%"

    val caloriesFromMacro = when (macroType.lowercase()) {
        "protein" -> macroGrams * 4  // 4 calories per gram
        "net carbs", "carbs" -> macroGrams * 4  // 4 calories per gram
        "fat" -> macroGrams * 9  // 9 calories per gram
        else -> macroGrams * 4 // Fallback, though ideally 'macroType' should match
    }

    val percentage = (caloriesFromMacro / totalCalories) * 100
    return "${percentage.toInt()}%"
}

@Composable
fun MacroRow(
    name: String,
    grams: Double,
    totalCalories: Double,
    color: Color
) {
    val amount = if (grams < 0.1) "0g" else "${String.format("%.1f", grams)}g"
    val percentage = calculateMacroPercentage(grams, name, totalCalories)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$name ($percentage) - $amount",
            fontSize = 12.sp
        )
    }
}

@Composable
fun NutrientRow(
    name: String,
    amount: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontSize = 14.sp)
        Text(text = amount, fontSize = 14.sp, color = Color.Gray)
    }
}

// Helper function for calculating macro percentages
fun calculatePercentage(macroGrams: Double, totalCalories: Double): String {
    if (totalCalories <= 0) return "0%"

    val caloriesFromMacro = when {
        macroGrams <= 0 -> 0.0
        else -> when {
            // Protein: 4 calories per gram
            // Carbs: 4 calories per gram
            // Fat: 9 calories per gram
            macroGrams.toString().contains("protein", ignoreCase = true) -> macroGrams * 4
            macroGrams.toString().contains("carb", ignoreCase = true) -> macroGrams * 4
            macroGrams.toString().contains("fat", ignoreCase = true) -> macroGrams * 9
            else -> macroGrams * 4 // Default to 4 calories per gram
        }
    }

    val percentage = (caloriesFromMacro / totalCalories) * 100
    return "${percentage.toInt()}%"
}