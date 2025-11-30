package com.example.mhikeandroidapp.screens.hike

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mhikeandroidapp.R
import com.example.mhikeandroidapp.data.AppDatabase
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.observation.ObservationModel
import com.example.mhikeandroidapp.data.observation.ObservationRepository
import com.example.mhikeandroidapp.ui.theme.*
import com.example.mhikeandroidapp.viewmodel.HikeViewModel
import com.example.mhikeandroidapp.viewmodel.ObservationViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class Observation(
    val id: Long,
    val content: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeDetailScreen(
    hike: HikeModel,
    hikeViewModel: HikeViewModel,
    observationViewModel: ObservationViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddObservation: () -> Unit,
    onBack: () -> Unit   // callback back
) {
    val context = LocalContext.current
    val LightPrimaryGreen = PrimaryGreen.copy(alpha = 0.2f)

    // utils
    val dateFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.US)
    }

    // state open confirm dialog
    var showDeleteDialog by remember { mutableStateOf(false) }


    // state open add observation dialog
    var showAddObservationDialog by remember { mutableStateOf(false) }

    // state open edit observation dialog
    var showEditObservationDialog by remember { mutableStateOf(false) }
    var editingObservation by remember { mutableStateOf<ObservationModel?>(null) }

    // state confirm delete dialog
    var showDeleteObservationDialog by remember { mutableStateOf(false) }
    var deletingObservation by remember { mutableStateOf<ObservationModel?>(null) }


    // init observation
    var observationText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }
    var imageObservationUri by remember { mutableStateOf("") }
    var observationError by remember { mutableStateOf(false) } // state  error
    var selectedObservation by remember { mutableStateOf<ObservationModel?>(null) }

    // collect observation list
    val observations by observationViewModel
        .getObservationsForHike(hike.id)
        .collectAsState(initial = emptyList())

    // Reset form function
    fun resetObservationForm() {
        observationText = ""
        comments = ""
        imageObservationUri = ""
        observationError = false
    }

    // thumbnail observation
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
                    imageObservationUri = file.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

    // capture image when add observation
    val photoFile = remember {
        File(context.filesDir, "obs_${System.currentTimeMillis()}.jpg")
    }
    val photoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageObservationUri = photoFile.absolutePath
            }
        }
    )

    // Sync
    val coroutineScope = rememberCoroutineScope()

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                        text = "Hike Details",
                        style = MaterialTheme.typography.displayMedium,
                        color = PrimaryGreen,

                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = PrimaryGreen
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                // Hike Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightPrimaryGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Thumbnail
                        Image(
                            painter = if (hike.imageUri.isNullOrBlank()) {
                                painterResource(id = R.drawable.default_img)
                            } else {
                                rememberAsyncImagePainter(model = hike.imageUri)
                            },
                            contentDescription = "Hike Image",
                            modifier = Modifier
                                .width(180.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info detail
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = hike.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextBlack,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Location: ${hike.location}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Date: ${dateFormatter.format(Date(hike.dateMs))}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Parking: ${if (hike.parking) "Yes" else "No"}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Length: ${hike.plannedLengthKm} km", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Difficulty: ${hike.difficulty}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            Spacer(modifier = Modifier.height(8.dp))

                            hike.description?.let {
                                Text("Description: $it", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            hike.estimatedDurationMinutes?.let {
                                Text("Duration: $it minutes", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            hike.groupSize?.let {
                                Text("Group Size: $it", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Edit and Delete Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            //edit btn
                            OutlinedButton(
                                onClick = onEdit,
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                                border = BorderStroke(1.dp, AccentBlue)
                            ) {
                                Text("Edit", color = AccentBlue)
                            }

                            // delete btn
                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                            ) {
                                Text("Delete")
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Sync to cloud Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color.Transparent),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .clickable {
                                        coroutineScope.launch {
                                            val observations = observationViewModel.getObservationsForHikeOnce(hike.id)
                                            hikeViewModel.syncToCloud(hike, observations)
                                            Toast.makeText(context, "Synced to cloud!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            ) {
                                Text(
                                    text = "☁️ Sync to cloud",
                                    color = PrimaryGreen,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Observation Section
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Observation", style = MaterialTheme.typography.titleMedium, color = HighlightsGreen)

                    // Add Observation Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(HighlightsGreen)
                            .clickable { showAddObservationDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Add Observation",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Observation Item
            if (observations.isEmpty()) {
                item {
                    // Empty observation
                    Text(
                        text = "No observations yet. Add one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(observations) { obs ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedObservation = obs },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TextSecondary),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            // image request
                            val imageRequest = if (!obs.imageObservationUri.isNullOrBlank()) {
                                ImageRequest.Builder(context)
                                    .data(File(obs.imageObservationUri!!))
                                    .crossfade(true)
                                    .error(R.drawable.default_img)
                                    .placeholder(R.drawable.default_img)
                                    .build()
                            } else {
                                ImageRequest.Builder(context)
                                    .data(R.drawable.default_img)
                                    .build()
                            }

                            // Thumbnail left
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = "Observation Image",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // observation info center
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    obs.observationText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                obs.comments?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    formatDateDetail(obs.timeMs),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            // action buttons right (vertical)
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        editingObservation = obs
                                        observationText = obs.observationText
                                        comments = obs.comments ?: ""
                                        imageObservationUri = obs.imageObservationUri ?: ""
                                        showEditObservationDialog = true
                                    }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.pencil_icon),
                                        contentDescription = "Edit Observation",
                                        tint = AccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Delete observation
                                IconButton(onClick = {
                                    deletingObservation = obs
                                    showDeleteObservationDialog = true
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.delete_icon),
                                        contentDescription = "Delete Observation",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }



            item {
                Spacer(modifier = Modifier.height(500.dp)) // add margin bottom to test SCROLL
            }
        }



        // AlertDialog confirm delete
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        "Confirm Delete",
                        color = ErrorRed,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column {
                        Text(
                            "Are you sure you want to delete this hike?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "All observations linked to this hike will also be permanently deleted.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextBlack,
                            fontStyle = FontStyle.Italic
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete() // delete

                            // successfully message
                            Toast.makeText(context, "Hike deleted successfully!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("OK", color = ErrorRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = TextBlack)
                    }
                }
            )
        }

        //  Add observation Dialog
        if (showAddObservationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddObservationDialog = false
                    resetObservationForm()
                },
                title = {
                    Text("Add Observation", style = MaterialTheme.typography.titleMedium, color = PrimaryGreen)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Thumbnail image picker
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                                .clickable {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val isDefaultImage = imageObservationUri.isBlank()
                            val painter = if (isDefaultImage) {
                                painterResource(id = R.drawable.default_img)
                            } else {
                                rememberAsyncImagePainter(model = imageObservationUri)
                            }

                            Image(
                                painter = painter,
                                contentDescription = "Observation Image",
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (isDefaultImage) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryBrown.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(
                                        "Choose Image",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Button(
                                    onClick = {
                                        cameraLauncher.launch(photoUri)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TextBlack.copy(alpha = 0.6f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Take a picture",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Observation Text (required)
                        OutlinedTextField(
                            value = observationText,
                            isError = observationError,
                            onValueChange = {
                                observationText = it
                                if (observationError && it.isNotBlank()) observationError = false
                            },
                            label = { Text("Observation *...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (observationError) {
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = ErrorRed,
                                    unfocusedIndicatorColor = ErrorRed,
                                    cursorColor = ErrorRed,
                                    focusedContainerColor = LightPrimaryGreen,
                                    unfocusedContainerColor = LightPrimaryGreen
                                )
                            } else {
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = PrimaryGreen,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = PrimaryGreen,
                                    focusedTextColor = PrimaryGreen,
                                    unfocusedTextColor = TextBlack,
                                    focusedContainerColor = LightPrimaryGreen,
                                    unfocusedContainerColor = LightPrimaryGreen
                                )
                            }
                        )

                        // Comments
                        OutlinedTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            label = { Text("Comments here...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = PrimaryGreen,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = PrimaryGreen,
                                focusedTextColor = PrimaryGreen,
                                unfocusedTextColor = TextBlack,
                                focusedContainerColor = LightPrimaryGreen,
                                unfocusedContainerColor = LightPrimaryGreen
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (observationText.isBlank()) {
                                // báo lỗi
                                observationError = true
                                Toast.makeText(context, "Observation text is required!", Toast.LENGTH_SHORT).show()
                            } else {
                                showAddObservationDialog = false
                                val observation = ObservationModel(
                                    hikeId = hike.id,
                                    observationText = observationText,
                                    timeMs = System.currentTimeMillis(),
                                    comments = comments.ifBlank { null },
//                                    imageObservationUri = imageObservationUri.ifBlank { null }
                                    imageObservationUri = imageObservationUri,
                                )

                                // save to db
                                observationViewModel.addObservation(observation)

                                Toast.makeText(context, "Observation added!", Toast.LENGTH_SHORT).show()
                                resetObservationForm()
                            }
                        }
                    ) {
                        Text("Save", color = PrimaryGreen,  style = MaterialTheme.typography.titleMedium )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddObservationDialog = false
                        resetObservationForm()
                    }) {
                        Text("Cancel", color = TextBlack)
                    }
                }
            )
        }

        // Observation Detail Dialog
        selectedObservation?.let { obs ->
            AlertDialog(
                onDismissRequest = { selectedObservation = null },
                title = {
                    Text(
                        "Observation Detail",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryGreen
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Image
                        val imageRequest = if (!obs.imageObservationUri.isNullOrBlank()) {
                            ImageRequest.Builder(context)
                                .data(File(obs.imageObservationUri!!))
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
                            contentDescription = "Observation Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Observation text
                        Text(
                            text = obs.observationText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Comments
                        obs.comments?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedObservation = null }) {
                        Text(
                            "Close",
                            color = TextBlack,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )
        }


        // Edit Observation Dialog
        if (showEditObservationDialog && editingObservation != null) {
            AlertDialog(
                onDismissRequest = {
                    showEditObservationDialog = false
                    resetObservationForm()
                    editingObservation = null
                },
                title = {
                    Text("Edit Observation", style = MaterialTheme.typography.titleMedium, color = PrimaryGreen)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Thumbnail image picker (giống add)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                                .clickable {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val isDefaultImage = imageObservationUri.isBlank()
                            val painter = if (isDefaultImage) {
                                painterResource(id = R.drawable.default_img)
                            } else {
                                rememberAsyncImagePainter(model = imageObservationUri)
                            }

                            Image(
                                painter = painter,
                                contentDescription = "Observation Image",
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (isDefaultImage) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryBrown.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(
                                        "Choose Image",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Button(
                                    onClick = {
                                        cameraLauncher.launch(photoUri)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TextBlack.copy(alpha = 0.6f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Take a picture",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Observation Text (required)
                        OutlinedTextField(
                            value = observationText,
                            isError = observationError,
                            onValueChange = {
                                observationText = it
                                if (observationError && it.isNotBlank()) observationError = false
                            },
                            label = { Text("Observation *") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (observationError) {
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = ErrorRed,
                                    unfocusedIndicatorColor = ErrorRed,
                                    cursorColor = ErrorRed,
                                    focusedContainerColor = LightPrimaryGreen,
                                    unfocusedContainerColor = LightPrimaryGreen
                                )
                            } else {
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = PrimaryGreen,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = PrimaryGreen,
                                    focusedTextColor = PrimaryGreen,
                                    unfocusedTextColor = TextBlack,
                                    focusedContainerColor = LightPrimaryGreen,
                                    unfocusedContainerColor = LightPrimaryGreen
                                )
                            }
                        )

                        // Comments
                        OutlinedTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            label = { Text("Comments here...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = PrimaryGreen,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = PrimaryGreen,
                                focusedTextColor = PrimaryGreen,
                                unfocusedTextColor = TextBlack,
                                focusedContainerColor = LightPrimaryGreen,
                                unfocusedContainerColor = LightPrimaryGreen
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (observationText.isBlank()) {
                                observationError = true
                                Toast.makeText(context, "Observation text is required!", Toast.LENGTH_SHORT).show()
                            } else {
                                showEditObservationDialog = false
                                val updated = editingObservation!!.copy(
                                    observationText = observationText,
                                    comments = comments.ifBlank { null },
//                                    imageObservationUri = imageObservationUri.ifBlank { null }
                                    imageObservationUri = imageObservationUri,
                                )

                                observationViewModel.updateObservation(updated)

                                Toast.makeText(context, "Observation updated!", Toast.LENGTH_SHORT).show()
                                resetObservationForm()
                                editingObservation = null
                            }
                        }
                    ) {
                        Text("Save", color = PrimaryGreen, style = MaterialTheme.typography.titleMedium )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditObservationDialog = false
                        resetObservationForm()
                        editingObservation = null
                    }) {
                        Text("Cancel", color = TextBlack)
                    }
                }
            )
        }

        // AlertDialog confirm delete for observation
        if (showDeleteObservationDialog && deletingObservation != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteObservationDialog = false
                    deletingObservation = null
                },
                title = {
                    Text(
                        "Confirm Delete Observation",
                        color = ErrorRed,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to delete this observation?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteObservationDialog = false
                            deletingObservation?.let {
                                observationViewModel.deleteObservation(it)
                                Toast.makeText(context, "Observation deleted!", Toast.LENGTH_SHORT).show()
                            }
                            deletingObservation = null
                        }
                    ) {
                        Text("OK", color = ErrorRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteObservationDialog = false
                        deletingObservation = null
                    }) {
                        Text("Cancel", color = TextBlack)
                    }
                }
            )
        }

    }
}

fun formatDateDetail(epochMs: Long): String {
    val sdf = SimpleDateFormat("HH:mm · dd MMM", Locale.getDefault())
    return sdf.format(Date(epochMs))
}
