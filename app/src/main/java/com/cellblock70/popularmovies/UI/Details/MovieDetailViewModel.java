package com.cellblock70.popularmovies.UI.Details;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.cellblock70.popularmovies.data.MovieRepository;
import com.cellblock70.popularmovies.data.database.CompleteMovie;

public class MovieDetailViewModel extends ViewModel {

    private LiveData<CompleteMovie> movie;
    private final MovieRepository movieRepository;
    private final int movieId;

    public MovieDetailViewModel(Application application, int movieId) {
        this.movieRepository = MovieRepository.provideRepository(application);
        this.movieId = movieId;
    }

    public LiveData<CompleteMovie> getMovieLiveData() {
        if (movie == null) {
            movie = movieRepository.getCompleteMovie(movieId);
        }
        return movie;
    }

    public void setIsFavorite(boolean isFavorite) {
        if (isFavorite) {
            movieRepository.setIsFavoriteInDb(movieId);
        } else {
            movieRepository.setNotFavorite(movieId);
        }
    }
}
