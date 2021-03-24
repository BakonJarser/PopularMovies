package com.cellblock70.popularmovies.data.network

import com.cellblock70.popularmovies.data.database.MovieReview

data class TmdbMovieReviewQueryResults (
    val id: String,
    val results: List<MovieReview>,
    val page: Int
        )