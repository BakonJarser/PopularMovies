package com.cellblock70.popularmovies.UI.Details;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cellblock70.popularmovies.R;
import com.cellblock70.popularmovies.UI.MovieList.MovieListFragment;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
import com.cellblock70.popularmovies.data.database.Movie;
import com.cellblock70.popularmovies.data.database.MovieReview;
import com.cellblock70.popularmovies.data.database.MovieTrailer;
import com.cellblock70.popularmovies.databinding.FragmentMovieDetailsBinding;


public class MovieDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private LinearLayout mTrailerLinearLayout;
    private LinearLayout mReviewLinearLayout;
    private Integer movieId;
    private FragmentMovieDetailsBinding mDetailBinding;
    private MovieDetailViewModel viewModel;

    /**
     * Updates the tables in the database to reflect the users new preference.
     *
     * @param view - the view that was clicked to toggle the favorites.
     */
    public void onFavoriteClicked(View view) {
        viewModel.setIsFavorite(((ToggleButton) view).isChecked());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            movieId = savedInstanceState.getInt(MovieListFragment.MOVIE_ID, -1);
        } else {
            movieId = getIntent().getIntExtra(MovieListFragment.MOVIE_ID, -1);
        }
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.fragment_movie_details);
        viewModel = new MovieDetailViewModel(this.getApplication(), movieId);
        viewModel.getMovieLiveData().observe(this, movie -> {
            if (movie != null && movie.getMovie() != null) { loadMovieIntoView(movie); }
        });
        if (savedInstanceState != null) {
            Log.e(LOG_TAG, "Saved instance wasn't null");

            int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
            if (position != null && position.length > 0) {
                mDetailBinding.activityMovieDetailsScrollview.scrollTo(position[0], position[1]);
            }
        }
    }

    private void loadMovieIntoView(CompleteMovie completeMovie) {
        mTrailerLinearLayout = mDetailBinding.trailerListView;
        mReviewLinearLayout = mDetailBinding.reviewListView;
        Movie movie = completeMovie.getMovie();
        boolean isFavorite = completeMovie.getIsFavorite();
        populateReviewAndTrailerViews(completeMovie);

        if (movie == null) {
            Log.e(LOG_TAG, "Movie doesn't exist in database:" + movieId);
            mDetailBinding.titleView.setText(R.string.error_could_not_find_movie_in_db);
        } else {
            Log.i(LOG_TAG, "Loading movie " + movie.getTitle());
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
                        // FIXME figure out why this is being called
                        Log.e(LOG_TAG, "Failed to load image.  Load was canceled.");
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition transition) {
                        Log.d(LOG_TAG, "Setting background for movie " + movieId);
                        mDetailBinding.activityMovieDetailsScrollview.setBackground(resource);

                    }

                });
                ToggleButton favoriteButton = mDetailBinding.favoriteButton;
                favoriteButton.setChecked(isFavorite);
                mDetailBinding.originalTitleView.setText(movie.getOriginalTitle());
                mDetailBinding.titleView.setText(movie.getTitle());
                mDetailBinding.synopsis.setText(movie.getSynopsis());
                String userRating = movie.getRating() + " (" + movie.getReviews() + " votes)";
                mDetailBinding.userRating.setText(userRating);
                mDetailBinding.releaseDate.setText(movie.getReleaseDate());

            } catch (Exception e) {
                Log.e(LOG_TAG, "onCreate: " + e.getMessage());
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ScrollView scrollView = mDetailBinding.activityMovieDetailsScrollview;
        Log.e(LOG_TAG, "Y: " + scrollView.getScrollY());
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
        outState.putInt(MovieListFragment.MOVIE_ID, movieId);
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
                startActivity(intent);
        });
        return button;
    }
}