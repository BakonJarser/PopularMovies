package com.cellblock70.popularmovies.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cellblock70.popularmovies.MovieDetails
import com.cellblock70.popularmovies.MovieGrid
import com.cellblock70.popularmovies.MyApplication
import com.cellblock70.popularmovies.ui.details.MovieDetailsRootScreen
import com.cellblock70.popularmovies.ui.movielist.MovieGridRootScreen
import com.cellblock70.popularmovies.ui.theme.PopularMoviesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PopularMoviesTheme {
                val navController = rememberNavController()
                val selectedTab = remember { mutableStateOf("popular") }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MyNavigationBar(
                            onMenuItemClicked = { preferenceSelected ->
                                navController.navigate(MovieGrid(preferenceSelected ?: "popular"))
                                selectedTab.value = preferenceSelected ?: "popular"
                            },
                            selectedTab = selectedTab.value
                        )
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = MovieGrid("popular"),
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<MovieGrid> { backStackEntry ->
                            val movieGrid = backStackEntry.toRoute<MovieGrid>()
                            MovieGridRootScreen(
                                application = application as MyApplication,
                                onMovieClicked = { movieId ->
                                    navController.navigate(MovieDetails(movieId))
                                },
                                movieListType = movieGrid.movieListType
                            )
                        }
                        composable<MovieDetails> { backStackEntry ->
                            val movieDetails = backStackEntry.toRoute<MovieDetails>()
                            MovieDetailsRootScreen(
                                movieId = movieDetails.movieId,
                                application = application as MyApplication
                            )
                        }
                    }
                }
            }
        }
    }
}
