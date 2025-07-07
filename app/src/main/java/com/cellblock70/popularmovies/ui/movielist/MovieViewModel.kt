package com.cellblock70.popularmovies.ui.movielist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.domain.MovieRepository
import com.cellblock70.popularmovies.domain.usecase.GetSelectedTabUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieViewModel.MovieViewModelFactory::class)
class MovieViewModel @AssistedInject constructor(
    private val movieRepository: MovieRepository,
    private val getSelectedTabUseCase: GetSelectedTabUseCase,
    @Assisted("language") val language: String
) : ViewModel() {

    @AssistedFactory
    interface MovieViewModelFactory {
        fun create(
            @Assisted("language") language: String
        ): MovieViewModel
    }

    val movies: StateFlow<List<Movie>> = movieRepository.getMovies()

    init {
        getMovies()
    }

    private fun getMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            val movieListType = getSelectedTabUseCase()
            movieRepository.getMovies(movieListType , 1, language)
        }
    }
}