package com.cellblock70.popularmovies.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey
    @ColumnInfo(name = "movie_id")
    val movieId : Int
)
