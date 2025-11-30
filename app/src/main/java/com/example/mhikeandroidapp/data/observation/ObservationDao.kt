package com.example.mhikeandroidapp.data.observation

import androidx.room.*
import com.example.mhikeandroidapp.data.observation.ObservationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    @Query("""
    SELECT * FROM observations
    WHERE hikeId = :hikeId
    ORDER BY timeMs ASC
""")
    suspend fun getObservationsForHike(hikeId: Long): List<ObservationModel> // for sync

    @Query("""
    SELECT * FROM observations
    WHERE hikeId = :hikeId
    ORDER BY timeMs ASC
""")
    fun getObservationsForHikeFlow(hikeId: Long): Flow<List<ObservationModel>> // return Flow<List<>> for ui


    @Query("SELECT * FROM observations WHERE id = :id LIMIT 1")
    suspend fun getObservationById(id: Long): ObservationModel?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObservation(observation: ObservationModel): Long

    @Update
    suspend fun updateObservation(observation: ObservationModel)

    @Delete
    suspend fun deleteObservation(observation: ObservationModel)

    @Query("DELETE FROM observations WHERE hikeId = :hikeId")
    suspend fun deleteAllForHike(hikeId: Long)
}