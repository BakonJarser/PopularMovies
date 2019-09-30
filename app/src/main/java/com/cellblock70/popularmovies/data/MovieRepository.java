package com.cellblock70.popularmovies.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cellblock70.popularmovies.AppExecutors;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
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

    public LiveData<List<Movie>> getMovies(String movieListType) {
        MutableLiveData<List<Movie>> movies = new MutableLiveData<>();

        movieDataSource.fetchMovies(movieListType, movies);

        movies.observeForever(movies1 -> {
            setIsFavoriteInList(movies1);
            insertMovies(movies1);
//            for (Movie movie : movies1) {
//                MutableLiveData<List<MovieTrailer>> trailers = new MutableLiveData<>();
//                // TODO possibly fetch the trailers and reviews right away
//                movieDataSource.fetchTrailers();
//            }
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

    private void insertMovies(List<Movie> movies) {
        new InsertMoviesTask(movieDao).execute(movies.toArray(new Movie[0]));
    }

    public boolean isFavorite(int movieId) {
        Movie movie = movieDao.getMovie(movieId);
        return movie != null && movie.getFavorite();
    }

    public void setIsFavoriteInList(List<Movie> movies) {
        new SetFavoriteStatusOnMoviesTask(movieDao).execute(movies.toArray(new Movie[0]));
    }

    public void setIsFavoriteInDb(Movie movieId) {
        // TODO add ability to set movie as favorite
    }

    public LiveData<CompleteMovie> getCompleteMovie(Integer movieId) {
        MutableLiveData<CompleteMovie> completeMovieData = new MutableLiveData<>();
        CompleteMovie completeMovie = null;
        try {
            completeMovie = new GetCompleteMovie(movieDao).execute(movieId).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        completeMovieData.observeForever(movie -> {

            if(movie != null && (movie.getTrailerList().isEmpty() || movie.getReviewList().isEmpty())) {
                // TODO make sure this doesn't loop
                movieDataSource.fetchTrailersAndReviews(movieId, completeMovieData);
            }
            if (movie != null && movie.getReviewList() != null && !movie.getReviewList().isEmpty()) {
                new InsertReviewsTask(movieDao).execute(movie.getReviewList().toArray(new MovieReview[0]));
            }
            if (movie != null && movie.getTrailerList() != null && !movie.getTrailerList().isEmpty()) {
                new InsertTrailersTask(movieDao).execute(movie.getTrailerList().toArray(new MovieTrailer[0]));
            }
        });
        completeMovieData.postValue(completeMovie);

        return completeMovieData;
    }

    private static class InsertMoviesTask extends AsyncTask<Movie, Void, Void> {
        private MovieDao movieDao;

        InsertMoviesTask(MovieDao movieDao) {
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

    private static class SetFavoriteStatusOnMoviesTask extends AsyncTask<Movie, Void, Void> {

        private MovieDao movieDao;

        public SetFavoriteStatusOnMoviesTask(MovieDao movieDao) {
            this.movieDao = movieDao;
        }

        @Override
        protected Void doInBackground(Movie... movies) {

            for (Movie movie : movies) {
                Movie dbMovie = movieDao.getMovie(movie.getId());

                if (dbMovie != null && dbMovie.getFavorite()) {
                    movie.setFavorite(true);
                }
            }
            return null;
        }
    }

    private static class GetCompleteMovie extends AsyncTask<Integer, Void, CompleteMovie> {

        private MovieDao movieDao;

        public GetCompleteMovie(MovieDao movieDao) {
            this.movieDao = movieDao;
        }

        @Override
        protected CompleteMovie doInBackground(Integer... movieIds) {
           return movieDao.getCompleteMovie(movieIds[0]);

        }
    }
}
