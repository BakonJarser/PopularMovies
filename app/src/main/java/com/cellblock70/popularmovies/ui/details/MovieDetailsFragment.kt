package com.cellblock70.popularmovies.ui.details

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.databinding.FragmentMovieDetailsBinding
import timber.log.Timber

private const val baseBackdropURL = "https://image.tmdb.org/t/p/w780/"

class MovieDetailsFragment : Fragment() {

    lateinit var viewModel: MovieDetailsViewModel
    lateinit var binding: FragmentMovieDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val application = requireActivity().application
        binding = FragmentMovieDetailsBinding.inflate(inflater)
        val movieId = MovieDetailsFragmentArgs.fromBundle(requireArguments()).movieId
        viewModel = MovieDetailsViewModelFactory(movieId, application).create(MovieDetailsViewModel::class.java)
        viewModel.movie.observe(viewLifecycleOwner) {movie ->
            movie.let {
                binding.movie = it
                loadBackground(it)
            }
        }

        viewModel.reviews.observe(viewLifecycleOwner, { reviews ->
            reviews.let {
                binding.reviewListView.removeAllViews()
                binding.reviewListView.addView(binding.reviewLabelTextView)
                for (review in it) {
                    val author = review.author ?: "Anonymous"
                    val text = review.reviewText ?: "Missing review text"
                    binding.reviewListView.addView(getReviewLayout(author, text))
                }
            }
        })

        viewModel.trailers.observe(viewLifecycleOwner, { trailers ->
            binding.trailerListView.removeAllViews()
            binding.trailerListView.addView(binding.trailerLabelTextView)
            trailers.let {
                for (trailer in it) {
                    val link = trailer.link
                    link.let {
                        val name = trailer.name ?: "Unknown Title"
                        binding.trailerListView.addView(getTrailerButton(it!!, name))
                    }
                }
            }
        })
        viewModel.favoriteLiveData.observe(viewLifecycleOwner, {
            // if the db call returns an empty list then this is not a favorite
            binding.favoriteButton.isChecked = it.isNotEmpty()
        })
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun loadBackground(movie: Movie) {
        val backdropUrl =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) movie.backdropPath
            else movie.posterPath
        val height : Int
        val width : Int
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            // TODO test this with an older device
            val metrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getRealMetrics(metrics)
            height = metrics.heightPixels
            width = metrics.widthPixels
        } else {
            // TODO image size looks a little off, make adjustments
            val metrics = requireActivity().windowManager.currentWindowMetrics
            height = metrics.bounds.height()
            width = metrics.bounds.width()
            Timber.e("height: $height   width: $width")
        }
        Timber.e("Loading $baseBackdropURL$backdropUrl")
        Glide.with(requireActivity()).load(baseBackdropURL + backdropUrl)
            .override(width, height)
            .centerCrop().into(object : CustomTarget<Drawable>() {

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                binding.activityMovieDetailsScrollview.background = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                Timber.e("Failed to load image.  Load was canceled.")

            }
        })
    }

    private fun getReviewLayout(author: String, content: String): LinearLayout {
        @SuppressLint("InflateParams")
        val reviewLayout = layoutInflater.inflate(R.layout.movie_review, null) as LinearLayout
        reviewLayout.id = author.hashCode() + content.hashCode()
        (reviewLayout.findViewById<View>(R.id.reviewer_text_view) as TextView).text =
            author
        (reviewLayout.findViewById<View>(R.id.review_content_text_view) as TextView).text =
            content
        return reviewLayout
    }

    private fun getTrailerButton(key: String, name: String): Button {
        val button = layoutInflater.inflate(R.layout.trailer_button, null) as Button
        button.text = name
        button.id = key.hashCode() + name.hashCode()
        val uri = Uri.parse("https://youtube.com/").buildUpon()
            .appendPath("watch")
            .appendQueryParameter("v", key).build()
        button.setOnClickListener { v: View? ->
            Timber.i(uri.toString())
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        return button
    }
}