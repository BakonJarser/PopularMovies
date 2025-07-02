package com.cellblock70.popularmovies.ui.movielist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.database.Movie


@Composable
fun MovieGridRootScreen(
    modifier: Modifier = Modifier,
    onMovieClicked: (Int) -> Unit,
    movieListType: String,
    language: String,
    viewModel: MovieViewModel = hiltViewModel(
        creationCallback = { movieViewModelFactory: MovieViewModel.MovieViewModelFactory ->
            movieViewModelFactory.create(movieListType, language)
        }
    )
) {
    val state = viewModel.movies.collectAsState()
    MovieGridScreen(modifier = modifier, state.value, onMovieClicked)
}

@Composable
fun MovieGridScreen(modifier: Modifier, state: List<Movie>, onMovieClicked: (Int) -> Unit) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier,
        verticalArrangement = Arrangement.Top
    ) {
        items(state.size) { index ->
            MovieGridItem(state[index], onMovieClicked)
        }
    }
}

@Composable
fun MovieGridItem(movie: Movie, onMovieClicked: (Int) -> Unit) {
    AsyncImage(
        model = stringResource(R.string.portrait_poster_path, movie.posterPath.orEmpty()),
        contentDescription = movie.title,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.clickable {
            onMovieClicked.invoke(movie.id)
        }
    )
}
