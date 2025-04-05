package com.cellblock70.popularmovies.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.getDatabase
import kotlinx.coroutines.launch

class MovieDetailsViewModel(val movieId: Int, application: Application) :
    AndroidViewModel(application) {

    private val database = getDatabase(getApplication())
    private val repository = MovieRepository(database)
    val state = repository.getCompleteMovie(movieId)

    fun onAction(action: MovieDetailsAction) {
        when (action) {
            is MovieDetailsAction.OnFavoriteClicked -> viewModelScope.launch { repository.setIsFavorite(movieId, action.isFavorite) }
        }
    }
}