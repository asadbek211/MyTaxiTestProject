package com.bizmiz.testproject.data.db

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Keep
@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLocation(data: LocationModel)

    @Query("SELECT * FROM location ORDER BY id DESC LIMIT 1")
    fun getCurrentLocation(): Flow<LocationModel?>
}