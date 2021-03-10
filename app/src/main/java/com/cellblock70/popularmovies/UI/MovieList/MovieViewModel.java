package com.cellblock70.popularmovies.UI.MovieList;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cellblock70.popularmovies.data.MovieRepository;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
import com.cellblock70.popularmovies.data.database.Movie;

import java.util.List;

public class MovieViewModel extends AndroidViewModel {

    //private MovieDatabase movieDatabase;
    private LiveData<List<Movie>> allMovies;
    private MutableLiveData<List<CompleteMovie>> favoriteMovies;
    private MovieRepository movieRepository;
    LiveData<List<CompleteMovie>> favorites = favoriteMovies;

    public MovieViewModel(@NonNull Application application) {
        super(application);
        movieRepository = MovieRepository.provideRepository(application);
        favoriteMovies = getFavorites();
    }

    public LiveData<List<Movie>> getMovies(String movieListType) {
        allMovies = movieRepository.getMovies(movieListType);
        return allMovies;
    }

    public MutableLiveData<List<CompleteMovie>> getFavorites() {
        if (favoriteMovies == null) {
            favoriteMovies = movieRepository.getFavorites();
        }
        return favoriteMovies;
    }
}
