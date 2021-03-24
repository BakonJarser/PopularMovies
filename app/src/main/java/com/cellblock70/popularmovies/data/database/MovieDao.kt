package com.cellblock70.popularmovies.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MovieDao {
    @Transaction
    @Query("SELECT * FROM details WHERE movie_id IN (SELECT * FROM favorite)")
    fun getFavoriteMovies(): LiveData<List<Movie>>

    @Query("SELECT * FROM favorite WHERE movie_id = (:movieId)")
    fun getFavorite(movieId: Int) : LiveData<List<Favorite>>

    @Query("SELECT * FROM favorite")
    suspend fun getFavoriteList() : List<Favorite>

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Transaction
    @Query("DELETE FROM details")
    suspend fun deleteAllMovies()

    @Transaction
    @Query("SELECT * FROM details WHERE movie_id = (:movieId)")
    fun getMovie(movieId: Int): LiveData<Movie>

    @Transaction
    @Query("SELECT * FROM details")
    fun getMovieList(): LiveData<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: Favorite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg movieDetails: Movie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg trailers: MovieTrailer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg reviews: MovieReview)

    @Transaction
    @Query("SELECT * FROM reviews WHERE movie_id = (:movieId)")
    fun getReviews(movieId: Int): LiveData<List<MovieReview>>

    @Transaction
    @Query("SELECT * FROM trailers WHERE movie_id = (:movieId)")
    fun getTrailers(movieId: Int): LiveData<List<MovieTrailer>>
}