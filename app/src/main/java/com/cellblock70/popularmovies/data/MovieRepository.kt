package com.cellblock70.popularmovies.data

import androidx.lifecycle.LiveData
import com.cellblock70.popularmovies.BuildConfig
import com.cellblock70.popularmovies.data.database.*
import com.cellblock70.popularmovies.data.network.TMDBApi
import retrofit2.HttpException
import timber.log.Timber
import java.util.LinkedList

private const val TMDB_API_KEY = BuildConfig.TMDB_MAP_API_KEY

class MovieRepository(private val database: MovieDatabase) {

    val movies : LiveData<List<Movie>> = database.movieDao.getMovieList()
    private var currentMovieListType : String = "popular"

    suspend fun getMovies(movieListType: String, page: Int, language: String) {

        if (movies.value.isNullOrEmpty() || currentMovieListType != movieListType) {
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

    fun getMovie(movieId: Int): LiveData<Movie> {
        return database.movieDao.getMovie(movieId)
    }

    fun getReviews(movieId: Int) : LiveData<List<MovieReview>> {
        return database.movieDao.getReviews(movieId)
    }

    fun getTrailers(movieId: Int) : LiveData<List<MovieTrailer>> {
        return database.movieDao.getTrailers(movieId)
    }

    fun getIsFavorite(movieId: Int): LiveData<List<Favorite>> {
        return database.movieDao.getFavorite(movieId)
    }

    suspend fun setIsFavorite(movieId: Int, isFavorite: Boolean) {
        if (isFavorite) {
            Timber.e("Inserting favorite: $movieId")
            database.movieDao.insert(Favorite(movieId))
        } else {
            Timber.e("Deleting favorite: $movieId")
            database.movieDao.deleteFavorite(Favorite(movieId))
        }
    }
}