package com.cellblock70.popularmovies.ui.movielist

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cellblock70.popularmovies.MyApplication
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.MovieRepository
import com.cellblock70.popularmovies.data.database.getDatabase
import kotlinx.coroutines.launch
import timber.log.Timber

class MovieListFragment : Fragment() {

    private val viewModel : MovieViewModel by viewModels()
    private lateinit var movieGridAdapter: MovieGridAdapter
    private lateinit var optionsMenu: Menu
    private lateinit var application: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = requireActivity().application as MyApplication
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_movie_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val columns = if (resources.configuration.orientation
            == Configuration.ORIENTATION_PORTRAIT) 2 else 4
        val movieGrid : RecyclerView = view.findViewById(R.id.movie_grid)
        movieGrid.layoutManager = GridLayoutManager(requireContext(), columns, RecyclerView.VERTICAL, false)
        movieGrid.setHasFixedSize(true)
        movieGridAdapter = MovieGridAdapter { movieId ->
            findNavController().navigate(
                MovieListFragmentDirections.actionMovieListFragmentToMovieDetailsFragment(movieId))
        }
        movieGrid.adapter = movieGridAdapter
    }

    override fun onStart() {
        super.onStart()

        viewModel.movies.observe(viewLifecycleOwner) { movieList ->
            movieGridAdapter.submitList(movieList)
        }

        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.setHomeButtonEnabled(false)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.main, menu)
        optionsMenu = menu
        setMenuPref()
    }

    private fun setMenuPref() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val selectedPref = prefs.getString(getString(R.string.movie_list_type), "popular")
        val selectedPrefTitle = application.movieListTypeMapKeyIsValues[selectedPref]
        Timber.e("Setting menu pref to $selectedPrefTitle")
        for (menuItem in optionsMenu.iterator()) {
            if (menuItem.title == selectedPrefTitle) {
                menuItem.isChecked = true
                break
            }
        }
    }

    @Deprecated("Deprecated in Java")
    // TODO use MenuProvider
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuText = item.title
        menuText?.let {

            val prefToSet = application.movieListTypeMapKeyIsTitles[menuText.toString()] ?: "popular"
            val isMovieListTypeChanged = when (item.itemId) {
                R.id.popular -> setMovieListPref(R.id.popular, prefToSet)
                R.id.top_rated -> setMovieListPref(R.id.top_rated, prefToSet)
                R.id.upcoming -> setMovieListPref(R.id.upcoming, prefToSet)
                R.id.now_playing -> setMovieListPref(R.id.now_playing, prefToSet)
                R.id.favorites -> setMovieListPref(R.id.favorites, prefToSet)

                else -> false
            }

            if (isMovieListTypeChanged) {
                val repository = MovieRepository(getDatabase(requireContext()))
                lifecycleScope.launch {
                    val language = application.applicationContext.getString(R.string.language)
                    repository.getMovies(prefToSet, 1, language)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setMovieListPref(menuId : Int, prefToSet: String) : Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        Timber.e("Setting pref to $prefToSet")
        prefs.edit().putString(getString(R.string.movie_list_type), prefToSet)
            .apply()
        optionsMenu.findItem(menuId).isChecked = true
        return true
    }
}