package com.cellblock70.popularmovies.data.di

import android.app.Application
import androidx.room.Room
import com.cellblock70.popularmovies.data.database.MovieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): MovieDatabase {
        return Room.databaseBuilder(
            application, MovieDatabase::class.java, "movie"
        ).build()
    }
}