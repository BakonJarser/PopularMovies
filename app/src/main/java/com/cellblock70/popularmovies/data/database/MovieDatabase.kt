package com.cellblock70.popularmovies.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MovieTrailer::class, Favorite::class, MovieReview::class, Movie::class],
    version = 2,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
}
