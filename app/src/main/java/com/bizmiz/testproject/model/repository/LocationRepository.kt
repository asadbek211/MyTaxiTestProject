package com.bizmiz.testproject.model.repository

import com.bizmiz.testproject.data.db.LocationDao
import com.bizmiz.testproject.data.db.LocationModel

class LocationRepository(private val locationDao: LocationDao) {
    suspend fun addLocation(data: LocationModel) { locationDao.addLocation(data) }
    fun getCurrentLocation() = locationDao.getCurrentLocation()
}