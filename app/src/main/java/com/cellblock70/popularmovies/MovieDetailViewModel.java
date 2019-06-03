package com.cellblock70.popularmovies;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cellblock70.popularmovies.data.MovieRepository;
import com.cellblock70.popularmovies.data.database.CompleteMovie;

public class MovieDetailViewModel extends ViewModel {

    private final LiveData<CompleteMovie> movie;
    private final MovieRepository movieRepository;
    private final int movieId;

    public MovieDetailViewModel(MovieRepository repository, int movieId) {
        this.movieRepository = repository;
        this.movieId = movieId;
        this.movie = movieRepository.getCompleteMovie(movieId);
    }

    public LiveData<CompleteMovie> getMovie() {
        if (movie == null) {
            movieRepository.getCompleteMovie(movieId);
        }
        return movie;
    }
}
