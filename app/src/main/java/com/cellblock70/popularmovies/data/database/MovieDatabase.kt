package com.cellblock70.popularmovies.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MovieTrailer::class, Favorite::class, MovieReview::class, Movie::class], version = 2, exportSchema = false)
//@Database(entities = [Movie::class, MovieReview::class, MovieTrailer::class, Favorite::class], version = 2, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract val movieDao : MovieDao
}

private lateinit var INSTANCE : MovieDatabase

fun getDatabase(context: Context) : MovieDatabase {

    synchronized(MovieDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext, MovieDatabase::class.java, "movie").build()
        }
    }

    return INSTANCE
}
