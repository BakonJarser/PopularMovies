package com.cellblock70.popularmovies.ui.details

import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.data.database.MovieReview
import com.cellblock70.popularmovies.data.database.MovieTrailer

data class MovieWithReviewsAndTrailers(
    val movie: Movie?,
    val trailers: List<MovieTrailer>?,
    val reviews: List<MovieReview>?,
    val isFavorite: Boolean
)
