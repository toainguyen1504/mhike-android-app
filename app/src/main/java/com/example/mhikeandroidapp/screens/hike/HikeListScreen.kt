package com.example.mhikeandroidapp.screens.hike

import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.mhikeandroidapp.R
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.ui.theme.MhikeAndroidAppTheme
import com.example.mhikeandroidapp.ui.theme.PrimaryGreen
import com.example.mhikeandroidapp.ui.theme.HighlightsGreen
import com.example.mhikeandroidapp.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

//mock data
val mockHikes = listOf(
    HikeModel(
        id = 1L,
        name = "Mount Bà Đen Mount Bà Đen Mount Bà Đen",
        location = "Tây Ninh",
        dateMs = System.currentTimeMillis(),
        parking = true,
        plannedLengthKm = 12.5,
        difficulty = "Hard",
        description = "Chuyến leo núi đầy thử thách với cảnh đẹp tuyệt vời. Chuyến leo núi đầy thử thách với cảnh đẹp tuyệt vời.",
        estimatedDurationMinutes = 240,
        groupSize = 5,
        latitude = 11.3602,
        longitude = 106.1427,
        imageUri = null,
        reminderMs = null
    )
)

@Composable
fun HikeListScreen(
    hikes: List<HikeModel>,
    onSearch: (String) -> Unit,
    onHikeClick: (HikeModel) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val LightPrimaryGreen = PrimaryGreen.copy(alpha = 0.1f)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header: Title + Search (không cuộn)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Hiker Management",
                style = MaterialTheme.typography.displayLarge,
                color = PrimaryGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearch(it.text)
                },
                placeholder = { Text("Search My Hikes", color = PrimaryGreen) },
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

            // Danh sách cuộn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hikes.isEmpty()) {
                    item {
                        Text(
                            text = "No hikes yet. Add one!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                } else {
                    items(hikes) { hike ->
                        HikeItem(hike = hike, onClick = { onHikeClick(hike) })
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // tránh bị che bởi nút Add
            }
        }

        // Floating Add Hike Button
        IconButton(
            onClick = { navController.navigate("add_hike") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 72.dp)
                .size(68.dp)
                .shadow(8.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(HighlightsGreen)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.plus_icon),
                contentDescription = "Add Hike",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


@Composable
fun HikeItem(hike: HikeModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TextSecondary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageModel = hike.imageUri?.let { Uri.parse(it) } ?: R.drawable.logo

            AsyncImage(
                model = imageModel,
                contentDescription = "Hike Image",
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(6.dp)) // giảm khoảng cách ảnh và nội dung

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hike.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp)) // giãn dòng

                Text(
                    text = hike.description ?: "No description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp)) // giãn dòng

                Text(
                    text = formatDate(hike.dateMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = { onClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.menu_dots_vertical_icon),
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


fun formatDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(epochMs))
}

@Preview(showBackground = true)
@Composable
fun HikeListScreenPreview() {
    MhikeAndroidAppTheme {
        val dummyNavController = rememberNavController()
        HikeListScreen(
            navController = dummyNavController,
            hikes = mockHikes,
            onSearch = {},
            onHikeClick = {},
            modifier = Modifier
        )
    }
}


