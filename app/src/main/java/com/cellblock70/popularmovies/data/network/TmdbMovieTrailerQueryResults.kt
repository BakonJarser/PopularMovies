package com.cellblock70.popularmovies.data.network

import com.cellblock70.popularmovies.data.database.MovieTrailer

data class TmdbMovieTrailerQueryResults(

    val id: Int,
    val results: List<MovieTrailer>
)
