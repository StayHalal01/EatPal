package com.example.eatpal.presentation.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eatpal.data.model.FoodItem

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
                Column(modifier = Modifier.selectableGroup()) {
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