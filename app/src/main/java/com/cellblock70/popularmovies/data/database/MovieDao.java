package com.cellblock70.popularmovies.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface MovieDao {

    @Transaction
    @Query("SELECT * FROM details WHERE movie_id IN (SELECT * FROM favorite)")
    List<CompleteMovie> getFavorites();

    @Delete
    void delete(Favorite favorite);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Favorite favorite);

    // TODO delete unused movies
    @Query("DELETE FROM details WHERE movie_id NOT IN (SELECT movie_id FROM favorite)")
    void deleteAllExceptFavorites();

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
}
