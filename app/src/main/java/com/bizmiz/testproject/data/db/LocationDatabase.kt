package com.bizmiz.testproject.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocationModel::class],
    version = 1,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

}