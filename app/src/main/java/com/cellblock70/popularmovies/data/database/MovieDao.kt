package com.cellblock70.popularmovies.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Transaction
    @Query("SELECT * FROM details WHERE movie_id IN (SELECT * FROM favorite)")
    fun getFavoriteMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM favorite WHERE movie_id = (:movieId)")
    fun getFavorite(movieId: Int) : Flow<List<Favorite>>

    @Query("SELECT * FROM favorite")
    suspend fun getFavoriteList() : List<Favorite>

    @Query("SELECT EXISTS(SELECT * FROM favorite WHERE movie_id = :movieId)")
    fun isFavorite(movieId: Int): Boolean

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Transaction
    @Query("DELETE FROM details")
    suspend fun deleteAllMovies()

    @Transaction
    @Query("SELECT * FROM details WHERE movie_id = (:movieId)")
    fun getMovie(movieId: Int): Movie

    @Transaction
    @Query("SELECT * FROM details")
    fun getMovieList(): Flow<List<Movie>>

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
    fun getReviews(movieId: Int): List<MovieReview>

    @Transaction
    @Query("SELECT * FROM trailers WHERE movie_id = (:movieId)")
    fun getTrailers(movieId: Int): List<MovieTrailer>
}