package com.cellblock70.popularmovies.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cellblock70.popularmovies.MyApplication

@Database(entities = [MovieTrailer::class, Favorite::class, MovieReview::class, Movie::class], version = 2, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract val movieDao : MovieDao
}

private lateinit var INSTANCE : MovieDatabase

fun getDatabase(applicationContext: MyApplication) : MovieDatabase {

    synchronized(MovieDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                applicationContext, MovieDatabase::class.java, "movie").build()
        }
    }

    return INSTANCE
}
