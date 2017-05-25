package com.cellblock70.popularmovies.data;

import android.net.Uri;

/**
 * Created by BakonJarser on 5/7/2017.
 */

public class PopularMoviesContract {

    public static final String CONTENT_AUTHORITY = "com.cellblock70.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_DETAILS = "details";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_TRAILERS = "trailers";

    public static final class MovieDetailsEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_DETAILS)
                .build();

        public static final String TABLE_NAME = "details";

        // This column is the primary key that uniquely identifies the movie.
        public static final String COL_MOVIE_ID = "movie_id";

        // This column will store Y if it is a user's favorite, otherwise 'N'.  Defaults to 'N'.
        public static final String COL_FAVORITE = "favorite";

        // The movie title.
        public static final String COL_TITLE = "title";

        // The original movie title.
        public static final String COL_ORIGINAL_TITLE = "original_title";

        // The user rating of the movie.
        public static final String COL_USER_RATING = "rating";

        // The number of reviews by users.
        public static final String COL_USER_REVIEWS = "reviews";

        // The movie synopsis.
        public static final String COL_SYNOPSIS = "synopsis";

        // The movie's release date.
        public static final String COL_RELEASE_DATE = "release_date";

        // The path to the movie's poster.
        public static final String COL_POSTER_PATH = "poster_path";

        // The path to the movie's backdrop.
        public static final String COL_BACKDROP_PATH = "backdrop_path";
    }

    public static final class MovieTrailersEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRAILERS)
                .build();

        public static final String TABLE_NAME = "trailers";

        // A unique ID provided by the movie database.
        public static final String COL_ID = "id";

        // The name of the movie trailer.
        public static final String COL_NAME = "name";

        // The youtube link for the trailer.  Does not include the base URL.
        public static final String COL_LINK = "link";

        // The name of the site where the review is hosted.
        public static final String COL_SITE = "site";
    }

    public static final class MovieReviewsEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEWS)
                .build();

        public static final String TABLE_NAME = "reviews";

        // A unique ID provided by the movie database.
        public static final String COL_ID = "id";

        // The review text.
        public static final String COL_REVIEW = "review_tx";

        // The review author.
        public static final String COL_AUTHOR = "author";
    }
}
