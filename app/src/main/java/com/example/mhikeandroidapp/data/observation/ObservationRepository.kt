package com.example.mhikeandroidapp.data.observation

import com.example.mhikeandroidapp.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class ObservationRepository(private val db: AppDatabase) {

    fun getObservationsForHikeFlow(hikeId: Long): Flow<List<ObservationModel>> =
        db.observationDao().getObservationsForHikeFlow(hikeId)

    suspend fun getObservationsForHike(hikeId: Long): List<ObservationModel> =
        db.observationDao().getObservationsForHike(hikeId)

    suspend fun insertObservation(observation: ObservationModel): Long =
        db.observationDao().insertObservation(observation)

    suspend fun updateObservation(observation: ObservationModel) =
        db.observationDao().updateObservation(observation)

    suspend fun deleteObservation(observation: ObservationModel) =
        db.observationDao().deleteObservation(observation)

    suspend fun deleteAllForHike(hikeId: Long) =
        db.observationDao().deleteAllForHike(hikeId)

    // sync
    suspend fun getByHikeId(hikeId: Long): List<ObservationModel> =
        db.observationDao().getByHikeId(hikeId)
}

