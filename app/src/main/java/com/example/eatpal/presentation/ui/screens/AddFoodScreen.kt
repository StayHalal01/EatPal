package com.example.eatpal.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.LaunchedEffect
import com.example.eatpal.data.model.FoodDatabase
import com.example.eatpal.data.model.FoodItem
import com.example.eatpal.data.repository.FoodRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onDismiss: () -> Unit,
    onAddFood: (FoodItem) -> Unit,
    defaultCategory: String = "Breakfast"
) {
    val foodRepository = remember { FoodRepository.getInstance() }
    var currentView by remember { mutableStateOf("list") } // "list" or "detail"
    var selectedFood by remember { mutableStateOf<FoodDatabase?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }

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
                        Color.Gray,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (currentView == "list") {
            FoodListView(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it },
                foodRepository = foodRepository,
                onFoodSelect = { food ->
                    selectedFood = food
                    currentView = "detail"
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
    }
}

@Composable
fun FoodListView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: String,
    onTabChange: (String) -> Unit,
    foodRepository: FoodRepository,
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
            placeholder = { Text("Search") },
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

        // Tabs (removed Custom tab)
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
            Text("Result", fontWeight = FontWeight.Medium)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Food Items
        val foods = when (selectedTab) {
            "Favourite" -> {
                val favorites = foodRepository.getFavorites()
                val filteredFavorites = if (searchQuery.isEmpty()) {
                    favorites
                } else {
                    favorites.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                sortFoods(filteredFavorites, sortBy)
            }

            else -> sortFoods(foodRepository.searchFoods(searchQuery), sortBy)
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(foods) { food ->
                FoodItemRow(
                    food = food,
                    onClick = { onFoodSelect(food) }
                )
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
        Text(
            text = food.name,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
    Divider(color = Color.LightGray, thickness = 0.5.dp)
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
    var selectedServing by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf(defaultCategory) }
    var showNutritionExpanded by remember { mutableStateOf(true) }
    var showNutrientsExpanded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(foodRepository.isFavorite(food)) }

    // Force recomposition when favorite status might have changed
    LaunchedEffect(food.name) {
        isFavorite = foodRepository.isFavorite(food)
    }

    val categories = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val servingSizes = listOf("Small", "Medium", "Large")

    // Calculate serving multiplier based on selected serving size
    val servingMultiplier = when (selectedServing) {
        "Small" -> 0.5
        "Medium" -> 1.0
        "Large" -> 1.5
        else -> 1.0
    }

    // Calculate nutrition values based on amount and serving size
    val baseMultiplier = (amount.toDoubleOrNull() ?: 1.0) * servingMultiplier
    val totalCalories = (food.caloriesPer100g * baseMultiplier).toInt()

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
                Icon(Icons.Default.Close, contentDescription = "Back")
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
                        servingSizes.forEach { serving ->
                            DropdownMenuItem(
                                text = { Text(serving) },
                                onClick = {
                                    selectedServing = serving
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
                                    CircularProgressIndicator(
                                        progress = 1f,
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
                                        percentage = "65%",
                                        amount = "${
                                            String.format(
                                                "%.1f",
                                                food.nutritionPer100g.protein * baseMultiplier
                                            )
                                        }g",
                                        color = Color(0xFF4CAF50)
                                    )
                                    MacroRow(
                                        name = "Net Carbs",
                                        percentage = "2%",
                                        amount = "${
                                            String.format(
                                                "%.1f",
                                                food.nutritionPer100g.carbs * baseMultiplier
                                            )
                                        }g",
                                        color = Color(0xFF2196F3)
                                    )
                                    MacroRow(
                                        name = "Fat",
                                        percentage = "35%",
                                        amount = "${
                                            String.format(
                                                "%.1f",
                                                food.nutritionPer100g.fat * baseMultiplier
                                            )
                                        }g",
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
                                        food.nutritionPer100g.fiber * baseMultiplier
                                    )
                                }mg"
                            )
                            NutrientRow(
                                "Sugar",
                                "${
                                    String.format(
                                        "%.1f",
                                        food.nutritionPer100g.sugar * baseMultiplier
                                    )
                                }mg"
                            )
                            NutrientRow(
                                "Sodium",
                                "${
                                    String.format(
                                        "%.1f",
                                        food.nutritionPer100g.sodium * baseMultiplier
                                    )
                                }mg"
                            )
                            NutrientRow(
                                "Cholesterol",
                                "${
                                    String.format(
                                        "%.1f",
                                        food.nutritionPer100g.cholesterol * baseMultiplier
                                    )
                                }mg"
                            )
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
                    servingSize = selectedServing,
                    amount = amount.toDoubleOrNull() ?: 1.0,
                    nutritionInfo = food.nutritionPer100g.copy(
                        protein = food.nutritionPer100g.protein * baseMultiplier,
                        carbs = food.nutritionPer100g.carbs * baseMultiplier,
                        fat = food.nutritionPer100g.fat * baseMultiplier,
                        fiber = food.nutritionPer100g.fiber * baseMultiplier,
                        sugar = food.nutritionPer100g.sugar * baseMultiplier,
                        sodium = food.nutritionPer100g.sodium * baseMultiplier,
                        cholesterol = food.nutritionPer100g.cholesterol * baseMultiplier
                    )
                )
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

@Composable
fun MacroRow(
    name: String,
    percentage: String,
    amount: String,
    color: Color
) {
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

// Helper function for sorting foods
fun sortFoods(foods: List<FoodDatabase>, sortBy: String): List<FoodDatabase> {
    return when (sortBy) {
        "A-Z" -> foods.sortedBy { it.name }
        "Z-A" -> foods.sortedByDescending { it.name }
        "Recently Added" -> foods.reversed() // Assuming last added items are at the end
        else -> foods // Most Relevant (default order)
    }
}
