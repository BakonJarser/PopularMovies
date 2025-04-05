package com.cellblock70.popularmovies.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cellblock70.popularmovies.MyApplication
import com.cellblock70.popularmovies.R

@Composable
fun MyNavigationBar(onMenuItemClicked: (String?) -> Unit, application: MyApplication) {
    val navigationBarItemColors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        selectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.tertiaryContainer,
        unselectedTextColor = MaterialTheme.colorScheme.tertiaryContainer,

        )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        val popularLabel = stringResource(R.string.popular)
        var selectedTab by remember { mutableStateOf(popularLabel) }
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(R.drawable.fire_icon),
                        contentDescription = popularLabel,
                        Modifier.size(24.dp)
                    )
                    Text(text = popularLabel, modifier = Modifier.padding(4.dp), fontSize = 12.sp)
                }
            },
            selected = selectedTab == popularLabel,
            onClick = {
                onMenuItemClicked.invoke((application).movieListTypeMapKeyIsTitles[popularLabel])
                selectedTab = popularLabel
            },
            colors = navigationBarItemColors
        )

        val topRatedLabel = stringResource(R.string.top_rated)
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(R.drawable.satisfaction_icon),
                        contentDescription = topRatedLabel,
                        Modifier.size(24.dp)
                    )
                    Text(text = topRatedLabel, modifier = Modifier.padding(4.dp), fontSize = 12.sp)
                }
            },
            selected = selectedTab == topRatedLabel,
            onClick = {
                onMenuItemClicked.invoke(application.movieListTypeMapKeyIsTitles[topRatedLabel])
                selectedTab = topRatedLabel
            },
            colors = navigationBarItemColors
        )

        val nowPlayingLabel = stringResource(R.string.now_playing)
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(R.drawable.video_play_roll_icon),
                        contentDescription = topRatedLabel,
                        Modifier.size(24.dp)
                    )
                    Text(text = nowPlayingLabel, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.padding(4.dp), fontSize = 12.sp)
                }
            },
            selected = selectedTab == nowPlayingLabel,
            onClick = {
                onMenuItemClicked.invoke(application.movieListTypeMapKeyIsTitles[nowPlayingLabel])
                selectedTab = nowPlayingLabel
            },
            colors = navigationBarItemColors
        )

        val comingSoonLabel = stringResource(R.string.upcoming)
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(R.drawable.coming_soon_icon),
                        contentDescription = topRatedLabel,
                        Modifier.size(24.dp)
                    )
                    Text(text = comingSoonLabel, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.padding(4.dp), fontSize = 12.sp)
                }
            },
            selected = selectedTab == comingSoonLabel,
            onClick = {
                onMenuItemClicked.invoke(application.movieListTypeMapKeyIsTitles[comingSoonLabel])
                selectedTab = comingSoonLabel
            },
            colors = navigationBarItemColors
        )

        val favoritesLabel = stringResource(R.string.favorites)
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = favoritesLabel,
                        Modifier.size(24.dp)
                    )
                    Text(text = favoritesLabel, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.padding(4.dp), fontSize = 12.sp)
                }
            },
            selected = selectedTab == favoritesLabel,
            onClick = {
                onMenuItemClicked.invoke(application.movieListTypeMapKeyIsTitles[favoritesLabel])
                selectedTab = favoritesLabel
            },
            colors = navigationBarItemColors
        )

    }
}