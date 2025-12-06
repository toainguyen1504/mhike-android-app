package com.example.mhikeandroidapp.data.observation

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.mhikeandroidapp.data.hike.HikeModel

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

    val hikeId: Long,                 // FK Hike.id
    val observationText: String,     // required
    val timeMs: Long = System.currentTimeMillis(), // required - default

    val comments: String? = null,    // optional
    val imageObservationUri: String? = null     // optional
)