package com.cellblock70.popularmovies;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cellblock70.popularmovies.data.database.Movie;
import com.cellblock70.popularmovies.data.database.MovieDatabase;

import java.util.List;

public class MovieViewModel extends AndroidViewModel {

    private MovieDatabase movieDatabase;
    private LiveData<List<Movie>> allMovies;
    private LiveData<List<Movie>> favoriteMovies;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        movieDatabase = MovieDatabase.getDatabase(application);
    }

    public LiveData<List<Movie>> getMovies() {
        if (allMovies == null) {
            allMovies = movieDatabase.movieDao().getMovies();
        }
        return allMovies;
    }

    public LiveData<List<Movie>> getFavorites() {
        if (favoriteMovies == null) {
            favoriteMovies = movieDatabase.movieDao().getFavorites();
        }
        return favoriteMovies;
    }
}
