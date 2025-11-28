package com.example.mhikeandroidapp.data.hike

import com.example.mhikeandroidapp.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class HikeRepository(private val db: AppDatabase) {

    fun getAllHikesFlow(): Flow<List<HikeModel>> = db.hikeDao().getAllHikesFlow()

    suspend fun getAllHikes(): List<HikeModel> = db.hikeDao().getAllHikes()

    suspend fun getHikeById(id: Long): HikeModel? = db.hikeDao().getHikeById(id)

    suspend fun insertHike(hike: HikeModel): Long = db.hikeDao().insertHike(hike)

    suspend fun updateHike(hike: HikeModel) = db.hikeDao().updateHike(hike)

    suspend fun deleteHike(hike: HikeModel) = db.hikeDao().deleteHike(hike)

    fun searchHikes(query: String): Flow<List<HikeModel>> = db.hikeDao().searchHikesFlow(query)
}