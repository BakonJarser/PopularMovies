package com.cellblock70.popularmovies.data.database;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite",
        indices = {@Index("movie_id")})
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
