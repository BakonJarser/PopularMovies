package com.cellblock70.popularmovies.data.database

import androidx.room.*
import com.squareup.moshi.Json

@Entity(tableName = "trailers")
data class MovieTrailer(

    @PrimaryKey
    val id : String,

    val name : String?,

    @Json(name = "key")
    val link : String?,

    val site : String?,

    val type : String?,

    @ColumnInfo(name = "movie_id")
    var movieId: Int?
)
