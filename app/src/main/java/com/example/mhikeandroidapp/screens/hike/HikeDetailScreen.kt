package com.example.mhikeandroidapp.screens.hike

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.mhikeandroidapp.R
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class Observation(
    val id: Long,
    val content: String,
    val timestamp: Long
)

val mockObservation = Observation(
    id = 1L,
    content = "Trail condition was muddy",
    timestamp = 1764000000000
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikeDetailScreen(
    hike: HikeModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddObservation: () -> Unit,
    onBack: () -> Unit   // callback back
) {
    val LightPrimaryGreen = PrimaryGreen.copy(alpha = 0.2f)

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
                                .width(160.dp)
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
                            Text("Date: ${formatDate(hike.dateMs)}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
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
                            hike.latitude?.let {
                                Text("Latitude: $it", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            hike.longitude?.let {
                                Text("Longitude: $it", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            hike.reminderMs?.let {
                                Text("Reminder: ${formatDate(it)}", style = MaterialTheme.typography.bodyLarge, color = TextBlack)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onEdit,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Text("Edit")
                            }
                            Button(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                            ) {
                                Text("Delete")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            item {
                // Observation Section
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Observation", style = MaterialTheme.typography.titleMedium, color = HighlightsGreen)

                    // Add Observation Button
                    IconButton(
                        onClick = onAddObservation,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(HighlightsGreen)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.plus_icon),
                            contentDescription = "Add Observation",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                // Mock Observation Item
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryGreen),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(mockObservation.content, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(formatDate(mockObservation.timestamp), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(500.dp)) // thêm margin bottom để test cuộn
            }
        }
    }



}

