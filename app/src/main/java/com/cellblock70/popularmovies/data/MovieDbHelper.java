package com.cellblock70.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieDetailsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieReviewsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieTrailersEntry;

/**
 * Helper class for creating the database tables for Popular Movies app.
 * <p>
 * Created by BakonJarser on 5/7/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 2;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIE_DETAILS_TABLE =
                "CREATE TABLE " + MovieDetailsEntry.TABLE_NAME
                        + " ("
                        + MovieDetailsEntry.COL_MOVIE_ID + " INTEGER PRIMARY KEY, "
                        // Use this as a flag so that favorite movies aren't deleted.
                        + MovieDetailsEntry.COL_FAVORITE + " CHAR(1) DEFAULT 'N', "
                        + MovieDetailsEntry.COL_ORIGINAL_TITLE + " VARCHAR, "
                        + MovieDetailsEntry.COL_SYNOPSIS + " TEXT, "
                        + MovieDetailsEntry.COL_TITLE + " TEXT NOT NULL, "
                        + MovieDetailsEntry.COL_USER_RATING + " REAL, "
                        + MovieDetailsEntry.COL_RELEASE_DATE + " TEXT, "
                        + MovieDetailsEntry.COL_POSTER_PATH + " TEXT, "
                        + MovieDetailsEntry.COL_BACKDROP_PATH + " TEXT, "
                        + MovieDetailsEntry.COL_USER_REVIEWS + " INTEGER);";

        db.execSQL(SQL_CREATE_MOVIE_DETAILS_TABLE);

        final String SQL_CREATE_MOVIE_TRAILER_TABLE =
                "CREATE TABLE " + MovieTrailersEntry.TABLE_NAME
                        + " ("
                        + MovieTrailersEntry.COL_ID + " TEXT PRIMARY KEY, "
                        + MovieDetailsEntry.COL_MOVIE_ID + " INTEGER NOT NULL, "
                        // Use this as a flag so that favorite movie trailers aren't deleted.
                        + MovieDetailsEntry.COL_FAVORITE + " CHAR(1) DEFAULT 'N', "
                        + MovieTrailersEntry.COL_NAME + " TEXT, "
                        + MovieTrailersEntry.COL_LINK + " TEXT NOT NULL, "
                        + MovieTrailersEntry.COL_SITE + " TEXT NOT NULL, "
                        + " FOREIGN KEY (" + MovieDetailsEntry.COL_FAVORITE + ") REFERENCES " +
                        MovieDetailsEntry.TABLE_NAME +
                        " (" + MovieDetailsEntry.COL_FAVORITE + "), "
                        + " FOREIGN KEY (" + MovieDetailsEntry.COL_MOVIE_ID + ") REFERENCES " +
                        MovieDetailsEntry.TABLE_NAME +
                        " (" + MovieDetailsEntry.COL_MOVIE_ID + "));";

        db.execSQL(SQL_CREATE_MOVIE_TRAILER_TABLE);

        final String SQL_CREATE_MOVIE_REVIEW_TABLE =
                "CREATE TABLE " + MovieReviewsEntry.TABLE_NAME
                        + " ("
                        + MovieReviewsEntry.COL_ID + " STRING PRIMARY KEY, "
                        + MovieDetailsEntry.COL_MOVIE_ID + " INTEGER NOT NULL, "
                        // Use this as a flag so that favorite movie reviews aren't deleted.
                        + MovieDetailsEntry.COL_FAVORITE + " CHAR(1) DEFAULT 'N', "
                        + MovieReviewsEntry.COL_AUTHOR + " TEXT NOT NULL, "
                        + MovieReviewsEntry.COL_REVIEW + " TEXT NOT NULL, "
                        + " FOREIGN KEY (" + MovieDetailsEntry.COL_FAVORITE + ") REFERENCES " +
                        MovieDetailsEntry.TABLE_NAME +
                        " (" + MovieDetailsEntry.COL_FAVORITE + "), "
                        + " FOREIGN KEY (" + MovieDetailsEntry.COL_MOVIE_ID + ") REFERENCES " +
                        MovieDetailsEntry.TABLE_NAME +
                        " (" + MovieDetailsEntry.COL_MOVIE_ID + "));";

        db.execSQL(SQL_CREATE_MOVIE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
