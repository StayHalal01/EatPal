package com.example.eatpal.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import java.util.Calendar

@Composable
fun DateSelector(
    currentDate: Date,
    onNavigateDay: (Boolean) -> Unit,
    onDateClick: () -> Unit = {}
) {
    val today = Calendar.getInstance().time
    val isToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate) ==
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today)

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
                modifier = Modifier.clickable { onNavigateDay(false) }
            )
            Text(
                "${if (isToday) "Today, " else ""}${
                    SimpleDateFormat(
                        "d MMMM yyyy",
                        Locale.getDefault()
                    ).format(currentDate)
                }",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onDateClick() }
            )

            val nextDay = Calendar.getInstance().apply {
                time = currentDate
                add(Calendar.DAY_OF_MONTH, 1)
            }.time

            val canGoForward = !nextDay.after(today)

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Next day",
                tint = if (canGoForward) Color.Unspecified else Color.Gray,
                modifier = Modifier.clickable {
                    if (canGoForward) onNavigateDay(true)
                }
            )
        }
    }
}
