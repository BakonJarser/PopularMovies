package com.cellblock70.popularmovies.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.ui.theme.PopularMoviesTheme

@Composable
fun MyNavigationBar(onMenuItemClicked: (String?) -> Unit, selectedTab: String) {
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
        val prefValues = stringArrayResource(R.array.pref_movie_list_type_values)

        val items = listOf(
            NavigationItem(label = stringResource(R.string.popular), R.drawable.fire_icon, null, prefValues[0]),
            NavigationItem(label = stringResource(R.string.top_rated), R.drawable.satisfaction_icon, null, prefValues[1]),
            NavigationItem(label = stringResource(R.string.now_playing), R.drawable.video_play_roll_icon, null, prefValues[2]),
            NavigationItem(label = stringResource(R.string.upcoming), R.drawable.coming_soon_icon, null, prefValues[3]),
            NavigationItem(label = stringResource(R.string.favorites), null, Icons.Filled.Favorite, prefValues[4])
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    CustomNavigationBarIcon(
                        icon = item.iconRes,
                        iconImageVector = item.iconVector,
                        label = item.label
                    )
                },
                selected = item.value == selectedTab,
                onClick = {
                    onMenuItemClicked.invoke(item.value)
                },
                colors = navigationBarItemColors
            )
        }
    }
}

@Composable
private fun CustomNavigationBarIcon(
    @DrawableRes icon:  Int? = null,
    iconImageVector: ImageVector? = null,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            icon != null -> Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            iconImageVector != null -> Icon(
                imageVector = iconImageVector,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false
        )
    }
}

data class NavigationItem(
    val label: String,
    val iconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val value: String
)

@Preview(showBackground = true)
@Composable
fun MyNavigationBarPreview() {
    PopularMoviesTheme {
        MyNavigationBar(
            selectedTab = "popular",
            onMenuItemClicked = {}
        )
    }
}
