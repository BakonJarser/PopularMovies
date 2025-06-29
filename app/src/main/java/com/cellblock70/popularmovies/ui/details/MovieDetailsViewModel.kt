package com.cellblock70.popularmovies.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cellblock70.popularmovies.domain.MovieRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieDetailsViewModel.Factory::class)
class MovieDetailsViewModel @AssistedInject constructor(
    @Assisted val movieId: Int,
    private val movieRepository: MovieRepository,
    ) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailsViewModel
    }

    val state = movieRepository.getCompleteMovie(movieId)

    fun onAction(action: MovieDetailsAction) {
        when (action) {
            is MovieDetailsAction.OnFavoriteClicked -> viewModelScope.launch {
                movieRepository.setIsFavorite(
                    movieId,
                    action.isFavorite
                )
            }
        }
    }
}