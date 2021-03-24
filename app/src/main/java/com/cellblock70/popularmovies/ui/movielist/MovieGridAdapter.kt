package com.cellblock70.popularmovies.ui.movielist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.databinding.MovieGridItemBinding

class MovieGridAdapter(private val listener: OnClickListener) : ListAdapter<Movie, MovieGridAdapter.MovieViewHolder>(DiffCallBack) {

    class MovieViewHolder(private var binding : MovieGridItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.movie = movie
            binding.executePendingBindings()
        }
    }

    companion object DiffCallBack : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            //TODO fix this
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(MovieGridItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.itemView.setOnClickListener  { listener.onClick(movie.id) }
        holder.bind(movie)

    }

    fun interface OnClickListener { fun onClick(movieId : Int) }
}

