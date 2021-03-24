package com.cellblock70.popularmovies

import android.app.Application
import timber.log.Timber

@Suppress("unused")
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}