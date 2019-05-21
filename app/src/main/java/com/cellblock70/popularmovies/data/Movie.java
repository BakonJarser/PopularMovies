package com.cellblock70.popularmovies.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "details")
public class Movie {

    @PrimaryKey
    @ColumnInfo(name = "movie_id")
    private int id;

    @ColumnInfo(name = "favorite")
    private boolean isFavorite;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "original_title")
    @SerializedName("original_title")
    private String originalTitle;

    @ColumnInfo(name = "rating")
    @SerializedName("vote_average")
    private double rating;

    @ColumnInfo(name = "reviews")
    @SerializedName("vote_count")
    private int reviews;

    @ColumnInfo(name = "synopsis")
    @SerializedName("overview")
    private String synopsis;

    @ColumnInfo(name = "release_date")
    @SerializedName("release_date")
    private String releaseDate;

    @ColumnInfo(name = "poster_path")
    @SerializedName("poster_path")
    private String posterPath;

    @ColumnInfo(name = "backdrop_path")
    @SerializedName("backdrop_path")
    private String backdropPath;

    public int getId() {
        return id;
    }

    public boolean getFavorite() {
        return isFavorite;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public double getRating() {
        return rating;
    }

    public int getReviews() {
        return reviews;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }


}
