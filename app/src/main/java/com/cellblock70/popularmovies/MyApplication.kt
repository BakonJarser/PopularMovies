package com.cellblock70.popularmovies

import android.app.Application
import timber.log.Timber

class MyApplication : Application() {

    val movieListTypeMapKeyIsTitles = HashMap<String, String>()
    val movieListTypeMapKeyIsValues = HashMap<String, String>()

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