package com.cellblock70.popularmovies.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "reviews",
        foreignKeys = {@ForeignKey(entity = Movie.class, parentColumns = "movie_id",
                childColumns = "movie_id", onDelete = ForeignKey.CASCADE)},
        indices = {@Index("movie_id")})
public class MovieReview {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private transient int id;

    @ColumnInfo(name = "review_tx")
    @SerializedName("content")
    private String reviewText;

    @ColumnInfo(name = "author")
    private String author;

    @ColumnInfo(name = "movie_id")
    private int movieId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }
}
