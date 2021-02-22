package com.cellblock70.popularmovies.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.cellblock70.popularmovies.R;
import com.cellblock70.popularmovies.UI.Details.MovieDetailsActivity;
import com.cellblock70.popularmovies.UI.MovieList.MovieListFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private Menu mOptionsMenu;
    private MovieListFragment movieListFragment;
    private Map<String, String> movieListTypeMapKeyIsValues;
    private Map<String, String> movieListTypeMapKeyIsTitles;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showMovieList();

        if (movieListTypeMapKeyIsValues == null) {
            String[] pref_titles = getResources().getStringArray(R.array.pref_movie_list_type_titles);
            String[] pref_values = getResources().getStringArray(R.array.pref_movie_list_type_values);
            movieListTypeMapKeyIsValues = new HashMap<>(pref_titles.length);
            movieListTypeMapKeyIsTitles = new HashMap<>(pref_values.length);
            for (int i = 0; i < pref_titles.length; i++) {
                movieListTypeMapKeyIsValues.put(pref_values[i], pref_titles[i]);
                movieListTypeMapKeyIsTitles.put(pref_titles[i], pref_values[i]);
            }
        }
    }

    private void showMovieList() {

        if (movieListFragment == null) {
            movieListFragment = new MovieListFragment();
        }

        if (!movieListFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(R.id.fragment, movieListFragment, "LIST")
                    .addToBackStack("LIST")
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                    .show(movieListFragment)
                    .addToBackStack("LIST")
                    .commit();
        }
    }

    public void reloadMovieList() {
        getSupportFragmentManager().beginTransaction().remove(movieListFragment).commitNow();
        movieListFragment = new MovieListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment, MovieListFragment.class, null, "LIST")
                .commit();
    }

    public void showMovieDetails(int movieId) {

        Intent downloadIntent = new Intent(MainActivity.this,
                MovieDetailsActivity.class).putExtra(MovieListFragment.MOVIE_ID,
                movieId);
        startActivity(downloadIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        mOptionsMenu = menu;
        setMenuPref();
        return true;
    }

    private void setMenuPref() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedPref = prefs.getString(getString(R.string.movie_list_type), "popular");

        String selectedPrefTitle = movieListTypeMapKeyIsValues.get(selectedPref);
        for (int i = 0; i < mOptionsMenu.size(); i++) {
            if (mOptionsMenu.getItem(i).getTitle().equals(selectedPrefTitle)) {
                mOptionsMenu.getItem(i).setChecked(true);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int id = item.getItemId();
        boolean changeMovieList = false;

        if (id == R.id.popular) {
            prefs.edit().putString(getString(R.string.movie_list_type),
                    movieListTypeMapKeyIsTitles.get(getString(R.string.popular))).apply();
            mOptionsMenu.findItem(R.id.popular).setChecked(true);
            changeMovieList = true;
        } else if (id == R.id.top_rated) {
            prefs.edit().putString(getString(R.string.movie_list_type),
                    movieListTypeMapKeyIsTitles.get(getString(R.string.top_rated))).apply();
            mOptionsMenu.findItem(R.id.top_rated).setChecked(true);
            changeMovieList = true;
        } else if (id == R.id.upcoming) {
            prefs.edit().putString(getString(R.string.movie_list_type),
                    movieListTypeMapKeyIsTitles.get(getString(R.string.upcoming))).apply();
            mOptionsMenu.findItem(R.id.upcoming).setChecked(true);
            changeMovieList = true;
        } else if (id == R.id.now_playing) {
            prefs.edit().putString(getString(R.string.movie_list_type),
                    movieListTypeMapKeyIsTitles.get(getString(R.string.now_playing))).apply();
            mOptionsMenu.findItem(R.id.now_playing).setChecked(true);
            changeMovieList = true;
        } else if (id == R.id.favorites) {
            prefs.edit().putString(getString(R.string.movie_list_type),
                    movieListTypeMapKeyIsTitles.get(getString(R.string.favorites))).apply();
            mOptionsMenu.findItem(R.id.favorites).setChecked(true);
            changeMovieList = true;
        }

        if (changeMovieList) {
            reloadMovieList();
        }

        return super.onOptionsItemSelected(item);
    }
}
