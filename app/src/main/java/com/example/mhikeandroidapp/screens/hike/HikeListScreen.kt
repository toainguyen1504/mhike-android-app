package com.example.mhikeandroidapp.screens.hike

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mhikeandroidapp.R
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.ui.theme.MhikeAndroidAppTheme
import com.example.mhikeandroidapp.ui.theme.PrimaryGreen
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
        imageUri = null, // hoặc "file:///android_asset/sample_hike.jpg"
        reminderMs = null
    )
)

@Composable
fun HikeListScreen(
    hikes: List<HikeModel>,
    onSearch: (String) -> Unit,
    onHikeClick: (HikeModel) -> Unit,
    modifier: Modifier
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        val PrimaryGreen = Color(0xFF2E7D32)
        val LightPrimaryGreen = PrimaryGreen.copy(alpha = 0.1f)

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Hiker Management",
            style = MaterialTheme.typography.displayLarge,
            color = PrimaryGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearch(it.text)
            },
            placeholder = {
                Text(
                    text = "Search My Hikes",
                    color = PrimaryGreen
                )
            },
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

        if (hikes.isEmpty()) {
            Text(
                text = "No hikes yet. Add one!",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        } else {
            Column {
                hikes.forEach { hike ->
                    HikeItem(hike = hike, onClick = { onHikeClick(hike) })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
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

            Spacer(modifier = Modifier.width(4.dp)) // giảm khoảng cách ảnh và nội dung

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
        HikeListScreen(
            modifier = Modifier,
            hikes = mockHikes,
            onSearch = {},
            onHikeClick = {}
        )
    }
}


