package com.example.mhikeandroidapp.screens.hike

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mhikeandroidapp.R
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHikeScreen(
    onBack: () -> Unit,
    onSave: (HikeModel) -> Unit
) {
    val LightPrimaryGreen = PrimaryGreen.copy(alpha = 0.1f)

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var parking by remember { mutableStateOf(false) }
    var lengthKm by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var groupSize by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            fun Modifier.inputModifier() = this
                .fillMaxWidth()
                .height(64.dp)

            val inputColors = TextFieldDefaults.colors(
                focusedIndicatorColor = PrimaryGreen,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryGreen,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedContainerColor = LightPrimaryGreen,
                unfocusedContainerColor = LightPrimaryGreen
            )

            // Title
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Add New Hike",
                style = MaterialTheme.typography.displayLarge,
                color = PrimaryGreen
            )
            Spacer(modifier = Modifier.height(16.dp))

            // scroll
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightPrimaryGreen)
                        .padding(16.dp)
                ) {

                    // Thumbnail
                    item {
                        // Text("Thumbnail", style = MaterialTheme.typography.bodyMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                                .clickable {
                                    // TODO: mở chọn ảnh từ thư viện nếu muốn
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.default_img),
                                contentDescription = "Hike Thumbnail",
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = "Choose Image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Hike name
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Hike Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            colors = inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Location
                    item {

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            modifier = Modifier.inputModifier(),
                            colors = inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Date
                    item {
                        val context = LocalContext.current
                        val dateFormatter = remember {
                            SimpleDateFormat(
                                "dd MMM yyyy",
                                Locale.getDefault()
                            )
                        }
                        val calendar = remember { Calendar.getInstance() }
                        calendar.timeInMillis = dateMs

                        val datePickerDialog = remember {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(year, month, dayOfMonth)
                                    dateMs = calendar.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pick Date",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Date: ${dateFormatter.format(Date(dateMs))}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Lenght
                    item {
                        OutlinedTextField(
                            value = lengthKm,
                            onValueChange = { lengthKm = it },
                            label = { Text("Length (km)") },
                            modifier = Modifier.inputModifier(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Level of difficulty
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        val difficultyOptions = listOf("Easy", "Medium", "Hard")
                        var expanded by remember { mutableStateOf(false) }
                        var difficulty by remember { mutableStateOf("") }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = difficulty,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = {
                                    if (difficulty.isBlank()) {
                                        Text(
                                            text = "Choose difficulty",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = inputColors
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                difficultyOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            difficulty = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Description
                    item {

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.inputModifier(),
                            colors = inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Duration
                    item {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Estimated Duration (minutes)") },
                            modifier = Modifier.inputModifier(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Size members
                    item {
                        OutlinedTextField(
                            value = groupSize,
                            onValueChange = { groupSize = it },
                            label = { Text("Group Size") },
                            modifier = Modifier.inputModifier(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Parking
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = parking, onCheckedChange = { parking = it })
                            Text("Parking Available")
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBack) {
                    Text("Back")
                }
                Button(onClick = {
                    val hike = HikeModel(
                        name = name,
                        location = location,
                        dateMs = dateMs,
                        parking = parking,
                        plannedLengthKm = lengthKm.toDoubleOrNull() ?: 0.0,
                        difficulty = difficulty,
                        description = description.ifBlank { null },
                        estimatedDurationMinutes = duration.toIntOrNull(),
                        groupSize = groupSize.toIntOrNull(),
                        imageUri = imageUri
                    )
                    onSave(hike)
                }) {
                    Text("Add Hike")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

}
