package com.cellblock70.popularmovies.domain

import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.ui.details.MovieWithReviewsAndTrailers
import kotlinx.coroutines.flow.StateFlow

interface MovieRepository {

    suspend fun getMovies(movieListType: String, page: Int, language: String)

    fun getCompleteMovie(movieId: Int): StateFlow<MovieWithReviewsAndTrailers>

    suspend fun setIsFavorite(movieId: Int, isFavorite: Boolean)

    fun getMovies(): StateFlow<List<Movie>>
}
