package com.example.mhikeandroidapp.data
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mhikeandroidapp.data.hike.HikeDao
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.observation.ObservationDao
import com.example.mhikeandroidapp.data.observation.ObservationModel

@Database(entities = [HikeModel::class, ObservationModel::class], version = 1, exportSchema = false)
//@TypeConverters(TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hikeDao(): HikeDao
    abstract fun observationDao(): ObservationDao
}
