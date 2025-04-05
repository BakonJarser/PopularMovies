package com.cellblock70.popularmovies.ui.details

sealed interface MovieDetailsAction {
    class OnFavoriteClicked(val isFavorite: Boolean) : MovieDetailsAction
}
