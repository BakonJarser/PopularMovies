package com.cellblock70.popularmovies;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cellblock70.popularmovies.data.MovieRepository;

public class MovieDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final MovieRepository repository;
    private final int movieId;

    public MovieDetailViewModelFactory(MovieRepository repository, int movieId) {
        this.repository = repository;
        this.movieId = movieId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MovieDetailViewModel(repository, movieId);
    }
}
