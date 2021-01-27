package com.cellblock70.popularmovies.data.database;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM favorite")
    LiveData<List<Favorite>> getFavorites();
    @Delete
    void delete(Favorite favorite);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Favorite favorite);

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
}
