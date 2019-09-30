package com.cellblock70.popularmovies.UI.MovieList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.cellblock70.popularmovies.R;
import com.cellblock70.popularmovies.SettingsActivity;
import com.cellblock70.popularmovies.UI.Details.MovieDetailsActivity;
import com.cellblock70.popularmovies.data.database.Movie;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String POSTERS = "posters";
    public static final String MOVIE_IDS = "movieIds";
    public static final String MOVIE_ID = "movie_id";
    private ImageViewAdapter mMovieAdapter;
    private ArrayList<String> posters = new ArrayList<>();
    private int[] movieIds;
    private MovieViewModel movieViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_movie_list);
        movieViewModel = new MovieViewModel(MainActivity.this.getApplication());
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
                LiveData<List<Movie>> movieData = movieViewModel.getMovies(movieListType);
                movieData.observe(MainActivity.this, movies -> {
                    movieIds = new int[movies.size()];
                    int index = 0;
                    for (Movie movie : movies) {
                        posters.add(movie.getPosterPath());
                        movieIds[index++] = movie.getId();
                    }
                    mMovieAdapter.notifyDataSetChanged();
                });
            }
        }
    }

    /**
     * Retrieves the movie poster paths for the user's favorites from the database and loads
     * them into the adapter view.
     */
    private void getFavoritesFromDatabase() {
        // TODO Get and store images in the database instead of loading them every time.


        LiveData<List<Movie>> favoriteData = movieViewModel.getFavorites();
        favoriteData.observe(MainActivity.this, movies -> {
            movieIds = new int[movies.size()];
            int index = 0;
            for (Movie movie : movies) {
                posters.add(movie.getPosterPath());
                movieIds[index++] = movie.getId();
            }
            mMovieAdapter.notifyDataSetChanged();
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
                        MovieDetailsActivity.class).putExtra(MOVIE_ID,
                        movieIds[getAdapterPosition()]);
                startActivity(downloadIntent);
            }
        }
    }
}
