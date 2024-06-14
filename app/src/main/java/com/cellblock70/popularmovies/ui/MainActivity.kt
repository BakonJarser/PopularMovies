package com.cellblock70.popularmovies.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.cellblock70.popularmovies.MyApplication
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.getDatabase
import com.cellblock70.popularmovies.ui.details.MovieDetailsFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Setup the bottom navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setOnItemSelectedListener {
            lifecycleScope.launch {
                val prefToSet =
                    (application as MyApplication).movieListTypeMapKeyIsTitles[it.title]
                        ?: "popular"
                setMovieListPref(prefToSet)
                val navController = supportFragmentManager.primaryNavigationFragment?.findNavController()
                // Changing tabs while on the movie details tab will crash the app so navigate back to list
                if (navController?.currentDestination?.label.contentEquals(getString(R.string.movieDetailsFragment))) {
                    navController?.navigate(MovieDetailsFragmentDirections.actionMovieDetailsFragmentToMovieListFragment())
                }
                val repository = MovieRepository(getDatabase(application as MyApplication))
                val language = application.applicationContext.getString(R.string.language)
                repository.getMovies(prefToSet, 1, language)
            }

            true
        }
    }

    private fun setMovieListPref(prefToSet: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        Timber.e("Setting pref to $prefToSet")
        prefs.edit()
            .putString(getString(R.string.movie_list_type), prefToSet)
            .apply()
    }
}
