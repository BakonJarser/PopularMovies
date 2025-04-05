package com.cellblock70.popularmovies.data

import com.cellblock70.popularmovies.BuildConfig
import com.cellblock70.popularmovies.data.database.Favorite
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.data.database.MovieDatabase
import com.cellblock70.popularmovies.data.network.TMDBApi
import com.cellblock70.popularmovies.ui.details.MovieWithReviewsAndTrailers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.util.LinkedList

private const val TMDB_API_KEY = BuildConfig.TMDB_MAP_API_KEY

class MovieRepository(private val database: MovieDatabase) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var currentlyLoadedMovie = -1
    private val completeMovie : MutableStateFlow<MovieWithReviewsAndTrailers> = MutableStateFlow(MovieWithReviewsAndTrailers(movie = null, trailers = null, reviews = null, isFavorite = false))
    private val _movies : Flow<List<Movie>> = database.movieDao.getMovieList()
    val movies = _movies.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    private var currentMovieListType : String = "popular"


    suspend fun getMovies(movieListType: String, page: Int, language: String) {

        if (movies.value.isEmpty() || currentMovieListType != movieListType) {
            currentMovieListType = movieListType
            try{
                getMovies(page, language)

            } catch (e: HttpException) {
                Timber.e(e.message())
            }
        }
    }

    private suspend fun getMovies(page: Int, language: String) {
        database.movieDao.deleteAllMovies()
        Timber.e("Getting movies of type $currentMovieListType")
        if (currentMovieListType != "Favorites") {
            val queryResult =
                TMDBApi.tmdbService.getMovies(currentMovieListType, page, language, TMDB_API_KEY)
            database.movieDao.insertAll(*queryResult.results.toTypedArray())
            queryResult.results.forEach {
                getTrailers(it.id, language)
                getReviews(it.id, language)
            }
        } else {
            val downloadedMovies = LinkedList<Movie>()
            database.movieDao.getFavoriteList().forEach {
                Timber.e("Downloading favorite ${it.movieId}")
                val movie = TMDBApi.tmdbService.getMovie(it.movieId.toString(), language, TMDB_API_KEY)
                database.movieDao.insertAll(movie)
                // save to a list so that trailers and reviews can be downloaded later
                downloadedMovies.add(movie)
            }
            // now that all movies are downloaded get the trailers and reviews
            downloadedMovies.forEach {
                getTrailers(it.id, language)
                getReviews(it.id, language)
            }
        }
    }

    private suspend fun getReviews(movieId: Int, language: String) {
        try {
            val queryResult = TMDBApi.tmdbService.getReviews(movieId, language, TMDB_API_KEY)
            queryResult.results.forEach { movieReview -> movieReview.movieId = movieId }
            database.movieDao.insertAll(*queryResult.results.toTypedArray())

        } catch (e: HttpException) {
            Timber.e( e.message())
        }
    }

    private suspend fun getTrailers(movieId: Int, language: String) {
        try {
            val queryResult = TMDBApi.tmdbService.getTrailers(movieId, language, TMDB_API_KEY)
            queryResult.results.forEach { movieTrailer -> movieTrailer.movieId = movieId }
            database.movieDao.insertAll(*queryResult.results.toTypedArray())

        } catch (e: HttpException) {
            Timber.e( e.message())
        }
    }

    fun getCompleteMovie(movieId: Int): StateFlow<MovieWithReviewsAndTrailers> {
        if (movieId != currentlyLoadedMovie) {
            loadMovie(movieId)
        }
        return completeMovie
    }

    private fun loadMovie(movieId: Int) {
        scope.launch {
            currentlyLoadedMovie = movieId
            val movie = database.movieDao.getMovie(currentlyLoadedMovie)
            val reviews = database.movieDao.getReviews(currentlyLoadedMovie)
            val trailers = database.movieDao.getTrailers(currentlyLoadedMovie)
            val isFavorite = database.movieDao.isFavorite(currentlyLoadedMovie)
            completeMovie.emit(MovieWithReviewsAndTrailers(movie = movie, trailers = trailers, reviews = reviews, isFavorite = isFavorite))
        }
    }

    suspend fun setIsFavorite(movieId: Int, isFavorite: Boolean) {
        if (isFavorite) {
            Timber.e("Inserting favorite: $movieId")
            database.movieDao.insert(Favorite(movieId))
        } else {
            Timber.e("Deleting favorite: $movieId")
            database.movieDao.deleteFavorite(Favorite(movieId))
        }
        completeMovie.emit(completeMovie.value.copy(isFavorite = isFavorite))
    }
}