package com.cellblock70.popularmovies

import android.app.Application
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.MovieDatabase
import timber.log.Timber

class MyApplication : Application() {

    val movieListTypeMapKeyIsTitles = HashMap<String, String>()
    private val movieListTypeMapKeyIsValues = HashMap<String, String>()
    val movieRepository by lazy { MovieRepository(MovieDatabase.getDatabase(this)) }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        val prefTitles = resources.getStringArray(R.array.pref_movie_list_type_titles)
        val prefValues = resources.getStringArray(R.array.pref_movie_list_type_values)

        for (i in prefTitles.indices) {
            movieListTypeMapKeyIsTitles[prefTitles[i]] = prefValues[i]
            movieListTypeMapKeyIsValues[prefValues[i]] = prefTitles[i]
        }
    }
}