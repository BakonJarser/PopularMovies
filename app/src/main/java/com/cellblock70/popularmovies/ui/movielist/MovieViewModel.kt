package com.cellblock70.popularmovies.ui.movielist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cellblock70.popularmovies.MyApplication
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.data.database.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class MovieViewModel(application: Application, var movieListType: String) : AndroidViewModel(application) {

    private val database = getDatabase(application as MyApplication)
    private val movieRepository = MovieRepository(database)
    val movies : StateFlow<List<Movie>> = movieRepository.movies.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val language = application.applicationContext.getString(R.string.language)

    init {
        getMovies(movieListType)
    }

    private fun getMovies(movieListType: String?) {
        Timber.e( "getMovies $movieListType")
        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.getMovies(movieListType ?: "popular", 1, language)
        }
    }
}