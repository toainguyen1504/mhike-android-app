package com.example.mhikeandroidapp.screens.hike

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mhikeandroidapp.R
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.ui.theme.ErrorRed
import com.example.mhikeandroidapp.ui.theme.PrimaryGreen
import com.example.mhikeandroidapp.ui.theme.HighlightsGreen
import com.example.mhikeandroidapp.ui.theme.TextBlack
import com.example.mhikeandroidapp.ui.theme.TextSecondary
import com.example.mhikeandroidapp.viewmodel.HikeViewModel
import java.io.File
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HikeListScreen(
    viewModel: HikeViewModel,
    onSearch: (String) -> Unit,
    onHikeClick: (HikeModel) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val LightPrimaryGreen = PrimaryGreen.copy(alpha = 0.1f)
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    val allHikes by viewModel.hikes.collectAsState(initial = emptyList())
    val filtered by viewModel.filteredHikes.collectAsState(initial = emptyList())
    val isSearching = searchQuery.text.isNotBlank()
    var isSyncing by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // Header: Title + Search (no scroll)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hiker Management",
                    style = MaterialTheme.typography.displayMedium,
                    color = PrimaryGreen
                )

                // State open/ close menu
                var expanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.menu_icon),
                            contentDescription = "Menu",
                            tint = TextBlack,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(8.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        // Sync hikes to cloud
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                isSyncing = true
                                // TODO: sync all to cloud
                                viewModel.syncAllHikesToCloud {
                                    isSyncing = false
                                    Toast.makeText(context, "All hikes synced to cloud!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            text = { Text("Sync all to cloud", color = TextBlack) },
                        )
                        Divider(color = Color.LightGray, thickness = 1.dp)

                        // Delete
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                showDeleteAllDialog = true // open dialog confirm
                            },
                            text = { Text("Delete all hikes", color = ErrorRed) },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.updateSearchQuery(it.text)
                },
                placeholder = { Text("Find hikes by name or location", color = PrimaryGreen.copy(0.6f)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search_icon),
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp),
                        tint = PrimaryGreen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = PrimaryGreen,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = PrimaryGreen,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = LightPrimaryGreen,
                    unfocusedContainerColor = LightPrimaryGreen
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // List hike
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    // Trường hợp không có bất kỳ hike nào trong DB
                    allHikes.isEmpty() && !isSearching -> {
                        item {
                            Text(
                                text = "No hikes yet. Add one!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }

                    // Trường hợp đang search nhưng không có kết quả
                    isSearching && filtered.isEmpty() -> {
                        item {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }

                    // Có dữ liệu để hiển thị (theo search nếu có query)
                    else -> {
                        items(filtered) { hike ->
                            HikeItem(
                                hike = hike,
                                onClick = { selected -> navController.navigate("hike_detail/${selected.id}") }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

        }

        // Floating Add Hike Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 72.dp)
                .size(68.dp)
                .shadow(8.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(HighlightsGreen)
                .clickable { navController.navigate("add_hike") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.plus_icon),
                contentDescription = "Add Hike",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    "Confirm Delete All",
                    color = ErrorRed,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    Text(
                        buildAnnotatedString {
                            append("Are you sure you want to delete ")
                            withStyle(
                                style = SpanStyle(
                                    color = ErrorRed,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("ALL hikes")
                            }
                            append("?")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This action will remove all hikes and their linked observations permanently.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextBlack,
                        fontStyle = FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        // gọi hàm xóa toàn bộ
                        viewModel.deleteAll()

                        Toast.makeText(context, "All hikes deleted successfully!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("OK", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = TextBlack)
                }
            }
        )
    }

    if (isSyncing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)), // overlay
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryGreen)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Syncing all hikes to cloud...", color = Color.White)
            }
        }
    }

}

@Composable
fun HikeItem(
    hike: HikeModel,
    onClick: (HikeModel) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick(hike) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TextSecondary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current

            val dateFormatter = remember {
                SimpleDateFormat("MMM dd, yyyy", Locale.US)
            }

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

            // Thumbnail
            AsyncImage(
                model = imageRequest,
                contentDescription = "Hike Image",
                modifier = Modifier
                    .size(84.dp)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Hike info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hike.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = hike.description ?: "No description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${hike.location} · ${dateFormatter.format(Date(hike.dateMs))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

fun formatDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(epochMs))
}