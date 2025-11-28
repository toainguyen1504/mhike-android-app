package com.example.mhikeandroidapp.data.observation

import com.example.mhikeandroidapp.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class ObservationRepository(private val db: AppDatabase) {

    fun getObservationsForHikeFlow(hikeId: Long): Flow<List<ObservationModel>> =
        db.observationDao().getObservationsForHikeFlow(hikeId)

    suspend fun getObservationById(id: Long): ObservationModel? =
        db.observationDao().getObservationById(id)

    suspend fun insertObservation(observation: ObservationModel): Long =
        db.observationDao().insertObservation(observation)

    suspend fun updateObservation(observation: ObservationModel) =
        db.observationDao().updateObservation(observation)

    suspend fun deleteObservation(observation: ObservationModel) =
        db.observationDao().deleteObservation(observation)

    suspend fun deleteAllForHike(hikeId: Long) =
        db.observationDao().deleteAllForHike(hikeId)
}

