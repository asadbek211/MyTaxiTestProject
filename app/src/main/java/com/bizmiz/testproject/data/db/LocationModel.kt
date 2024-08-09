package com.bizmiz.testproject.data.db

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bizmiz.testproject.util.Constants.LOCATION_TABLE

@Keep
@Entity(tableName = LOCATION_TABLE)
data class LocationModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val latitude: Double,
    val longitude: Double,
    val time: Long
)