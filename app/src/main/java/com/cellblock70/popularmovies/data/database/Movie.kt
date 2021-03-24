package com.cellblock70.popularmovies.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "details")
data class Movie (

    @PrimaryKey
    @ColumnInfo(name = "movie_id")
    val id : Int,

    val title: String?,

    @ColumnInfo(name = "original_title")
    @Json(name ="original_title")
    val originalTitle : String?,

    @Json(name = "vote_average")
    val rating: Double?,

    @Json(name = "vote_count")
    val reviews: Int?,

    @Json(name = "overview")
    val synopsis: String?,

    @ColumnInfo(name = "release_date")
    @Json(name = "release_date")
    val releaseDate : String?,

    @ColumnInfo(name = "poster_path")
    @Json(name = "poster_path")
    val posterPath : String?,

    @ColumnInfo(name = "backdrop_path")
    @Json(name = "backdrop_path")
    val backdropPath : String?

)