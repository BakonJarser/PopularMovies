package com.cellblock70.popularmovies.data.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.cellblock70.popularmovies.AppExecutors;
import com.cellblock70.popularmovies.BuildConfig;
import com.cellblock70.popularmovies.R;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
import com.cellblock70.popularmovies.data.database.Movie;

import com.cellblock70.popularmovies.data.database.MovieReview;
import com.cellblock70.popularmovies.data.database.MovieTrailer;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieNetworkDataSource {

    private static final String LOG_TAG = "MovieNetworkDataSource";

    private static MovieNetworkDataSource instance;
    private final Context context;
    private AppExecutors appExecutors;
    private static final String TMDB_API_KEY = BuildConfig.TMDB_MAP_API_KEY;

    private MovieNetworkDataSource(Context context, AppExecutors executors) {
        this.context = context.getApplicationContext();
        this.appExecutors = executors;
    }

    public static MovieNetworkDataSource getInstance(Context context, AppExecutors executors) {
        if (instance == null) {
            synchronized (MovieNetworkDataSource.class) {
                if (instance == null) {
                    instance = new MovieNetworkDataSource(context, executors);
                }
            }
        }
        return instance;
    }

    public void fetchTrailersAndReviews(Integer movieId, MutableLiveData<CompleteMovie> movie) {
        StringBuilder buffer = new StringBuilder();
        appExecutors.networkIO().execute(() -> {
            InputStream input = null;
            try {
                Uri uri = Uri.parse(context.getString(R.string.base_url))
                        .buildUpon()
                        .appendEncodedPath(movieId.toString())
                        .appendQueryParameter(context.getString(R.string.language_param), context.getString(R.string.language))
                        .appendQueryParameter(context.getString(R.string.api_key), BuildConfig.TMDB_MAP_API_KEY)
                        .appendQueryParameter("append_to_response", context.getString(R.string.trailer_list_path)
                                + "," + context.getString(R.string.review_list_path))
                        .build();
                URL url = new URL(uri.toString());
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                input = httpURLConnection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error message: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Failed to close stream");
                    }
                }
            }

            String s = (buffer.length() != 0) ? buffer.toString() : "";
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject trailers = jsonObject.getJSONObject(context.getString(R.string
                        .trailer_list_path));
                JSONArray trailerResults = trailers.getJSONArray("results");
                List<MovieTrailer> movieTrailers = new ArrayList<>(trailerResults.length());
                for (int i = 0; i < trailerResults.length(); i++) {

                    Gson gson = new Gson();
                    MovieTrailer movieTrailer = gson.fromJson(trailerResults.get(i).toString(), MovieTrailer.class);
                    movieTrailer.setMovieId(movieId);
                    movieTrailers.add(movieTrailer);
                    // Make sure this is a youtube video.
                    if (!movieTrailer.getSite().equalsIgnoreCase("youtube")) {
                        Log.i(LOG_TAG, movieTrailer.getName() + " is not a youtube video.");
                        continue;
                    }

                }

                if (movie.getValue() != null) {
                    movie.getValue().setTrailerList(movieTrailers);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject reviews = jsonObject.getJSONObject(context.getString(R.string
                        .review_list_path));
                JSONArray reviewResults = reviews.getJSONArray("results");
                List<MovieReview> reviewList = new ArrayList<>(reviewResults.length());
                for (int i = 0; i < reviewResults.length(); i++) {
                    Gson gson = new Gson();
                    MovieReview review = gson.fromJson(reviewResults.get(i).toString(), MovieReview.class);
                    review.setMovieId(movieId);
                    reviewList.add(review);
                }
                if (movie.getValue() != null) {
                    movie.getValue().setReviewList(reviewList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            movie.postValue(movie.getValue());
        });
    }

    public void fetchMovies(String movieListType, MutableLiveData<List<Movie>> liveMovies) {

        StringBuilder jsonString = new StringBuilder();

        // get the movies
        appExecutors.networkIO().execute(() -> {
            InputStream inputStream = null;
            try {
                String baseUrl = context.getString(R.string.base_url);
                String page = context.getString(R.string.page);
                String apiKey = context.getString(R.string.api_key);
                String languageParam = context.getString(R.string.language_param);
                String language = context.getString(R.string.language);
                Uri uri = Uri.parse(baseUrl).buildUpon()
                        .appendPath(movieListType)
                        .appendQueryParameter(page, Integer.toString(1))
                        .appendQueryParameter(languageParam, language)
                        .appendQueryParameter(apiKey, TMDB_API_KEY)
                        .build();

                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Read the input stream into a String
                inputStream = connection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.e(LOG_TAG, "Null input stream");
                    //return;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    Log.e(LOG_TAG, "Buffer length is 0");
                    //return;
                }
                jsonString.append(buffer.toString());
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Failed to close input stream");
                    }
                }
            }
            List<Movie> movieList = new ArrayList<>();
            try {
                if (jsonString == null || jsonString.toString().isEmpty()) {
                    throw new JSONException("null or empty json string");
                }
                JSONObject object = new JSONObject(jsonString.toString());
                JSONArray movieArray = object.getJSONArray("results");

                for (int i = 0; i < movieArray.length(); i++) {
                    Gson gson = new Gson();
                    Movie movie = gson.fromJson(movieArray.get(i).toString(), Movie.class);
                    String posterPath = "https://image.tmdb.org/t/p/w500/" + movie.getPosterPath();
                    movie.setPosterPath(posterPath);
                    // TODO store the poster in the db and use live data
                    String backdropUrl = "https://image.tmdb.org/t/p/w780/" + movie.getBackdropPath();
                    movie.setBackdropPath(backdropUrl);
                    // todo figure out if this is already a favorite
                //    boolean fav = isFavorite(movie.getId());
                //    movie.setFavorite(fav);
                    movieList.add(movie);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            liveMovies.postValue(movieList);
        });
    }
}
