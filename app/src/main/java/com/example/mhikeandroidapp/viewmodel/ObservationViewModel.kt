package com.example.mhikeandroidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mhikeandroidapp.data.observation.ObservationModel
import com.example.mhikeandroidapp.data.observation.ObservationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ObservationViewModel(private val repository: ObservationRepository) : ViewModel() {

    // Lấy danh sách ghi chú cho một chuyến đi cụ thể
    fun getObservationsForHike(hikeId: Long): Flow<List<ObservationModel>> {
        return repository.getObservationsForHikeFlow(hikeId)
            .map { it.sortedBy { obs -> obs.timeMs } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addObservation(observation: ObservationModel) {
        viewModelScope.launch {
            repository.insertObservation(observation)
        }
    }

    fun updateObservation(observation: ObservationModel) {
        viewModelScope.launch {
            repository.updateObservation(observation)
        }
    }

    fun deleteObservation(observation: ObservationModel) {
        viewModelScope.launch {
            repository.deleteObservation(observation)
        }
    }

    fun deleteAllForHike(hikeId: Long) {
        viewModelScope.launch {
            repository.deleteAllForHike(hikeId)
        }
    }

    class Factory(private val repository: ObservationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ObservationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ObservationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
