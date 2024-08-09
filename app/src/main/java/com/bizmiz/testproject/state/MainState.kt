package com.bizmiz.testproject.state

import com.bizmiz.testproject.data.db.LocationModel

sealed class MainState {
    data object Idle : MainState()
    data class GetLocation(val location: LocationModel) : MainState()
}