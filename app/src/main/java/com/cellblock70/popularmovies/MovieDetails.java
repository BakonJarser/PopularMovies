package com.cellblock70.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetails extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetails.class.getSimpleName();
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String TITLE = "title";
    private static final String OVERVIEW = "overview";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String VOTE_COUNT = "vote_count";
    private static final String RELEASE_DATE = "release_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        String jsonString = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        final View rootView = findViewById(R.id.activity_movie_details);
        Log.i(LOG_TAG, jsonString);
        setupActionBar();

        try {
            JSONObject details = new JSONObject(jsonString);

            String backdropUrl;
            final ImageView posterView = new ImageView(this);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);


            // If in landscape then load the movie backdrop, else load the movie poster.
            if (metrics.widthPixels > metrics.heightPixels) {
                backdropUrl = getString(R.string.base_image_url) + getString(R.string.backdrop_size)
                        + details.get(getString(R.string.backdrop_path));

            } else {
                backdropUrl = getString(R.string.base_image_url) + getString(R.string.poster_size)
                        + details.get(getString(R.string.poster_path));
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

            String originalTitle = getString(R.string.original_title) + details.get(ORIGINAL_TITLE);
            ((TextView) findViewById(R.id.original_title_view)).setText(originalTitle);

            String title = (String) details.get(TITLE);
            ((TextView) findViewById(R.id.title_view)).setText(title);

            String synopsis = (String) details.get(OVERVIEW);
            ((TextView) findViewById(R.id.synopsis)).setText(synopsis);

            // This could return an Integer or a Double.
            String userRating = getString(R.string.user_rating) + details.get(VOTE_AVERAGE).toString();
            ((TextView) findViewById(R.id.user_rating)).setText(userRating);

            String voteCount = getString(R.string.votes) + (details.get(VOTE_COUNT)).toString();
            ((TextView) findViewById(R.id.vote_count)).setText(voteCount);

            String releaseDate = getString(R.string.release_date) +  details.get(RELEASE_DATE);
            ((TextView) findViewById(R.id.release_date)).setText(releaseDate);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
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
}