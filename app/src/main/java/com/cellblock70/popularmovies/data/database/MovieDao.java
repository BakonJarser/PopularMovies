package com.cellblock70.popularmovies.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM details")
    LiveData<List<Movie>> getMovies();

    @Query("SELECT * FROM details where movie_id = (:movieId)")
    LiveData<Movie> getMovie(Integer movieId);

    @Query("SELECT * from details where movie_id = (:movieId)")
    Movie getMovie(int movieId);

    @Query("SELECT * FROM details where favorite = 1")
    LiveData<List<Movie>> getFavorites();

    // TODO delete unused movies
//    @Query("DELETE FROM details")
//    void deleteAll();

    @Transaction
    @Query("SELECT * FROM details where movie_id = (:movieId)")
    LiveData<CompleteMovie> getMovieWithTrailersAndReviews(Integer movieId);

    @Transaction
    @Query("SELECT * FROM details where movie_id = (:movieId)")
    CompleteMovie getCompleteMovie(Integer movieId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Movie...movieDetails);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MovieTrailer...trailers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MovieReview...reviews);

    @Query("update details set favorite = :favorite where movie_id = :movieId")
    void updateFavorite(boolean favorite, int movieId);

}
