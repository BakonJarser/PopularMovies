package com.cellblock70.popularmovies.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.getDatabase
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var optionsMenu : Menu
    private val movieListTypeMapKeyIsTitles = HashMap<String, String>()
    private val movieListTypeMapKeyIsValues = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val prefTitles = resources.getStringArray(R.array.pref_movie_list_type_titles)
        val prefValues = resources.getStringArray(R.array.pref_movie_list_type_values)

        for (i in prefTitles.indices) {
            movieListTypeMapKeyIsTitles[prefTitles[i]] = prefValues[i]
            movieListTypeMapKeyIsValues[prefValues[i]] = prefTitles[i]
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (menu != null) {
            optionsMenu = menu
        }
        setMenuPref()
        return true
    }

    private fun setMenuPref() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedPref = prefs.getString(getString(R.string.movie_list_type), "popular")
        val selectedPrefTitle = movieListTypeMapKeyIsValues[selectedPref]
        Timber.e("Setting menu pref to $selectedPrefTitle")
        for (menuItem in optionsMenu.iterator()) {
            if (menuItem.title == selectedPrefTitle) {
                menuItem.isChecked = true
                break
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuText = item.title
        val prefToSet = movieListTypeMapKeyIsTitles[menuText.toString()] ?: "popular"
        val isMovieListTypeChanged = when (item.itemId) {
            R.id.popular -> setMovieListPref(R.id.popular, prefToSet)
            R.id.top_rated -> setMovieListPref(R.id.top_rated, prefToSet)
            R.id.upcoming -> setMovieListPref(R.id.upcoming, prefToSet)
            R.id.now_playing -> setMovieListPref(R.id.now_playing, prefToSet)
            R.id.favorites -> setMovieListPref(R.id.favorites, prefToSet)

            else -> false
        }

        if (isMovieListTypeChanged) {
            val repository = MovieRepository(getDatabase(this))
            lifecycleScope.launch {
                val language = application.applicationContext.getString(R.string.language)
                repository.getMovies(prefToSet, 1, language)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setMovieListPref(menuId : Int, prefToSet: String) : Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        Timber.e("Setting pref to $prefToSet")
        prefs.edit().putString(getString(R.string.movie_list_type), prefToSet)
            .apply()
        optionsMenu.findItem(menuId).isChecked = true
        return true
    }
}