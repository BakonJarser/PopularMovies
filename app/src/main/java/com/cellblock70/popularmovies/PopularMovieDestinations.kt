package com.cellblock70.popularmovies

import kotlinx.serialization.Serializable

@Serializable
data class MovieGrid(val movieListType: String)

@Serializable
data class MovieDetails(val movieId: Int)
