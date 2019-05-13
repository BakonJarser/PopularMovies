package com.cellblock70.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cellblock70.popularmovies.data.PopularMoviesContract;

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

public class MainActivity extends AppCompatActivity {

    public static final String POSTERS = "posters";
    public static final String MOVIE_IDS = "movieIds";
    private ImageViewAdapter mMovieAdapter;
    private ArrayList<String> posters = new ArrayList<>();
    private int[] movieIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_movie_list);
        RecyclerView movieGrid = findViewById(R.id.movie_grid);
        int columns = getResources().getConfiguration().orientation == OrientationHelper
                .VERTICAL ? 2 : 4;
        GridLayoutManager layoutManager = new GridLayoutManager(this, columns,
                RecyclerView.VERTICAL, false);
        movieGrid.setLayoutManager(layoutManager);
        movieGrid.setHasFixedSize(true);
        mMovieAdapter = new ImageViewAdapter(this);
        movieGrid.setAdapter(mMovieAdapter);

        if (savedInstanceState != null) {
            posters = savedInstanceState.getStringArrayList(POSTERS);
            movieIds = savedInstanceState.getIntArray(MOVIE_IDS);
        } else {
            AsyncTask<Void, Void, Void> task = new PopularMovieTask();
            task.execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(MOVIE_IDS, movieIds);
        outState.putStringArrayList(POSTERS, posters);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class PopularMovieTask extends AsyncTask<Void, Void, Void> {
        private static final String TMDB_API_KEY = BuildConfig.TMDB_MAP_API_KEY;
        // JSON movie object keys.
        private static final String ORIGINAL_TITLE = "original_title";
        private static final String TITLE = "title";
        private static final String OVERVIEW = "overview";
        private static final String VOTE_AVERAGE = "vote_average";
        private static final String VOTE_COUNT = "vote_count";
        private static final String RELEASE_DATE = "release_date";
        private static final String MOVIE_ID = "id";
        private final String LOG_TAG = PopularMovieTask.class.getSimpleName();
        private final String BASE_URL = getString(R.string.base_url);
        private final String PAGE = getString(R.string.page);
        private final String API_KEY = getString(R.string.api_key);
        private final String LANGUAGE = getString(R.string.language_param);
        private final String language = getString(R.string.language);

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                    (MainActivity.this);
            String movieListType = sharedPreferences.getString(getString(R.string.movie_list_type), "popular");

            if (getString(R.string.favorites).equals(movieListType)) {
                getFavoritesFromDatabase(movieListType);
            } else {
                getListFromServer(movieListType);
            }

            return null;
        }

        /**
         * Retrieves the movie poster paths for the user's favorites from the database and loads
         * them into the adapter view.
         */
        private void getFavoritesFromDatabase(String movieListType) {
            // TODO Get and store images in the database instead of loading them every time.

            if (movieListType.equals(getString(R.string.favorites))) {
                Cursor posterCursor = getContentResolver().query
                        (PopularMoviesContract.MovieDetailsEntry.CONTENT_URI,
                                new String[]{PopularMoviesContract.MovieDetailsEntry
                                        .COL_POSTER_PATH, PopularMoviesContract.MovieDetailsEntry.COL_MOVIE_ID},
                                PopularMoviesContract.MovieDetailsEntry.COL_FAVORITE + "=? ",
                                new String[]{"Y"}, null);
                if (posterCursor == null) {
                    Log.e(LOG_TAG, "Failed to retrieve favorite movie poster paths from the database");
                } else {
                    movieIds = new int[posterCursor.getCount()];
                    for (posterCursor.moveToFirst(); !posterCursor.isAfterLast(); posterCursor
                            .moveToNext()) {
                        String posterPath = posterCursor.getString(posterCursor.getColumnIndex
                                (PopularMoviesContract.MovieDetailsEntry.COL_POSTER_PATH));
                        posters.add(posterPath);
                        Integer movieId = posterCursor.getInt(posterCursor.getColumnIndex
                                (PopularMoviesContract.MovieDetailsEntry.COL_MOVIE_ID));
                        movieIds[posterCursor.getPosition()] = movieId;
                    }
                    posterCursor.close();
                }
            }
        }

        /**
         * Retrieves the information from the movie list from the server and stores it in the
         * database.
         *
         * @param movieListType - the type of movie list to retrieve.
         */
        private void getListFromServer(String movieListType) {
            String jsonString = null;
            InputStream inputStream = null;
            try {
                Uri uri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(movieListType)
                        .appendQueryParameter(PAGE, Integer.toString(1))
                        .appendQueryParameter(LANGUAGE, language)
                        .appendQueryParameter(API_KEY, TMDB_API_KEY)
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
                    return;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    Log.e(LOG_TAG, "Buffer length is 0");
                    return;
                }
                jsonString = buffer.toString();
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

            try {
                if (jsonString == null || jsonString.isEmpty()) {
                    throw new JSONException("null or empty json string");
                }
                JSONObject object = new JSONObject(jsonString);
                JSONArray movieArray = object.getJSONArray(getString(R.string.results));
                ContentValues[] contentValues = new ContentValues[movieArray.length()];
                movieIds = new int[movieArray.length()];

                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject movie = (JSONObject) movieArray.get(i);
                    ContentValues movieValues = new ContentValues();
                    Integer movieId = movie.getInt(MOVIE_ID);
                    movieIds[i] = movieId;
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_MOVIE_ID, movieId);
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_ORIGINAL_TITLE, movie.getString(ORIGINAL_TITLE));
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_SYNOPSIS, movie.getString(OVERVIEW));
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_TITLE, movie.getString(TITLE));
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_USER_RATING, movie.getString(VOTE_AVERAGE));
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_USER_REVIEWS, movie.getString(VOTE_COUNT));
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_RELEASE_DATE, movie.getString(RELEASE_DATE));
                    String posterPath = getString(R.string.base_image_url)
                            + getString(R.string.poster_size) + movie.get(getString(R.string.poster_path));
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_POSTER_PATH, posterPath);
                    posters.add(posterPath);
                    String backdropUrl = getString(R.string.base_image_url) + getString(R.string
                            .backdrop_size) + movie.get(getString(R.string.backdrop_path));
                    movieValues.put(PopularMoviesContract.MovieDetailsEntry.COL_BACKDROP_PATH, backdropUrl);

                    contentValues[i] = movieValues;
                }

                ContentResolver contentResolver = getContentResolver();
                int rowsInserted = contentResolver.bulkInsert(PopularMoviesContract.MovieDetailsEntry.CONTENT_URI,
                        contentValues);
                Log.i(LOG_TAG, "Inserted " + rowsInserted + " rows into the movie details " +
                        "database.");

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            mMovieAdapter.notifyDataSetChanged();
        }
    }

    /**
     * An adapter for loading an image into an ImageView.
     */
    private class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.PosterViewHolder> {

        private final Context mContext;

        ImageViewAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout
                    .activity_movie_grid_item, parent, false);
            view.setFocusable(true);
            return new PosterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
            ImageView posterView = holder.posterView;
            posterView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mContext).load(posters.get(position)).into(posterView);
        }

        @Override
        public int getItemCount() {
            return posters.size();
        }

        class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView posterView;

            PosterViewHolder(View itemView) {
                super(itemView);

                posterView = itemView.findViewById(R.id.activity_movie_grid_item);
                posterView.setScaleType(ImageView.ScaleType.FIT_XY);
                posterView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Intent downloadIntent = new Intent(MainActivity.this,
                        MovieDetails.class).putExtra(PopularMoviesContract.MovieDetailsEntry.COL_MOVIE_ID,
                        movieIds[getAdapterPosition()]);
                startActivity(downloadIntent);
            }
        }
    }
}
