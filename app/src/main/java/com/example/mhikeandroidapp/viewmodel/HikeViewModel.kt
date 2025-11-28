package com.example.mhikeandroidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.hike.HikeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HikeViewModel(private val repository: HikeRepository) : ViewModel() {

    val hikes = repository.getAllHikesFlow()
        .map { it.sortedBy { hike -> hike.dateMs } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHike(hike: HikeModel) {
        viewModelScope.launch {
            repository.insertHike(hike)
        }
    }

    fun deleteHike(hike: HikeModel) {
        viewModelScope.launch {
            repository.deleteHike(hike)
        }
    }
}