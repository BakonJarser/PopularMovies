package com.cellblock70.popularmovies.UI.Details;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cellblock70.popularmovies.BuildConfig;
import com.cellblock70.popularmovies.R;
import com.cellblock70.popularmovies.UI.MovieList.MainActivity;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
import com.cellblock70.popularmovies.data.database.Movie;
import com.cellblock70.popularmovies.data.MovieRepository;
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


public class MovieDetails extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetails.class.getSimpleName();
    private LinearLayout mTrailerLinearLayout;
    private LinearLayout mReviewLinearLayout;
    private Integer movieId;
    private int[] position = null;
    private MovieRepository movieRepository;
    private MovieDetailViewModel viewModel;

    /**
     * Updates the tables in the database to reflect the users new preference.
     *
     * @param view - the view that was clicked to toggle the favorites.
     */
    public void onFavoriteClicked(View view) {
        movieRepository.updateFavorite(((ToggleButton) view).isChecked(), movieId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_movie_details);
        loadMovieDetailsIntoView();
        if (savedInstanceState != null) {
            Log.e(LOG_TAG, "Saved instance wasn't null");

            // Save the scroll position information but don't scroll until all views have been
            // dynamically loaded.
            position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        }
    }

    private void loadMovieDetailsIntoView() {
        movieId = getIntent().getIntExtra(MainActivity.MOVIE_ID, -1);
        movieRepository = MovieRepository.provideRepository(this);
        MovieDetailViewModelFactory factory = new MovieDetailViewModelFactory(movieRepository, movieId);
        viewModel = factory.create(MovieDetailViewModel.class);
        viewModel.getMovieLiveData().observe(this, completeMovie -> {
            Log.d(LOG_TAG, "Movie Details have changed for movie " + movieId);
                if (completeMovie != null && completeMovie.getMovie() != null) loadMovieIntoView(completeMovie);
        });

    }

    private void loadMovieIntoView(CompleteMovie completeMovie) {
        final View rootView = findViewById(R.id.activity_movie_details_scrollview);
        mTrailerLinearLayout = rootView.findViewById(R.id.trailer_list_view);
        mReviewLinearLayout = rootView.findViewById(R.id.review_list_view);
        Movie movie = completeMovie.getMovie();
        if (completeMovie.getTrailerList().isEmpty() || completeMovie.getReviewList().isEmpty()) {
            Log.d(LOG_TAG, "Trailer list or review list was empty");
            new LoadTrailersAndReviewsTask().execute(movieId);
        } else {
            populateReviewAndTrailerViews(completeMovie);
        }

        if (movie == null) {
            Log.e(LOG_TAG, "Movie doesn't exist in database:" + movieId);
            ((TextView) rootView.findViewById(R.id.title_view)).setText(R.string.error_could_not_find_movie_in_db);
        } else {

            try {
                final String backdropUrl;
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                // If in landscape then load the movie backdrop, else load the movie poster.
                if (metrics.widthPixels > metrics.heightPixels) {
                    backdropUrl = movie.getBackdropPath();
                } else {
                    backdropUrl = movie.getPosterPath();
                }

                Glide.with(this).load(backdropUrl).override(metrics.widthPixels, metrics
                        .heightPixels).centerCrop().into(new CustomTarget<Drawable>() {
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        Log.e(LOG_TAG, "Failed to load image.  Load was canceled.");
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition transition) {
                        rootView.setBackground(resource);

                    }

                });
                ToggleButton favoriteButton = rootView.findViewById(R.id.favorite_button);
                favoriteButton.setChecked(movie.getFavorite());
                ((TextView) rootView.findViewById(R.id.original_title_view)).setText(movie.getOriginalTitle());
                ((TextView) rootView.findViewById(R.id.title_view)).setText(movie.getTitle());
                ((TextView) rootView.findViewById(R.id.synopsis)).setText(movie.getSynopsis());
                String userRating = movie.getRating() + " (" + movie.getReviews() + " votes)";
                ((TextView) rootView.findViewById(R.id.user_rating)).setText(userRating);
                ((TextView) rootView.findViewById(R.id.release_date)).setText(movie.getReleaseDate());

            } catch (Exception e) {
                Log.e(LOG_TAG, "onCreate: " + e.getMessage());
            } finally {
                // todo Do I still need this or was this solving a bug that has been fixed in new android release?
                if (position != null) {
                    Log.e(LOG_TAG, "position wasn't null " + position[0] + "  " + position[1]);
                    rootView.post(() ->{
                            Log.e(LOG_TAG, "scrolling");
                            rootView.scrollTo(position[0], position[1]);
                    });
                }
            }
        }
    }

    private void populateReviewAndTrailerViews(CompleteMovie movie) {
        if (movie.getTrailerList() == null || movie.getTrailerList().isEmpty()) {
            Log.e(LOG_TAG, "Failed to retrieve trailers for movie " + movieId);
        } else {
            mTrailerLinearLayout.removeAllViews();
            for (MovieTrailer movieTrailer : movie.getTrailerList()) {
                mTrailerLinearLayout.addView(getTrailerButton(movieTrailer.getLink(),
                        movieTrailer.getName()));
            }
        }

        if (movie.getReviewList() == null || movie.getReviewList().isEmpty()) {
            Log.e(LOG_TAG, "Failed to retrieve reviews for movie " + movieId);
        } else {
            mReviewLinearLayout.removeAllViews();
            // Load each review into the view.
            for (MovieReview review : movie.getReviewList()) {
                mReviewLinearLayout.addView(getReviewLayout(review.getAuthor(), review.getReviewText()));
            }
        }

        // todo Do I still need this or was this solving a bug that has been fixed in new android release?
        final ScrollView scrollView = findViewById(R.id.activity_movie_details_scrollview);

        if (position != null) {
            Log.e(LOG_TAG, "position wasn't null " + position[0] + "  " + position[1]);
            scrollView.post(() -> {
                    Log.e(LOG_TAG, "scrolling");
                    scrollView.scrollTo(position[0], position[1]);
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ScrollView scrollView = findViewById(R.id.activity_movie_details_scrollview);
        Log.e(LOG_TAG, "Y: " + scrollView.getScrollY());
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Loads the review into a linear layout.
     *
     * @param author  - the author of the review.
     * @param content - the review content.
     * @return - the linear layout containing the review.
     */
    @NonNull
    private LinearLayout getReviewLayout(String author, String content) {
        @SuppressLint("InflateParams") LinearLayout reviewLayout = (LinearLayout)
                getLayoutInflater().inflate(R.layout.movie_review, null);
        reviewLayout.setId(author.hashCode() + content.hashCode());
        ((TextView) reviewLayout.findViewById(R.id.reviewer_text_view)).setText(author);
        ((TextView) reviewLayout.findViewById(R.id.review_content_text_view)).setText
                (content);
        return reviewLayout;
    }

    /**
     * Retrieves a button that will launch the link to the trailer using an intent.
     *
     * @param key  - the youtube key for the trailer.
     * @param name - the name of the trailer.
     * @return - the button.
     */
    @NonNull
    private Button getTrailerButton(String key, String name) {
        Button button = (Button) getLayoutInflater().inflate(R.layout.trailer_button, null);
        button.setText(name);
        button.setId(key.hashCode() + name.hashCode());
        final Uri uri = Uri.parse("https://youtube.com/").buildUpon()
                .appendPath("watch")
                .appendQueryParameter("v", key).build();
        button.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Log.i(LOG_TAG, uri.toString());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't open trailer, no " +
                            "receiving apps installed!");
                }
        });
        return button;
    }

    /**
     * An AsyncTask used to load trailers and reviews into the view and store them in the db.
     */
    // TODO move this network call to MovieNetworkDataSource
    private class LoadTrailersAndReviewsTask extends AsyncTask<Integer, Void, String> {

        Integer movieId;

        @Override
        protected String doInBackground(Integer... params) {

            if (params.length != 1) throw new IllegalArgumentException("One Uri must be passed.");
            StringBuilder buffer = new StringBuilder();
            InputStream input = null;
            try {

                movieId = params[0];
                Uri uri = Uri.parse(getString(R.string.base_url))
                        .buildUpon()
                        .appendEncodedPath(movieId.toString())
                        .appendQueryParameter(getString(R.string.language_param), getString(R.string.language))
                        .appendQueryParameter(getString(R.string.api_key), BuildConfig.TMDB_MAP_API_KEY)
                        .appendQueryParameter("append_to_response", getString(R.string.trailer_list_path)
                                + "," + getString(R.string.review_list_path))
                        .build();
                URL url = new URL(uri.toString());
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                input = httpURLConnection.getInputStream();
                if (input == null) {
                    Log.e(LOG_TAG, "Input stream was null");
                    return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    Log.e(LOG_TAG, "Buffer length was 0");
                    return null;
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
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject trailers = jsonObject.getJSONObject(getString(R.string
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

                    Button button = getTrailerButton(movieTrailer.getLink(), movieTrailer.getName());
                    mTrailerLinearLayout.addView(button);
                }
                if (!movieTrailers.isEmpty()) {
                    movieRepository.insertTrailers(movieTrailers);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject reviews = jsonObject.getJSONObject(getString(R.string
                        .review_list_path));
                JSONArray reviewResults = reviews.getJSONArray("results");
                List<MovieReview> reviewList = new ArrayList<>(reviewResults.length());
                for (int i = 0; i < reviewResults.length(); i++) {
                    Gson gson = new Gson();
                    MovieReview review = gson.fromJson(reviewResults.get(i).toString(), MovieReview.class);
                    review.setMovieId(movieId);
                    reviewList.add(review);

                    LinearLayout reviewLayout = getReviewLayout(review.getAuthor(), review.getReviewText());
                    mReviewLinearLayout.addView(reviewLayout);
                }
                if (!reviewList.isEmpty()) {
                    movieRepository.insertReviews(reviewList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // todo Do I still need this or was this solving a bug that has been fixed in new android release?
            final ScrollView scrollView = findViewById(R.id.activity_movie_details_scrollview);
            if (position != null) {
                Log.e(LOG_TAG, "position wasn't null " + position[0] + "  " + position[1]);
                scrollView.post(() -> {
                        Log.e(LOG_TAG, "scrolling");
                        scrollView.scrollTo(position[0], position[1]);
                });
            }
        }
    }
}