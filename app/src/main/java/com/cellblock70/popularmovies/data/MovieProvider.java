package com.cellblock70.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieDetailsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieReviewsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieTrailersEntry;

/**
 * Content provider for the movie database.
 * <p>
 * Created by BakonJarser on 5/8/2017.
 */

public class MovieProvider extends ContentProvider {

    public static final int DETAILS = 100;
    public static final int REVIEWS = 101;
    public static final int TRAILERS = 102;
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    private static UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieDbHelper;

    /**
     * Creates a uri matcher for the class.
     *
     * @return the uri matcher.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PopularMoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PopularMoviesContract.PATH_DETAILS, DETAILS);
        matcher.addURI(authority, PopularMoviesContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority, PopularMoviesContract.PATH_TRAILERS, TRAILERS);

        return matcher;
    }

    /**
     * Returns the table name for the given URI code.
     *
     * @param uriCode - the code of the URI to return the table name of.
     * @return - the table name.
     */
    private static String getTableName(int uriCode) {
        String tableName;
        switch (uriCode) {
            case DETAILS:
                tableName = MovieDetailsEntry.TABLE_NAME;
                break;
            case REVIEWS:
                tableName = MovieReviewsEntry.TABLE_NAME;
                break;
            case TRAILERS:
                tableName = MovieTrailersEntry.TABLE_NAME;
                break;
            default:
                tableName = "";
        }
        return tableName;
    }

    /**
     * Returns the primary key column name for the given Uri.
     *
     * @param uriCode - the code of the Uri to find the primary column key for.
     * @return - the primary key column name.
     */
    private static String getPrimaryKeyCol(int uriCode) {
        String primaryKeyColName;
        switch (uriCode) {
            case DETAILS:
                primaryKeyColName = MovieDetailsEntry.COL_MOVIE_ID;
                break;
            case REVIEWS:
                primaryKeyColName = MovieReviewsEntry.COL_ID;
                break;
            case TRAILERS:
                primaryKeyColName = MovieTrailersEntry.COL_ID;
                break;
            default:
                primaryKeyColName = "";
        }
        return primaryKeyColName;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int rowsUpdated = 0;
        SQLiteDatabase db = null;
        try {
            db = mMovieDbHelper.getWritableDatabase();
            int uriCode = sUriMatcher.match(uri);
            String tableName;
            String primaryKeyColName;
            switch (uriCode) {
                case DETAILS:
                case REVIEWS:
                case TRAILERS:
                    tableName = getTableName(uriCode);
                    primaryKeyColName = getPrimaryKeyCol(uriCode);
                    break;
                default:
                    return super.bulkInsert(uri, values);
            }

            db.beginTransaction();
            for (ContentValues value : values) {
                // Try and do an insert.  If it already exists then do an update instead.  This will
                // preserve the user favorites column.
                long isAltered = (int) db.insertWithOnConflict(tableName, null, value,
                        SQLiteDatabase.CONFLICT_IGNORE);

                if (isAltered == -1) {
                    isAltered = db.update(tableName, value, primaryKeyColName + "=?", new String[]{
                            value.get(primaryKeyColName).toString()});
                }

                if (isAltered != -1) {
                    rowsUpdated++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Bulk insert failed: " + e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(this.getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();
            int uriCode = sUriMatcher.match(uri);
            String tableName = getTableName(uriCode);

            cursor = db.query(tableName, projection, selection, selectionArgs, null, null,
                    sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new RuntimeException("Insert not implemented.  Use bulkInsert.");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsUpdated = 0;

        // If the selection is null then pass 1 so that we get back a number of rows deleted.
        if (null == selection) selection = "1";

        SQLiteDatabase db = null;
        try {
            db = mMovieDbHelper.getWritableDatabase();
            db.beginTransaction();
            int uriCode = sUriMatcher.match(uri);
            String tableName;
            switch (uriCode) {
                case DETAILS:
                case REVIEWS:
                case TRAILERS:
                    tableName = getTableName(uriCode);
                    break;
                default:
                    throw new RuntimeException("Cannot delete for Uri " + uriCode);
            }

            db.delete(tableName, selection, selectionArgs);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Delete failed: " + e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
        return rowsUpdated;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = null;
        int rowsUpdated = 0;
        try {
            db = mMovieDbHelper.getWritableDatabase();
            int uriCode = sUriMatcher.match(uri);
            String tableName;
            switch (uriCode) {
                case DETAILS:
                case REVIEWS:
                case TRAILERS:
                    tableName = getTableName(uriCode);
                    break;
                default:
                    throw new RuntimeException("Update not implemented for Uri " + uri.toString());
            }

            db.beginTransaction();
            rowsUpdated = db.update(tableName, values, selection, selectionArgs);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Update failed: " + e.getMessage());
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
        return rowsUpdated;
    }
}
