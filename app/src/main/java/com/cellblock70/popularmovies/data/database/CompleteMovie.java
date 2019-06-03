package com.cellblock70.popularmovies.data.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CompleteMovie {

    @Embedded
    private Movie movie;

    @Relation(parentColumn = "movie_id", entityColumn = "movie_id", entity = MovieReview.class)
    private List<MovieReview> reviewList;

    @Relation(parentColumn = "movie_id", entityColumn = "movie_id", entity = MovieTrailer.class)
    private List<MovieTrailer> trailerList;

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public List<MovieReview> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<MovieReview> reviewList) {
        this.reviewList = reviewList;
    }

    public List<MovieTrailer> getTrailerList() {
        return trailerList;
    }

    public void setTrailerList(List<MovieTrailer> trailerList) {
        this.trailerList = trailerList;
    }

}
