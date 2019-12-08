package com.cellblock70.popularmovies.data.database;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite")
public class Favorite {

    @PrimaryKey
    @ColumnInfo(name = "movie_id")
    private int movieId;

    public Favorite(int movieId) {
        this.movieId = movieId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }


}
