package com.cellblock70.popularmovies

import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

private const val imageUrl = "image.tmdb.org/t/p/w500"


@BindingAdapter("imageUrl")
fun bindImage(imageView: ImageView, url: String?) {
    url?.let {
        val fullUrl = imageUrl + url
        val imageUri = fullUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imageView.context)
            .load(imageUri)
            .into(imageView)
    }
}

@BindingAdapter("rating", "voteCount")
fun bindText(textView: TextView, rating: Double, numRatings: Int) {
    val text = textView.context.getString(R.string.rating_formatter, rating, numRatings)
    textView.text = text
}