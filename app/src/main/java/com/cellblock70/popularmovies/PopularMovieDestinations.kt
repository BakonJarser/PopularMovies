package com.cellblock70.popularmovies

import kotlinx.serialization.Serializable

@Serializable
object MovieGrid

@Serializable
data class MovieDetails(val movieId: Int)
