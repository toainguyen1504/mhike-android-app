package com.example.mhikeandroidapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.hike.HikeRepository
import com.example.mhikeandroidapp.data.observation.ObservationModel
import com.example.mhikeandroidapp.data.observation.ObservationRepository
import com.example.mhikeandroidapp.utils.SyncUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HikeViewModel(
    private val hikeRepository: HikeRepository,
    private val observationRepository: ObservationRepository
) : ViewModel() {

    // All hikes sorted by date (latest first)
    val hikes = hikeRepository.getAllHikesFlow()
        .map { it.sortedByDescending { h -> h.createdAtMs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sync status for UI feedback
    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    // Sync a single hike by ID
    fun syncToCloud(hikeId: Long) {
        viewModelScope.launch {
            _syncStatus.value = "Syncing..."
            try {
                val hike = hikeRepository.getHikeById(hikeId)
                if (hike == null) {
                    _syncStatus.value = "Hike $hikeId not found"
                    return@launch
                }
                val observations = observationRepository.getObservationsForHike(hikeId)
                SyncUtils.syncHikeToCloud(hike, observations)
                _syncStatus.value = "Sync successful"
            } catch (e: Exception) {
                _syncStatus.value = "Sync error: ${e.message}"
            }
        }
    }

    // Sync a single hike with its observations
    fun syncToCloud(hike: HikeModel, observations: List<ObservationModel>) {
        viewModelScope.launch {
            _syncStatus.value = "Syncing..."
            try {
                SyncUtils.syncHikeToCloud(hike, observations)
                _syncStatus.value = "Sync successful"
            } catch (e: Exception) {
                _syncStatus.value = "Sync error: ${e.message}"
            }
        }
    }

    // Sync all hikes in the local database
    fun syncAllHikesToCloud(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _syncStatus.value = "Syncing all hikes..."
            val allHikes = hikes.value
            for (hike in allHikes) {
                val observations = observationRepository.getByHikeId(hike.id)
                try {
                    SyncUtils.syncHikeToCloud(hike, observations)
                } catch (e: Exception) {
                    Log.e("HikeViewModel", "Sync failed for hike ${hike.id}: ${e.message}")
                }
            }
            _syncStatus.value = "All hikes synced successfully"
            onDone()
        }
    }

    // state search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // filters
    private val _filterLengthRange = MutableStateFlow(0f..100f)
    private val _filterDifficulty = MutableStateFlow<String?>(null)

    fun updateLengthRange(range: ClosedFloatingPointRange<Float>) {
        _filterLengthRange.value = range
    }

    fun updateDifficulty(difficulty: String?) {
        _filterDifficulty.value = difficulty
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _filterLengthRange.value = 0f..1000f
        _filterDifficulty.value = null
    }

    // combine search and filter
    val filteredHikes = combine(
        hikeRepository.getAllHikesFlow(),
        _searchQuery,
        _filterLengthRange,
        _filterDifficulty
    ) { hikes, query, range, difficulty ->
        hikes.filter { hike ->
            val matchesQuery = query.isBlank() ||
                    hike.name.contains(query, ignoreCase = true) ||
                    hike.location.contains(query, ignoreCase = true)

            val matchesLength = hike.plannedLengthKm in range.start..range.endInclusive

            val matchesDifficulty = difficulty.isNullOrBlank() ||
                    hike.difficulty.equals(difficulty, ignoreCase = true)

            matchesQuery && matchesLength && matchesDifficulty
        }.sortedByDescending { it.dateMs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // CRUD operations
    fun addHike(hike: HikeModel) {
        viewModelScope.launch {
            hikeRepository.insertHike(hike)
            // Optional: force refresh if needed
            _searchQuery.value = ""
        }
    }

    suspend fun getHikeById(id: Long): HikeModel? {
        return hikeRepository.getHikeById(id)
    }

    fun updateHike(hike: HikeModel) {
        viewModelScope.launch {
            hikeRepository.updateHike(hike)
        }
    }

    fun deleteHike(hike: HikeModel) {
        viewModelScope.launch {
            hikeRepository.deleteHike(hike)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            hikeRepository.deleteAll()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class HikeViewModelFactory(
    private val hikeRepository: HikeRepository,
    private val observationRepository: ObservationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HikeViewModel::class.java)) {
            return HikeViewModel(hikeRepository, observationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}