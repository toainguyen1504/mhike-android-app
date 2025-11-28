package com.example.mhikeandroidapp.data.observation
import com.example.mhikeandroidapp.data.hike.HikeModel

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "observations",
    foreignKeys = [
        ForeignKey(
            entity = HikeModel::class,
            parentColumns = ["id"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hikeId"])]
)
data class ObservationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val hikeId: Long,           // FK → Hike.id
    val note: String,           // Nội dung ghi chú
    val timeMs: Long,           // Thời gian ghi chú (epoch millis)
    val photoUri: String? = null, // Ảnh đính kèm (nếu có)

    val createdAtMs: Long = System.currentTimeMillis()
)