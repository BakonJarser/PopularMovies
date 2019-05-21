package com.cellblock70.popularmovies.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM details where movie_id = (:movieId)")
    Movie getMovie(Integer movieId);

    @Query("SELECT * FROM details where favorite = 1")
    List<Movie> getFavorites();

    @Transaction
    @Query("SELECT * FROM details where movie_id = (:movieId)")
    CompleteMovie getMovieWithTrailersAndReviews(Integer movieId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Movie...movieDetails);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MovieTrailer...trailers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MovieReview...reviews);

    @Query("update details set favorite = :favorite where movie_id = :movieId")
    void updateFavorite(boolean favorite, int movieId);

}
