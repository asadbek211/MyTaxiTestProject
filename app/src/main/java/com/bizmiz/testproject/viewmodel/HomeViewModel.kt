package com.bizmiz.testproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizmiz.testproject.intent.MainIntent
import com.bizmiz.testproject.model.repository.LocationRepository
import com.bizmiz.testproject.state.MainState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val locationRepository: LocationRepository) : ViewModel() {
    val locationIntent = Channel<MainIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<MainState>(MainState.Idle)
    val state = _state.asStateFlow()

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            locationIntent.consumeAsFlow().collect {
                when (it) {
                    is MainIntent.FetchLocation -> getCurrentLocation()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            locationRepository.getCurrentLocation().collect {
                it?.let {
                    _state.emit(MainState.GetLocation(it))
                }
            }
        }
    }
}