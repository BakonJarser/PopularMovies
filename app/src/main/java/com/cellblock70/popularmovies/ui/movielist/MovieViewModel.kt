package com.cellblock70.popularmovies.ui.movielist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.domain.MovieRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = MovieViewModel.MovieViewModelFactory::class)
class MovieViewModel @AssistedInject constructor(
    private val movieRepository: MovieRepository,
    @Assisted("movieListType") val movieListType: String,
    @Assisted("language") val language: String
) : ViewModel() {

    @AssistedFactory
    interface MovieViewModelFactory {
        fun create(
            @Assisted("movieListType") movieListType: String,
            @Assisted("language") language: String
        ): MovieViewModel
    }

    val movies: StateFlow<List<Movie>> = movieRepository.getMovies()

    init {
        getMovies(movieListType)
    }

    private fun getMovies(movieListType: String?) {
        Timber.e("getMovies $movieListType")
        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.getMovies(movieListType ?: "popular", 1, language)
        }
    }
}