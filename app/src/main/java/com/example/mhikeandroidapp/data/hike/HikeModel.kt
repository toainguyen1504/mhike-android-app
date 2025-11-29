package com.example.mhikeandroidapp.data.hike

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hikes",
    indices = [
        Index(value = ["name"]),
        Index(value = ["location"])
    ]
)
data class HikeModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Required fields (theo đề)
    val name: String,              // Tên chuyến đi - required
    val location: String,          // Địa điểm - required
    val dateMs: Long,              // Ngày hike (epoch millis) - required
    val parking: Boolean,          // Có chỗ đậu xe? true=yes, false=no - required
    val plannedLengthKm: Double,   // Chiều dài dự kiến (km) - required
    val difficulty: String,        // Mức độ khó (Easy/Medium/Hard) - required

    // Optional (theo đề)
    val description: String? = null,

    // 2+ custom fields
    val estimatedDurationMinutes: Int? = null, // Thời gian dự kiến (phút)
    val groupSize: Int? = null,                // Số người tham gia

    // Extra cho GPS & Image & Reminder (feature g)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUri: String? = null,   // Avatar for hike
    val reminderMs: Long? = null,   // Thời điểm nhắc nhở (notification)

    // Meta
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis(),
)