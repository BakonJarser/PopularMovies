package com.cellblock70.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieDetailsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieReviewsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieTrailersEntry;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieDetails extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetails.class.getSimpleName();
    private LinearLayout mTrailerLinearLayout;
    private LinearLayout mReviewLinearLayout;
    private Integer movieId;
    private int[] position = null;

    /**
     * Updates the tables in the database to reflect the users new preference.
     *
     * @param view - the view that was clicked to toggle the favorites.
     */
    public void onFavoriteClicked(View view) {
        ContentValues newValue = new ContentValues(1);
        String favorite = ((ToggleButton) view).isChecked() ? "Y" : "N";
        newValue.put(MovieDetailsEntry.COL_FAVORITE, favorite);
        String where = MovieDetailsEntry.COL_MOVIE_ID + " =?";
        String[] argument = new String[]{movieId.toString()};
        getContentResolver().update(MovieDetailsEntry.CONTENT_URI, newValue, where, argument);
        getContentResolver().update(MovieReviewsEntry.CONTENT_URI, newValue, where, argument);
        getContentResolver().update(MovieTrailersEntry.CONTENT_URI, newValue, where, argument);
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
        final View rootView = findViewById(R.id.details_background_image_layout);
        mTrailerLinearLayout = (LinearLayout) rootView.findViewById(R.id.trailer_list_view);
        mReviewLinearLayout = (LinearLayout) rootView.findViewById(R.id.review_list_view);

        movieId = getIntent().getIntExtra(MovieDetailsEntry.COL_MOVIE_ID, -1);

        // See if the movie is a user's favorite.
        Cursor details = getContentResolver().query(MovieDetailsEntry.CONTENT_URI, null,
                MovieDetailsEntry.COL_MOVIE_ID + " =? AND " + MovieDetailsEntry.COL_FAVORITE + " =?",
                new String[]{movieId.toString(), "Y"}, null);

        // If the movie is a favorite then all of the information is already stored in the db.
        if (details != null && details.moveToFirst()) {
            getMovieFromDatabase();
        } else {
            // The movie wasn't in the database so get the reviews and trailers from the server.
            new LoadTrailersAndReviewsTask().execute(movieId);

            // The movie details are in the database since they are loaded and stored when the
            // movie list is loaded.
            details = getContentResolver().query(MovieDetailsEntry
                    .CONTENT_URI, null, MovieDetailsEntry.COL_MOVIE_ID + " = ? ", new
                    String[]{movieId.toString()}, null);
        }

        if (details == null || !details.moveToFirst()) {
            Log.e(LOG_TAG, "Movie doesn't exist in database:" + movieId);
            ((TextView) rootView.findViewById(R.id.title_view)).setText(R.string.error_could_not_find_movie_in_db);
        }

        try {
            String backdropUrl;
            final ImageView posterView = new ImageView(this);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            // If in landscape then load the movie backdrop, else load the movie poster.
            if (metrics.widthPixels > metrics.heightPixels) {
                int backDropColInt = details.getColumnIndex(MovieDetailsEntry.COL_BACKDROP_PATH);
                backdropUrl = details.getString(backDropColInt);
            } else {
                int backDropColInt = details.getColumnIndex(MovieDetailsEntry.COL_POSTER_PATH);
                backdropUrl = details.getString(backDropColInt);
            }

            Picasso.with(this).load(backdropUrl).resize(metrics.widthPixels, metrics
                    .heightPixels).centerCrop().into(posterView, new Callback() {
                @Override
                public void onSuccess() {
                    rootView.setBackground(posterView.getDrawable());
                }

                @Override
                public void onError() {
                    Log.e(LOG_TAG, "Failed to load image.");
                }
            });

            int favoriteColInt = details.getColumnIndex(MovieDetailsEntry.COL_FAVORITE);
            String favorite = details.getString(favoriteColInt);
            ToggleButton favoriteButton = ((ToggleButton) rootView.findViewById(R.id.favorite_button));

            if (favorite.equals("Y")) {
                favoriteButton.setChecked(true);
            } else {
                favoriteButton.setChecked(false);
            }

            int origTitleColInt = details.getColumnIndex(MovieDetailsEntry.COL_ORIGINAL_TITLE);
            String originalTitle = getString(R.string.original_title) + details.getString(origTitleColInt);
            ((TextView) rootView.findViewById(R.id.original_title_view)).setText(originalTitle);

            int titleColInt = details.getColumnIndex(MovieDetailsEntry.COL_TITLE);
            String title = details.getString(titleColInt);
            ((TextView) rootView.findViewById(R.id.title_view)).setText(title);

            int synColInt = details.getColumnIndex(MovieDetailsEntry.COL_SYNOPSIS);
            String synopsis = details.getString(synColInt);
            ((TextView) rootView.findViewById(R.id.synopsis)).setText(synopsis);

            int userRatingColInt = details.getColumnIndex(MovieDetailsEntry.COL_USER_RATING);
            int voteCountColInt = details.getColumnIndex(MovieDetailsEntry.COL_USER_REVIEWS);
            Double rating = details.getDouble(userRatingColInt);
            Integer voteCount = details.getInt(voteCountColInt);
            String userRating = getString(R.string.user_rating) + rating + " (" + voteCount + " votes)";
            ((TextView) rootView.findViewById(R.id.user_rating)).setText(userRating);

            int releaseDateColInt = details.getColumnIndex(MovieDetailsEntry.COL_RELEASE_DATE);
            String releaseDate = getString(R.string.release_date) + details.getString(releaseDateColInt);
            ((TextView) rootView.findViewById(R.id.release_date)).setText(releaseDate);

        } catch (Exception e) {
            Log.e(LOG_TAG, "onCreate: " + e.getMessage());
        } finally {
            if (details != null) {
                details.close();
            }

            if (position != null) {
                final ScrollView scrollView = (ScrollView) findViewById(R.id
                        .activity_movie_details_scrollview);
                Log.e(LOG_TAG, "position wasn't null " + position[0] + "  " + position[1]);
                scrollView.post(new Runnable() {
                    public void run() {
                        Log.e(LOG_TAG, "scrolling");
                        scrollView.scrollTo(position[0], position[1]);
                    }
                });
            }
        }
    }

    private void getMovieFromDatabase() {
        // Get the trailers from the database.
        Cursor trailers = getContentResolver().query(MovieTrailersEntry.CONTENT_URI, null,
                MovieDetailsEntry.COL_MOVIE_ID + " =? ", new String[]{movieId.toString()}, null);
        if (trailers == null || !trailers.moveToFirst()) {
            Log.e(LOG_TAG, "Failed to retrieve trailers for movie " + movieId);
        } else {
            int nameColIndex = trailers.getColumnIndex(MovieTrailersEntry.COL_NAME);
            int keyColIndex = trailers.getColumnIndex(MovieTrailersEntry.COL_LINK);

            // Load each trailer button into the view.
            for (int i = 0; i < trailers.getCount(); i++) {
                mTrailerLinearLayout.addView(getTrailerButton(trailers.getString(keyColIndex),
                        trailers.getString(nameColIndex)));
                trailers.moveToNext();
            }
            trailers.close();
        }

        // Get the reviews from the database.
        Cursor reviews = getContentResolver().query(MovieReviewsEntry.CONTENT_URI, null,
                MovieDetailsEntry.COL_MOVIE_ID + " =? ", new String[]{movieId.toString()}, null);
        if (reviews == null || !reviews.moveToFirst()) {
            Log.e(LOG_TAG, "Failed to retrieve reviews for movie " + movieId);
        } else {
            int authorColIndex = reviews.getColumnIndex(MovieReviewsEntry.COL_AUTHOR);
            int reviewColIndex = reviews.getColumnIndex(MovieReviewsEntry.COL_REVIEW);

            // Load each review into the view.
            for (int i = 0; i < reviews.getCount(); i++) {
                mReviewLinearLayout.addView(getReviewLayout(reviews.getString
                        (authorColIndex), reviews.getString(reviewColIndex)));
                reviews.moveToNext();
            }
            reviews.close();
        }

        final ScrollView scrollView = (ScrollView) findViewById(R.id
                .activity_movie_details_scrollview);

        if (position != null) {
            Log.e(LOG_TAG, "position wasn't null " + position[0] + "  " + position[1]);
            scrollView.post(new Runnable() {
                public void run() {
                    Log.e(LOG_TAG, "scrolling");
                    scrollView.scrollTo(position[0], position[1]);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ScrollView scrollView = (ScrollView) findViewById(R.id.activity_movie_details_scrollview);
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
        LinearLayout reviewLayout = (LinearLayout) getLayoutInflater().inflate(R.layout
                .movie_review, null);
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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Log.i(LOG_TAG, uri.toString());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't open trailer, no " +
                            "receiving apps installed!");
                }
            }
        });
        return button;
    }

    /**
     * An AsyncTask used to load trailers and reviews into the view and store them in the db.
     */
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
                ContentValues[] dbValues = new ContentValues[trailerResults.length()];
                for (int i = 0; i < trailerResults.length(); i++) {

                    JSONObject result = (JSONObject) trailerResults.get(i);
                    String site = result.getString("site");
                    String key = result.getString("key");
                    String name = result.getString("name");
                    String id = result.getString("id");
                    // Make sure this is a youtube video.
                    if (!site.equalsIgnoreCase("youtube")) {
                        Log.i(LOG_TAG, name + " is not a youtube video.");
                        continue;
                    }

                    Button button = getTrailerButton(key, name);
                    mTrailerLinearLayout.addView(button);

                    ContentValues reviewContents = new ContentValues(4);
                    reviewContents.put(MovieTrailersEntry.COL_NAME, name);
                    reviewContents.put(MovieTrailersEntry.COL_ID, id);
                    reviewContents.put(MovieTrailersEntry.COL_LINK, key);
                    reviewContents.put(MovieTrailersEntry.COL_SITE, site);
                    reviewContents.put(MovieDetailsEntry.COL_MOVIE_ID, movieId);
                    dbValues[i] = reviewContents;
                }
                if (dbValues.length > 0) {
                    getContentResolver().bulkInsert(MovieTrailersEntry.CONTENT_URI, dbValues);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject reviews = jsonObject.getJSONObject(getString(R.string
                        .review_list_path));
                JSONArray reviewResults = reviews.getJSONArray("results");
                ContentValues[] dbValues = new ContentValues[reviewResults.length()];

                for (int i = 0; i < reviewResults.length(); i++) {
                    JSONObject review = (JSONObject) reviewResults.get(i);
                    String author = review.getString("author").trim();
                    String id = review.getString("id");
                    String content = review.getString("content").trim();

                    LinearLayout reviewLayout = getReviewLayout(author, content);
                    mReviewLinearLayout.addView(reviewLayout);

                    // Create a content values so that this can be added to the db.
                    ContentValues trailerContents = new ContentValues(4);
                    trailerContents.put(MovieReviewsEntry.COL_AUTHOR, author);
                    trailerContents.put(MovieReviewsEntry.COL_ID, id);
                    trailerContents.put(MovieReviewsEntry.COL_REVIEW, content);
                    trailerContents.put(MovieDetailsEntry.COL_MOVIE_ID, movieId);
                    dbValues[i] = trailerContents;
                }
                if (dbValues.length > 0) {
                    getContentResolver().bulkInsert(MovieReviewsEntry.CONTENT_URI, dbValues);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final ScrollView scrollView = (ScrollView) findViewById(R.id
                    .activity_movie_details_scrollview);

            if (position != null) {
                Log.e(LOG_TAG, "position wasn't null " + position[0] + "  " + position[1]);
                scrollView.post(new Runnable() {
                    public void run() {
                        Log.e(LOG_TAG, "scrolling");
                        scrollView.scrollTo(position[0], position[1]);
                    }
                });
            }
        }
    }
}