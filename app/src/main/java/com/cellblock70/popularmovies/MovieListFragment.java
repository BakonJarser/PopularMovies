package com.cellblock70.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieDetailsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieReviewsEntry;
import com.cellblock70.popularmovies.data.PopularMoviesContract.MovieTrailersEntry;
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
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment {

    private ArrayAdapter<ImageView> mMovieAdapter;
    private List<String> posters = new ArrayList<>();
    private Integer[] movieIds;

    public MovieListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().getContentResolver().delete(MovieDetailsEntry.CONTENT_URI, MovieDetailsEntry
                .COL_FAVORITE + "=? ", new String[]{"N"});
        getContext().getContentResolver().delete(MovieReviewsEntry.CONTENT_URI, MovieDetailsEntry
                .COL_FAVORITE + "=? ", new String[]{"N"});
        getContext().getContentResolver().delete(MovieTrailersEntry.CONTENT_URI, MovieDetailsEntry
                .COL_FAVORITE + "=? ", new String[]{"N"});
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        mMovieAdapter = new ImageViewAdapter();

        final GridView movieGrid = (GridView) rootView.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(mMovieAdapter);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent downloadIntent = new Intent(getActivity(),
                        MovieDetails.class).putExtra(MovieDetailsEntry.COL_MOVIE_ID, movieIds[i]);
                startActivity(downloadIntent);
            }
        });

        AsyncTask<Void, Void, Void> task = new PopularMovieTask();
        task.execute();

        return rootView;
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
                    (getContext());
            String movieListType = sharedPreferences.getString(getString(R.string.movie_list_type), "popular");

            if (movieListType.equals(getString(R.string.favorites))) {
                getFavoritesFromDatabase();
            } else {
                getListFromServer(movieListType);
            }

            return null;
        }

        /**
         * Retrieves the movie poster paths for the user's favorites from the database and loads
         * them into the adapter view.
         */
        private void getFavoritesFromDatabase() {
            // TODO Get and store images in the database instead of loading them every time.

            Cursor posterCursor = MovieListFragment.this.getContext().getContentResolver().query
                    (MovieDetailsEntry.CONTENT_URI, new String[]{MovieDetailsEntry
                                    .COL_POSTER_PATH, MovieDetailsEntry.COL_MOVIE_ID},
                            MovieDetailsEntry.COL_FAVORITE + "=? ", new String[]{"Y"}, null);
            if (posterCursor == null) {
                Log.e(LOG_TAG, "Failed to retrieve favorite movie poster paths from the database");
            } else {
                mMovieAdapter.clear();
                movieIds = new Integer[posterCursor.getCount()];
                for (posterCursor.moveToFirst(); !posterCursor.isAfterLast(); posterCursor
                        .moveToNext()) {
                    String posterPath = posterCursor.getString(posterCursor.getColumnIndex
                            (MovieDetailsEntry.COL_POSTER_PATH));
                    posters.add(posterPath);
                    Integer movieId = posterCursor.getInt(posterCursor.getColumnIndex
                            (MovieDetailsEntry.COL_MOVIE_ID));
                    movieIds[posterCursor.getPosition()] = movieId;
                    ImageView imageView = new ImageView(getActivity());
                    mMovieAdapter.add(imageView);
                }
                posterCursor.close();
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
                JSONObject object = new JSONObject(jsonString);
                JSONArray movieArray = object.getJSONArray(getString(R.string.results));
                ContentValues[] contentValues = new ContentValues[movieArray.length()];
                movieIds = new Integer[movieArray.length()];

                for (int i = 0; i < movieArray.length(); i++) {
                    JSONObject movie = (JSONObject) movieArray.get(i);
                    ContentValues movieValues = new ContentValues();
                    Integer movieId = movie.getInt(MOVIE_ID);
                    movieIds[i] = movieId;
                    movieValues.put(MovieDetailsEntry.COL_MOVIE_ID, movieId);
                    movieValues.put(MovieDetailsEntry.COL_ORIGINAL_TITLE, movie.getString(ORIGINAL_TITLE));
                    movieValues.put(MovieDetailsEntry.COL_SYNOPSIS, movie.getString(OVERVIEW));
                    movieValues.put(MovieDetailsEntry.COL_TITLE, movie.getString(TITLE));
                    movieValues.put(MovieDetailsEntry.COL_USER_RATING, movie.getString(VOTE_AVERAGE));
                    movieValues.put(MovieDetailsEntry.COL_USER_REVIEWS, movie.getString(VOTE_COUNT));
                    movieValues.put(MovieDetailsEntry.COL_RELEASE_DATE, movie.getString(RELEASE_DATE));
                    String posterPath = getString(R.string.base_image_url)
                            + getString(R.string.poster_size) + movie.get(getString(R.string.poster_path));
                    movieValues.put(MovieDetailsEntry.COL_POSTER_PATH, posterPath);
                    posters.add(posterPath);
                    String backdropUrl = getString(R.string.base_image_url) + getString(R.string
                            .backdrop_size) + movie.get(getString(R.string.backdrop_path));
                    movieValues.put(MovieDetailsEntry.COL_BACKDROP_PATH, backdropUrl);

                    contentValues[i] = movieValues;
                }

                ContentResolver contentResolver = getContext().getContentResolver();
                int rowsInserted = contentResolver.bulkInsert(MovieDetailsEntry.CONTENT_URI,
                        contentValues);
                Log.i(LOG_TAG, "Inserted " + rowsInserted + " rows into the movie details " +
                        "database.");

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Void result){
            mMovieAdapter.clear();
            for (int i = 0; i < posters.size(); i++) {
                ImageView imageView = new ImageView(getActivity());
                mMovieAdapter.add(imageView);
            }
        }
    }

    /**
     * An adapter for loading an image into an ImageView.
     */
    private class ImageViewAdapter extends ArrayAdapter<ImageView>{

        ImageViewAdapter() {
            super(getActivity(), R.layout.activity_movie_grid_item);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent){

            ImageView imageView;
            if (convertView == null){
                imageView = new ImageView(getContext());
            }
            else{
                imageView = (ImageView) convertView;
            }
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            // Find the path to the poster in the posters list and load it into the ImageView.
            Picasso.with(getContext()).load(posters.get(position)).into(imageView);

            return imageView;
        }
    }
}
