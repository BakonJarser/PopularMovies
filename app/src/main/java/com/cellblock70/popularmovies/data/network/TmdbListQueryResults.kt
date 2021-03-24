package com.cellblock70.popularmovies.data.network

import com.cellblock70.popularmovies.data.database.Movie
import com.squareup.moshi.Json

data class TmdbListQueryResults(
    val page : String,
    val results : List<Movie>,
    @Json(name = "total_pages")
    val totalPages : Int,
    @Json(name = "total_results")
    val totalResults : Int
)
