package com.example.mhikeandroidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.hike.HikeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HikeViewModel(private val repository: HikeRepository) : ViewModel() {

    val hikes = repository.getAllHikesFlow()
        .map { list -> list.sortedByDescending { it.dateMs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // state search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredHikes = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllHikesFlow()
            else repository.searchHikes(query)
        }
        .map { list -> list.sortedByDescending { it.dateMs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun addHike(hike: HikeModel) {
        viewModelScope.launch {
            repository.insertHike(hike)
        }
    }

    suspend fun getHikeById(id: Long): HikeModel? {
        return repository.getHikeById(id)
    }

    fun updateHike(hike: HikeModel) {
        viewModelScope.launch {
            repository.updateHike(hike)
        }
    }

    fun deleteHike(hike: HikeModel) {
        viewModelScope.launch {
            repository.deleteHike(hike)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class HikeViewModelFactory(
    private val repository: HikeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HikeViewModel::class.java)) {
            return HikeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}