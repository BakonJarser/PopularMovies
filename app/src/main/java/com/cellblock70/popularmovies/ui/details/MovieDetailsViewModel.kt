package com.cellblock70.popularmovies.ui.details

import android.app.Application
import androidx.lifecycle.*
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.*
import kotlinx.coroutines.launch

class MovieDetailsViewModel(private val movieId : Int, application: Application)
    : AndroidViewModel(application) {

    private val database = getDatabase(getApplication())
    private val repository = MovieRepository(database)
    val movie : LiveData<Movie> = repository.getMovie(movieId)
    val reviews : LiveData<List<MovieReview>> = repository.getReviews(movieId)
    val trailers : LiveData<List<MovieTrailer>> = repository.getTrailers(movieId)
    var favoriteLiveData : LiveData<List<Favorite>> = repository.getIsFavorite(movieId)

    fun toggleFavorite() {
        val isFavorite = favoriteLiveData.value.isNullOrEmpty()
        viewModelScope.launch {
            repository.setIsFavorite(movieId, isFavorite)
        }
    }

}