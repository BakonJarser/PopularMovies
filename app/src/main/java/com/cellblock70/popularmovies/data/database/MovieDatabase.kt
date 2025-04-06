package com.cellblock70.popularmovies.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MovieTrailer::class, Favorite::class, MovieReview::class, Movie::class], version = 2, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object MovieDatabaseCompanion {
        @Volatile
        private var Instance: MovieDatabase? = null


        fun getDatabase(context: Context): MovieDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, MovieDatabase::class.java, "movie"
                ).build().also { Instance = it }
            }
        }
    }
}
