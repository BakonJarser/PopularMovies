package com.cellblock70.popularmovies.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cellblock70.popularmovies.MovieDetails
import com.cellblock70.popularmovies.MovieGrid
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.TabPreferences
import com.cellblock70.popularmovies.ui.details.MovieDetailsRootScreen
import com.cellblock70.popularmovies.ui.movielist.MovieGridRootScreen
import com.cellblock70.popularmovies.ui.theme.PopularMoviesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PopularMoviesTheme {
                val navController = rememberNavController()
                val defaultTab = stringResource(R.string.popular)
                val selectedTab = rememberSaveable { mutableStateOf(defaultTab) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    TabPreferences.getSelectedTab(this@MainActivity).firstOrNull()?.let {
                            selectedTab.value = it
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MyNavigationBar(
                            onMenuItemClicked = { preferenceSelected ->
                                if(preferenceSelected != selectedTab.value) {
                                    val newSelection = preferenceSelected ?: selectedTab.value
                                    scope.launch(Dispatchers.IO) {
                                        TabPreferences.saveSelectedTab(
                                            this@MainActivity,
                                            newSelection
                                        )
                                    }
                                    navController.navigate(MovieGrid) {
                                        popUpTo(MovieGrid) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                    selectedTab.value = newSelection
                                }
                            },
                            selectedTab = selectedTab.value
                        )
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = MovieGrid,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<MovieGrid> { backStackEntry ->
                            MovieGridRootScreen(
                                onMovieClicked = { movieId ->
                                    navController.navigate(MovieDetails(movieId))
                                },
                                language = stringResource(R.string.language)
                            )
                        }
                        composable<MovieDetails> { backStackEntry ->
                            val movieDetails = backStackEntry.toRoute<MovieDetails>()
                            MovieDetailsRootScreen(
                                movieId = movieDetails.movieId,
                            )
                        }
                    }
                }
            }
        }
    }
}
