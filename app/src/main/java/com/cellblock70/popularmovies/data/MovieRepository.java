package com.cellblock70.popularmovies.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cellblock70.popularmovies.AppExecutors;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
import com.cellblock70.popularmovies.data.database.Favorite;
import com.cellblock70.popularmovies.data.database.Movie;
import com.cellblock70.popularmovies.data.database.MovieDao;
import com.cellblock70.popularmovies.data.database.MovieDatabase;
import com.cellblock70.popularmovies.data.database.MovieReview;
import com.cellblock70.popularmovies.data.database.MovieTrailer;
import com.cellblock70.popularmovies.data.network.MovieNetworkDataSource;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MovieRepository {
    private static final String LOG_TAG = "MovieRepository";
    private final MovieDao movieDao;
    private final AppExecutors executors;
    private static MovieRepository instance;
    private final MovieNetworkDataSource movieDataSource;
    private MutableLiveData<List<CompleteMovie>> favoriteLD;

    private MovieRepository(MovieDao movieDao, MovieNetworkDataSource dataSource,
                            AppExecutors executors) {
        this.movieDao = movieDao;
        this.movieDataSource = dataSource;
        this.executors = executors;

        favoriteLD = new MutableLiveData<>();
        initFavorites();
    }

    private void initFavorites() {
        synchronized (this) {
            if (favoriteLD.getValue() == null) {
                executors.diskIO().execute(() -> {
                    favoriteLD.postValue(movieDao.getFavorites());
                });
            }
        }
    }

    public MutableLiveData<List<CompleteMovie>> getFavorites() {
        return favoriteLD;
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

    public LiveData<List<Movie>> getMovies(String movieListType) {
        MutableLiveData<List<Movie>> movies = new MutableLiveData<>();

        movieDataSource.fetchMovies(movieListType, movies);

        movies.observeForever(movies1 -> {
            executors.diskIO().execute(() -> movieDao.insertAll(movies1.toArray(new Movie[0])));
            for (Movie movie : movies1) {
                MutableLiveData<List<MovieTrailer>> trailer = new MutableLiveData<>();
                MutableLiveData<List<MovieReview>> review = new MutableLiveData<>();
                movieDataSource.fetchTrailersAndReviews(movie.getId(), trailer, review);
                trailer.observeForever(this::insertTrailers);
                review.observeForever(this::insertReviews);
           }
        });

        return movies;
    }

    public static MovieRepository provideRepository(Context context) {
        MovieDatabase database = MovieDatabase.getDatabase(context.getApplicationContext());
        AppExecutors executors = AppExecutors.getInstance();
        MovieNetworkDataSource networkDataSource =
                MovieNetworkDataSource.getInstance(context.getApplicationContext(), executors);
        return getInstance(database.movieDao(), networkDataSource, executors);
    }

    private void insertTrailers(List<MovieTrailer> trailers) {
        new InsertTrailersTask(movieDao).execute(trailers.toArray(new MovieTrailer[0]));
    }

    private void insertReviews(List<MovieReview> reviews) {
        new InsertReviewsTask(movieDao).execute(reviews.toArray(new MovieReview[0]));
    }


    public void setIsFavoriteInDb(int movieId) {
        Log.d(LOG_TAG, "Inserting movie into favorites: " + movieId);
        executors.diskIO().execute(() ->  movieDao.insert(new Favorite(movieId)));
    }

    public void setNotFavorite(int movieId) {
        executors.diskIO().execute(() -> movieDao.delete(new Favorite(movieId)));
    }

    public void deleteAllExceptFavorites() {
        executors.diskIO().execute(movieDao::deleteAllExceptFavorites);
    }

    public LiveData<CompleteMovie> getCompleteMovie(Integer movieId) {
        MutableLiveData<CompleteMovie> completeMovieData = new MutableLiveData<>();
        CompleteMovie completeMovie = null;
        try {
            completeMovie = new GetCompleteMovie(movieDao).execute(movieId).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        completeMovieData.postValue(completeMovie);

        return completeMovieData;
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

    private static class GetCompleteMovie extends AsyncTask<Integer, Void, CompleteMovie> {

        private MovieDao movieDao;

        private GetCompleteMovie(MovieDao movieDao) {
            this.movieDao = movieDao;
        }

        @Override
        protected CompleteMovie doInBackground(Integer... movieIds) {
           return movieDao.getCompleteMovie(movieIds[0]);

        }
    }
}
