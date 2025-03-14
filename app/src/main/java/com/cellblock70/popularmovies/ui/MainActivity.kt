package com.cellblock70.popularmovies.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            windowInsets
        }

        // Setup the bottom navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> { }
            windowInsets
        }
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
