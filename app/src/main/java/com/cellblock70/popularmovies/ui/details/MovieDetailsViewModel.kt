package com.cellblock70.popularmovies.ui.details

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cellblock70.popularmovies.MyApplication
import kotlinx.coroutines.launch

class MovieDetailsViewModel(val movieId: Int, val application: MyApplication) :
    AndroidViewModel(application) {


    val state = application.movieRepository.getCompleteMovie(movieId)

    fun onAction(action: MovieDetailsAction) {
        when (action) {
            is MovieDetailsAction.OnFavoriteClicked -> viewModelScope.launch {
                application.movieRepository.setIsFavorite(
                    movieId,
                    action.isFavorite
                )
            }
        }
    }
}