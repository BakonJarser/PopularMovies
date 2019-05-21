package com.cellblock70.popularmovies.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MovieRepository {

    private static final String LOG_TAG = "MovieRepository";
    private MovieDao movieDao;

    public MovieRepository(Context context) {
        MovieDatabase movieDb = MovieDatabase.getDatabase(context);
        movieDao = movieDb.movieDao();
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

    public List<Movie> getFavoritesAlreadyInBackground() {
        return movieDao.getFavorites();
    }

    public boolean isFavoriteAlreadyInBackground(int movieId) {
        Movie movie = movieDao.getMovie(movieId);
        return movie != null && movie.getFavorite();
    }

    public CompleteMovie getCompleteMovie(Integer movieId) {
        CompleteMovie movie = null;
        try {
            movie = new GetCompleteMovieTask(movieDao).execute(movieId).get();
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, "Failed to execute getCompleteMovie");
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "getCompleteMovie interrupted");
        }
        return movie;
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

    private static class GetCompleteMovieTask extends AsyncTask<Integer, Void, CompleteMovie> {

        private MovieDao movieDao;

        GetCompleteMovieTask(MovieDao movieDao) {
            this.movieDao = movieDao;
        }
        @Override
        protected CompleteMovie doInBackground(Integer... movieId) {
            return movieDao.getMovieWithTrailersAndReviews(movieId[0]);
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
