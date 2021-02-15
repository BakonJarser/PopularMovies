package com.cellblock70.popularmovies.UI.MovieList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cellblock70.popularmovies.data.MovieRepository;
import com.cellblock70.popularmovies.data.database.Movie;

import java.util.List;

public class MovieViewModel extends AndroidViewModel {

    //private MovieDatabase movieDatabase;
    private LiveData<List<Movie>> allMovies;
    private LiveData<List<Movie>> favoriteMovies;
    private MovieRepository movieRepository;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        movieRepository = MovieRepository.provideRepository(application);
    }

    public LiveData<List<Movie>> getMovies(String movieListType) {
        //if (allMovies == null) {
            allMovies = movieRepository.getMovies(movieListType);
       // }
        return allMovies;
    }

    public void reloadMovies(String movieListType) {
        deleteMovies();
        getMovies(movieListType);
    }

    public void deleteMovies() {
        movieRepository.deleteAllExceptFavorites();
        allMovies.getValue().clear();

    }

    // TODO implement favorites menu
//    public LiveData<List<Movie>> getFavorites() {
//        if (favoriteMovies == null) {
//            favoriteMovies = movieRepository.getFavoritesAlreadyInBackground();
//        }
//        return favoriteMovies;
//    }
}
