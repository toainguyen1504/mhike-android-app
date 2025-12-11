package com.example.mhikeandroidapp.data.hike
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HikeDao {

    // get List hikes (live)
    @Query("SELECT * FROM hikes ORDER BY createdAtMs DESC")
    fun getAllHikesFlow(): Flow<List<HikeModel>>

    // get all hikes
    @Query("SELECT * FROM hikes ORDER BY dateMs ASC")
    suspend fun getAllHikes(): List<HikeModel>

    // get 1 hike by id
    @Query("SELECT * FROM hikes WHERE id = :id LIMIT 1")
    suspend fun getHikeById(id: Long): HikeModel?

    // ADD hike
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHike(hike: HikeModel): Long

    // UPDATE
    @Update
    suspend fun updateHike(hike: HikeModel)

    // DELETE
    @Delete
    suspend fun deleteHike(hike: HikeModel)

    // DELETE ALL (dev/test)
    @Query("DELETE FROM hikes")
    suspend fun deleteAll()

    // Search by name/location
    @Query("""
        SELECT * FROM hikes
        WHERE name LIKE '%' || :query || '%'
           OR location LIKE '%' || :query || '%'
    """)
    fun searchHikesFlow(query: String): Flow<List<HikeModel>>
}