package com.cellblock70.popularmovies.ui.movielist

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager

import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.data.database.getDatabase
import kotlinx.coroutines.launch
import timber.log.Timber

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application.applicationContext)
    private val movieRepository = MovieRepository(database)
    val movies : LiveData<List<Movie>> = movieRepository.movies
    private val language = application.applicationContext.getString(R.string.language)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val movieListTypePrefKey = application.getString(R.string.movie_list_type)
    private var movieListType: String = prefs.getString(movieListTypePrefKey, "popular") ?: "popular"

    init {
        getMovies()
    }

    private fun getMovies() {
        Timber.e( "getMovies $movieListType")
        viewModelScope.launch {

            movieRepository.getMovies(movieListType, 1, language)
            // TODO what to do with an empty list?
            Timber.e( "getMovies FINISHED ${movies.value?.size}")
        }
    }
}