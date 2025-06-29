package com.cellblock70.popularmovies.data.di

import com.cellblock70.popularmovies.data.MovieRepositoryImpl
import com.cellblock70.popularmovies.domain.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindRepository(impl: MovieRepositoryImpl): MovieRepository
}