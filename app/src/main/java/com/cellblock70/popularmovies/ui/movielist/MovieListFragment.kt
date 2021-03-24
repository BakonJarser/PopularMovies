package com.cellblock70.popularmovies.ui.movielist

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cellblock70.popularmovies.R

class MovieListFragment : Fragment() {

    private lateinit var viewModel : MovieViewModel
    private lateinit var movieGridAdapter: MovieGridAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

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

        val factory = MovieViewModelFactory(requireActivity().application)
        viewModel = factory.create(MovieViewModel::class.java)
        viewModel.movies.observe(viewLifecycleOwner, { movieList ->
            movieGridAdapter.submitList(movieList)
        })
    }
}