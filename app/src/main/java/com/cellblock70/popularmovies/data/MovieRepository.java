package com.cellblock70.popularmovies.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.cellblock70.popularmovies.AppExecutors;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
import com.cellblock70.popularmovies.data.database.Movie;
import com.cellblock70.popularmovies.data.database.MovieDao;
import com.cellblock70.popularmovies.data.database.MovieDatabase;
import com.cellblock70.popularmovies.data.database.MovieReview;
import com.cellblock70.popularmovies.data.database.MovieTrailer;
import com.cellblock70.popularmovies.data.network.MovieNetworkDataSource;

import java.util.List;

public class MovieRepository {
    private static final String LOG_TAG = "MovieRepository";
    private final MovieDao movieDao;
    private final AppExecutors executors;
    private boolean initialized = false;
    private static MovieRepository instance;
    private final MovieNetworkDataSource movieDataSource;

    private MovieRepository(MovieDao movieDao, MovieNetworkDataSource dataSource,
                            AppExecutors executors) {
        this.movieDao = movieDao;
        this.movieDataSource = dataSource;
        this.executors = executors;

        // TODO load the movies. Do I need to pass in the current movie list type?

    }

    private static MovieRepository getInstance(MovieDao movieDao, MovieNetworkDataSource dataSource,
                                        AppExecutors executors) {
        if (instance == null) {
            synchronized (MovieRepository.class) {
                if (instance == null) {
                    Log.d(LOG_TAG, "Creating new instance of movie repository");
                    instance = new MovieRepository(movieDao, dataSource, executors);
                }
            }
        }
        return instance;
    }

    public static MovieRepository provideRepository(Context context) {
        MovieDatabase database = MovieDatabase.getDatabase(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        MovieNetworkDataSource networkDataSource =
                MovieNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return getInstance(database.movieDao(), networkDataSource, executors);
    }

    public void insertMoviesAlreadyInBackground(List<Movie> movies) {
        movieDao.insertAll(movies.toArray(new Movie[0]));
    }

    public void insertTrailers(List<MovieTrailer> trailers) {
        new InsertTrailersTask(movieDao).execute(trailers.toArray(new MovieTrailer[0]));
    }

    public void insertReviews(List<MovieReview> reviews) {
        new InsertReviewsTask(movieDao).execute(reviews.toArray(new MovieReview[0]));
    }

    public void updateFavorite(boolean isFavorite, int movieId) {
        new UpdateFavoriteTask(movieDao, isFavorite, movieId).execute();
    }

    public LiveData<List<Movie>> getFavoritesAlreadyInBackground() {
        return movieDao.getFavorites();
    }

    public void insertMovie(Movie movie) {
        new InsertMovieTask(movieDao).execute(movie);
    }

    public boolean isFavoriteAlreadyInBackground(int movieId) {
        Movie movie = movieDao.getMovie(movieId);
        return movie != null && movie.getFavorite();
    }

    public LiveData<CompleteMovie> getCompleteMovie(Integer movieId) {
        return movieDao.getMovieWithTrailersAndReviews(movieId);
    }

    private static class InsertMovieTask extends AsyncTask<Movie, Void, Void> {
        private MovieDao movieDao;

        InsertMovieTask(MovieDao movieDao) {
            this.movieDao = movieDao;
        }

        @Override
        protected Void doInBackground(Movie... movies) {
            movieDao.insertAll(movies);
            return null;
        }
    }

    private static class InsertTrailersTask extends AsyncTask<MovieTrailer, Void, Void> {

        private MovieDao movieDao;

        InsertTrailersTask(MovieDao movieDao) {
            this.movieDao = movieDao;
        }
        @Override
        protected Void doInBackground(MovieTrailer... trailers) {
            movieDao.insertAll(trailers);
            return null;
        }
    }

    private static class InsertReviewsTask extends AsyncTask<MovieReview, Void, Void> {

        private MovieDao movieDao;

        InsertReviewsTask(MovieDao movieDao) {
            this.movieDao = movieDao;
        }
        @Override
        protected Void doInBackground(MovieReview... reviews) {
            movieDao.insertAll(reviews);
            return null;
        }
    }

    private static class UpdateFavoriteTask extends AsyncTask<Void, Void, Void> {

        private MovieDao movieDao;
        private boolean isFavorite;
        private int movieId;

        UpdateFavoriteTask(MovieDao movieDao, boolean isFavorite, int movieId) {

            this.movieDao = movieDao;
            this.isFavorite = isFavorite;
            this.movieId = movieId;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            movieDao.updateFavorite(isFavorite, movieId);
            return null;
        }
    }
}
