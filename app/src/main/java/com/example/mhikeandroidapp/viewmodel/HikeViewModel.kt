package com.example.mhikeandroidapp.viewmodel

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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HikeViewModel(
    private val hikeRepository: HikeRepository,
    private val observationRepository: ObservationRepository
) : ViewModel() {

    val hikes = hikeRepository.getAllHikesFlow()
        .map { it.sortedByDescending { h -> h.dateMs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus

    fun syncToCloud(hikeId: Long) {
        viewModelScope.launch {
            _syncStatus.value = "Đang đồng bộ..."
            try {
                val hike = hikeRepository.getHikeById(hikeId)
                if (hike == null) {
                    _syncStatus.value = "Không tìm thấy hike $hikeId"
                    return@launch
                }
                val observations = observationRepository.getObservationsForHike(hikeId)
                SyncUtils.syncHikeToCloud(hike, observations)
                _syncStatus.value = "Đồng bộ thành công"
            } catch (e: Exception) {
                _syncStatus.value = "Lỗi đồng bộ: ${e.message}"
            }
        }
    }

    fun syncToCloud(hike: HikeModel, observations: List<ObservationModel>) {
        viewModelScope.launch {
            _syncStatus.value = "Đang đồng bộ..."
            try {
                SyncUtils.syncHikeToCloud(hike, observations)
                _syncStatus.value = "Đồng bộ thành công"
            } catch (e: Exception) {
                _syncStatus.value = "Lỗi đồng bộ: ${e.message}"
            }
        }
    }


    // state search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredHikes = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) hikeRepository.getAllHikesFlow()
            else hikeRepository.searchHikes(query)
        }
        .map { list -> list.sortedByDescending { it.dateMs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun addHike(hike: HikeModel) {
        viewModelScope.launch {
            hikeRepository.insertHike(hike)
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