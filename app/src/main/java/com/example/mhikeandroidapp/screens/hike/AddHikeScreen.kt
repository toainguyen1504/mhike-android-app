package com.example.mhikeandroidapp.screens.hike

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mhikeandroidapp.R
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.ui.theme.ErrorRed
import com.example.mhikeandroidapp.ui.theme.HighlightsGreen
import com.example.mhikeandroidapp.ui.theme.PrimaryGreen
import com.example.mhikeandroidapp.ui.theme.TextBlack
import com.example.mhikeandroidapp.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer
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
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var parking: Boolean? by remember { mutableStateOf(null) }
    var lengthKm by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var groupSize by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }

    // thumbnail hike
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val contentResolver = context.contentResolver

                // Tạo file mới trong bộ nhớ riêng của app
                val fileName = "hike_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)

                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Lưu đường dẫn file thay vì content://
                    imageUri = file.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

    // difficulty
    val difficultyOptions = listOf("Easy", "Medium", "Hard")
    var expanded by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf("") }

    // parking
    val parkingOptions = listOf("Yes", "No")
    var parkingExpanded by remember { mutableStateOf(false) }


    // state errors
    var nameError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var lengthError by remember { mutableStateOf(false) }
    var difficultyError by remember { mutableStateOf(false) }
    var parkingError by remember { mutableStateOf(false) }

    // check validate
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                location.isNotBlank() &&
                dateMs > 0 &&
                lengthKm.toDoubleOrNull() != null &&
                difficulty.isNotBlank() &&
                parking != null
    }

    // Confirm info before add hike
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingHike by remember { mutableStateOf<HikeModel?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onBack)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.back_icon),
                                contentDescription = "Back",
                                tint = TextBlack,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Back",
                                color = TextBlack,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Add New Hike",
                        style = MaterialTheme.typography.displayMedium,
                        color = PrimaryGreen
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = PrimaryGreen
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {

            fun Modifier.inputModifier() = this
                .fillMaxWidth()
                .height(64.dp)

            // Reset init state
            fun resetForm() {
                name = ""
                location = ""
                dateMs = System.currentTimeMillis()
                parking = null
                lengthKm = ""
                description = ""
                duration = ""
                groupSize = ""
                imageUri = ""
                difficulty = ""
            }

            val inputColors = TextFieldDefaults.colors(
                focusedIndicatorColor = PrimaryGreen,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryGreen,
                focusedTextColor = PrimaryGreen,
                unfocusedTextColor = TextBlack,
                focusedContainerColor = LightPrimaryGreen,
                unfocusedContainerColor = LightPrimaryGreen
            )

            // body scroll
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding() // add padding = keyboard's height
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightPrimaryGreen)
                        .padding(16.dp)
                ) {

                    // Thumbnail
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                                .clickable {
                                    imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val isDefaultImage = imageUri.isBlank()
                            val painter = if (isDefaultImage) {
                                painterResource(id = R.drawable.default_img)
                            } else {
                                rememberAsyncImagePainter(model = imageUri)
                            }

                            Image(
                                painter = painter,
                                contentDescription = "Hike Thumbnail",
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (isDefaultImage) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.4f)) // lớp phủ đen nhẹ
                                )
                            }

                            Text(
                                text = "Choose Image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Hike name
                    item {
                        OutlinedTextField(
                            value = name,
                            isError = nameError,
                            onValueChange = { name = it },
                            label = { Text("Hike Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            colors = if (nameError) {
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = ErrorRed,
                                    unfocusedIndicatorColor = ErrorRed,
                                    cursorColor = ErrorRed,
                                    focusedContainerColor = LightPrimaryGreen,
                                    unfocusedContainerColor = LightPrimaryGreen
                                )
                            } else inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Location
                    item {
                        OutlinedTextField(
                            value = location,
                            isError = locationError,
                            onValueChange = { location = it },
                            label = { Text("Location *") },
                            modifier = Modifier.inputModifier(),
                            colors = if (locationError ) {
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = ErrorRed,
                                    unfocusedIndicatorColor = ErrorRed,
                                    cursorColor = ErrorRed,
                                    focusedContainerColor = LightPrimaryGreen,
                                    unfocusedContainerColor = LightPrimaryGreen
                                )
                            } else inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Date
                    item {
                        val dateFormatter = remember {
                            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
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
                                contentDescription = "Pick Date *",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Date*: ${dateFormatter.format(Date(dateMs))}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Parking
                    item {
                        ExposedDropdownMenuBox(
                            expanded = parkingExpanded,
                            onExpandedChange = { parkingExpanded = !parkingExpanded }
                        ) {
                            OutlinedTextField(
                                value = when (parking) {
                                    null -> ""
                                    true -> "Yes"
                                    false -> "No"
                                },
                                label = { Text("Parking available *") },
                                onValueChange = {},
                                readOnly = true,
                                isError = parkingError,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(parkingExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (parkingError && parking == null) ErrorRed
                                    else TextBlack
                                ),
                                colors = if (parkingError) {
                                    TextFieldDefaults.colors(
                                        focusedIndicatorColor = ErrorRed,
                                        unfocusedIndicatorColor = ErrorRed,
                                        cursorColor = ErrorRed,
                                        focusedContainerColor = LightPrimaryGreen,
                                        unfocusedContainerColor = LightPrimaryGreen
                                    )
                                } else inputColors
                            )

                            ExposedDropdownMenu(
                                expanded = parkingExpanded,
                                onDismissRequest = { parkingExpanded = false }
                            ) {
                                parkingOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            parking = (opt == "Yes")
                                            parkingError = false
                                            parkingExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Lenght
                    item {
                        OutlinedTextField(
                            value = lengthKm,
                            isError = lengthError,
                            onValueChange = { lengthKm = it },
                            label = { Text("Length (km) *") },
                            modifier = Modifier.inputModifier(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = if (lengthError) {
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = ErrorRed,
                                    unfocusedIndicatorColor = ErrorRed,
                                    cursorColor = ErrorRed,
                                    focusedContainerColor = LightPrimaryGreen,
                                    unfocusedContainerColor = LightPrimaryGreen
                                )
                            } else inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Level of difficulty
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = if (difficulty.isBlank()) "" else difficulty,
                                label = { Text("Level of difficulty *") },
                                onValueChange = {},
                                readOnly = true,
                                isError = difficultyError,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (difficultyError && difficulty.isBlank()) ErrorRed
                                    else TextBlack
                                ),
                                colors = if (difficultyError) {
                                    TextFieldDefaults.colors(
                                        focusedIndicatorColor = ErrorRed,
                                        unfocusedIndicatorColor = ErrorRed,
                                        cursorColor = ErrorRed,
                                        focusedContainerColor = LightPrimaryGreen,
                                        unfocusedContainerColor = LightPrimaryGreen
                                    )
                                } else inputColors
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
                            label = { Text("Estimated duration (minutes)") },
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
                            label = { Text("Group size") },
                            modifier = Modifier.inputModifier(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = inputColors
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Reset form btn - 40%
                Button(
                    onClick = { resetForm() }, // reset form
                    modifier = Modifier
                        .weight(0.4f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextSecondary.copy(alpha = 0.2f),
                        contentColor = TextBlack
                    )
                ) {
                    Text("Reset")
                }


                fun normalize(text: String): String {
                    return Normalizer.normalize(text, Normalizer.Form.NFD)
                        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                        .lowercase()
                }

                // Save btn - 60%
                Button(
                    onClick = {
                        val valid = isValid()

                        if (!valid) {
                            nameError = name.isBlank()
                            locationError = location.isBlank()
                            lengthError = lengthKm.toDoubleOrNull() == null
                            difficultyError = difficulty.isBlank()
                            parkingError = (parking == null)

                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val hike = HikeModel(
                            name = name,
                            location = location,
                            dateMs = dateMs,
                            parking = parking == true,
                            plannedLengthKm = lengthKm.toDoubleOrNull() ?: 0.0,
                            difficulty = difficulty,
                            description = description.ifBlank { null },
                            estimatedDurationMinutes = duration.toIntOrNull(),
                            groupSize = groupSize.toIntOrNull(),
                            imageUri = imageUri,
                        )

                        // confirm before save to DB - show modal
                        pendingHike = hike
                        showConfirmDialog = true
                    },
                    modifier = Modifier
                        .weight(0.6f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HighlightsGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Hike")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }


        // Review hike dialog
        if (showConfirmDialog && pendingHike != null) {
            val hike = pendingHike!!
            val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Confirm Hike",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryGreen,
                            modifier = Modifier.weight(1f)
                        )

                        val context = LocalContext.current
                        val imageRequest = if (!hike.imageUri.isNullOrBlank()) {
                            ImageRequest.Builder(context)
                                .data(File(hike.imageUri))
                                .crossfade(true)
                                .error(R.drawable.default_img)
                                .placeholder(R.drawable.default_img)
                                .build()
                        } else {
                            ImageRequest.Builder(context)
                                .data(R.drawable.default_img)
                                .build()
                        }

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .size(48.dp) // nhỏ gọn
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Name: ${hike.name}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Location: ${hike.location}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Date: ${dateFormatter.format(Date(hike.dateMs))}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Length: ${hike.plannedLengthKm} km", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Difficulty: ${hike.difficulty}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Parking: ${if (hike.parking) "Yes" else "No"}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Duration: ${hike.estimatedDurationMinutes ?: "—"} minutes", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Group size: ${hike.groupSize ?: "—"}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                        Text("Description: ${hike.description ?: "—"}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            onSave(pendingHike!!) // save to DB
                            Toast.makeText(context, "Hike added successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighlightsGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Back", color = TextBlack)
                    }
                }
            )
        }

    }
}




