package com.cellblock70.popularmovies

import android.app.Application
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.MovieDatabase
import timber.log.Timber

class MyApplication : Application() {

    // TODO inject this with dagger-hilt
    val movieRepository by lazy { MovieRepository(MovieDatabase.getDatabase(this)) }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}