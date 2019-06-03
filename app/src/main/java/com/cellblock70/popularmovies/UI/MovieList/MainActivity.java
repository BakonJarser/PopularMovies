package com.cellblock70.popularmovies.UI.MovieList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cellblock70.popularmovies.BuildConfig;
import com.cellblock70.popularmovies.R;
import com.cellblock70.popularmovies.SettingsActivity;
import com.cellblock70.popularmovies.UI.Details.MovieDetails;
import com.cellblock70.popularmovies.data.database.Movie;
import com.cellblock70.popularmovies.data.MovieRepository;
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

public class MainActivity extends AppCompatActivity {

    public static final String POSTERS = "posters";
    public static final String MOVIE_IDS = "movieIds";
    public static final String MOVIE_ID = "movie_id";
    private ImageViewAdapter mMovieAdapter;
    private ArrayList<String> posters = new ArrayList<>();
    private int[] movieIds;
    private MovieRepository movieRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_movie_list);
        if (movieRepository == null) {
             movieRepository = MovieRepository.provideRepository(this);
        }
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
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                    (MainActivity.this);
            String movieListType = sharedPreferences.getString(getString(R.string.movie_list_type), "popular");
            if (getString(R.string.favorites).equals(movieListType)) {
                getFavoritesFromDatabase();
            } else {
                new PopularMovieTask(movieListType).execute();
            }
        }
    }

    /**
     * Retrieves the movie poster paths for the user's favorites from the database and loads
     * them into the adapter view.
     */
    private void getFavoritesFromDatabase() {
        // TODO Get and store images in the database instead of loading them every time.

        MovieViewModel movieViewModel = new MovieViewModel(MainActivity.this.getApplication());
        LiveData<List<Movie>> favoriteData = movieViewModel.getFavorites();
        favoriteData.observe(MainActivity.this, movies -> {
            movieIds = new int[movies.size()];
            int index = 0;
            for (Movie movie : movies) {
                posters.add(movie.getPosterPath());
                movieIds[index++] = movie.getId();
            }
        });
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
        private final String LOG_TAG = PopularMovieTask.class.getSimpleName();
        private final String BASE_URL = getString(R.string.base_url);
        private final String PAGE = getString(R.string.page);
        private final String API_KEY = getString(R.string.api_key);
        private final String LANGUAGE = getString(R.string.language_param);
        private final String language = getString(R.string.language);
        private final String movieListType;

        PopularMovieTask(String movieListType) {
            super();
            this.movieListType = movieListType;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getListFromServer();
            return null;
        }

        /**
         * Retrieves the information from the movie list from the server and stores it in the
         * database.
         *
         */
        private void getListFromServer() {
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
                List<Movie> movieList = new ArrayList<>(movieArray.length());
                movieIds = new int[movieArray.length()];

                for (int i = 0; i < movieArray.length(); i++) {
                    Gson gson = new Gson();
                    Movie movie = gson.fromJson(movieArray.get(i).toString(), Movie.class);
                    movieIds[i] = movie.getId();
                    String posterPath = getString(R.string.base_image_url) + getString(R.string.poster_size) + movie.getPosterPath();
                    movie.setPosterPath(posterPath);
                    // TODO store the poster in the db and use live data
                    posters.add(posterPath);
                    String backdropUrl = getString(R.string.base_image_url) + getString(R.string
                            .backdrop_size) + movie.getBackdropPath();
                    movie.setBackdropPath(backdropUrl);
                    boolean fav = movieRepository.isFavoriteAlreadyInBackground(movie.getId());
                    movie.setFavorite(fav);
                    movieList.add(movie);
                }

                movieRepository.insertMoviesAlreadyInBackground(movieList);

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
                        MovieDetails.class).putExtra(MOVIE_ID,
                        movieIds[getAdapterPosition()]);
                startActivity(downloadIntent);
            }
        }
    }
}
