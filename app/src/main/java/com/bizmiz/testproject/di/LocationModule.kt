package com.bizmiz.testproject.di

import android.content.Context
import androidx.room.Room
import com.bizmiz.testproject.data.db.LocationDatabase
import com.bizmiz.testproject.model.repository.LocationRepository
import com.bizmiz.testproject.util.Constants.LOCAL_DB_NAME
import com.bizmiz.testproject.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val locationModule = module {
    single { locationDatabase(get()) }
    single { get<LocationDatabase>().locationDao() }
    single { LocationRepository(get()) }
    viewModel { HomeViewModel(get()) }
}

fun locationDatabase(context: Context): LocationDatabase {
    return Room.databaseBuilder(context, LocationDatabase::class.java, LOCAL_DB_NAME)
        .fallbackToDestructiveMigration().build()
}