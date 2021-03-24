package com.cellblock70.popularmovies.data.database

import androidx.room.*
import com.squareup.moshi.Json

@Entity(tableName = "reviews")
data class MovieReview (

    @PrimaryKey
    val id : String,

    @ColumnInfo(name = "review_tx")
    @Json(name = "content")
    val reviewText : String?,

    val author: String?,

    @ColumnInfo(name = "movie_id")
    var movieId : Int?

    )