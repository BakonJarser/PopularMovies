package com.cellblock70.popularmovies.ui.movielist

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cellblock70.popularmovies.MyApplication
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.database.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class MovieViewModel(val application: MyApplication, var movieListType: String) : AndroidViewModel(application) {

    val movies : StateFlow<List<Movie>> = application.movieRepository.movies.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val language = application.applicationContext.getString(R.string.language)

    init {
        getMovies(movieListType)
    }

    private fun getMovies(movieListType: String?) {
        Timber.e( "getMovies $movieListType")
        viewModelScope.launch(Dispatchers.IO) {
            application.movieRepository.getMovies(movieListType ?: "popular", 1, language)
        }
    }
}