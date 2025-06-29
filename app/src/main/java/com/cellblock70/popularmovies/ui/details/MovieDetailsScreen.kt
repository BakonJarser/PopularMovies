package com.cellblock70.popularmovies.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.database.Movie
import com.cellblock70.popularmovies.data.database.MovieReview
import com.cellblock70.popularmovies.data.database.MovieTrailer

@Composable
fun MovieDetailsRootScreen(
    modifier: Modifier = Modifier,
    movieId: Int,
    viewModel: MovieDetailsViewModel = hiltViewModel(
        creationCallback = { factory: MovieDetailsViewModel.Factory ->
            factory.create(movieId)
        }
    )
) {
    val state = viewModel.state.collectAsState()

    MovieDetailsScreen(modifier, state.value, viewModel::onAction)
}

@Composable
fun MovieDetailsScreen(
    modifier: Modifier,
    value: MovieWithReviewsAndTrailers,
    action: (MovieDetailsAction) -> Unit
) {
    val textColor = Color.White
    val movie = value.movie ?: return (Unit)

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.Black), contentAlignment = Alignment.TopStart) {
        AsyncImage(
            // todo landscape poster
            // todo full screen pixel fold
            model = stringResource(R.string.portrait_poster_path, movie.posterPath.orEmpty()),
            contentDescription = stringResource(R.string.movie_poster_description, movie.title.orEmpty()),
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.TopCenter
        )
        LazyColumn(
            modifier = Modifier
                .background(Color.Black.copy(alpha = .6f))
                .fillMaxWidth()
        ) {
            //Title
            item {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = movie.title.orEmpty(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp).weight(1f)
                    )
                    IconButton(
                        modifier = Modifier.minimumInteractiveComponentSize(),
                        onClick = { action.invoke(MovieDetailsAction.OnFavoriteClicked(!value.isFavorite)) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.favorite_toggle_button_description),
                            tint = if (value.isFavorite) MaterialTheme.colorScheme.tertiaryContainer else Color.White
                        )
                    }
                }
            }
            //Original Title
            if (value.movie.originalTitle != value.movie.title) {
                item {
                    StyledTextPair(
                        modifier = modifier,
                        valueText = movie.originalTitle.orEmpty(),
                        titleText = stringResource(R.string.original_title)
                    )
                }
            }
            item {
                StyledTextPair(
                    modifier = modifier,
                    valueText = movie.releaseDate.orEmpty(),
                    titleText = stringResource(R.string.release_date)
                )
            }
            item {
                StyledTextPair(
                    modifier = modifier,
                    valueText = stringResource(R.string.rating_formatter, movie.rating ?: 0.0, movie.reviews ?: 0.0),
                    titleText = stringResource(R.string.user_rating)
                )
            }
            item {
                Text(
                    fontSize = 18.sp,
                    text = movie.synopsis.orEmpty(),
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            if (value.trailers?.isNotEmpty() == true) {
                item {
                    Text(
                        text = stringResource(R.string.trailers),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            items(value.trailers?.size ?: 0) { index ->
                val trailer = value.trailers?.get(index) ?: return@items
                HyperlinkText(
                    modifier = modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                    text = trailer.name.orEmpty(),
                    linkText = listOf(trailer.name.orEmpty()),
                    hyperlinks = listOf(stringResource(R.string.youtube_link, trailer.link.orEmpty())),
                    fontSize = 16.sp,
                    linkTextFontWeight = FontWeight.SemiBold
                )
            }
            if (value.reviews?.isNotEmpty() == true) {
                item {
                    Text(
                        text = stringResource(R.string.reviews),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            items(value.reviews?.size ?: 0) { index ->
                val review = value.reviews?.getOrElse(index) { return@items }
                Column {
                    StyledTextPair(modifier = modifier, titleText = stringResource(R.string.reviewer), valueText = review?.author.orEmpty())
                    Text(modifier = modifier.padding(horizontal = 20.dp, vertical = 4.dp), text = review?.reviewText.toString(), color = textColor)
                    if (index < ((value.reviews?.size?.minus(1)) ?: 0)) {
                        HorizontalDivider(modifier.padding(horizontal = 20.dp, vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HyperlinkText(
    modifier: Modifier = Modifier,
    text: String,
    linkText: List<String>,
    hyperlinks: List<String>,
    linkTextColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontFamily: FontFamily = FontFamily.Monospace
) {
    val uriHandler = LocalUriHandler.current

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        linkText.forEachIndexed { index, link ->
            val startIndex = text.indexOf(link, lastIndex)
            val endIndex = startIndex + link.length

            if (startIndex > lastIndex) append(text.substring(lastIndex, startIndex))

            val linkUrL = LinkAnnotation.Url(
                hyperlinks[index], TextLinkStyles(
                    SpanStyle(
                        color = linkTextColor,
                        fontSize = fontSize,
                        fontWeight = linkTextFontWeight,
                        textDecoration = linkTextDecoration,
                        fontFamily = fontFamily
                    )
                )
            ) {
                val url = (it as LinkAnnotation.Url).url
                uriHandler.openUri(url)
            }
            withLink(linkUrL) { append(link) }
            append(" ")
            lastIndex = endIndex + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
        addStyle(
            style = SpanStyle(
                fontSize = fontSize, fontFamily = fontFamily
            ), start = 0, end = text.length
        )
    }
    Text(text = annotatedString, modifier = modifier)
}

@Composable
fun StyledTextPair(modifier: Modifier, titleText: String, valueText: String) {
    Row(modifier.fillMaxWidth()) {
        Text(
            titleText,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = modifier
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .alignByBaseline(),
            fontWeight = FontWeight.Bold
        )
        Text(text = valueText, color = Color.White, modifier = modifier.alignByBaseline())
    }
}

@Preview(showBackground = true)
@Composable
fun MovieDetailsScreenPreview() {
    MovieDetailsScreen(
        Modifier, MovieWithReviewsAndTrailers(
            Movie(id=129, title="Spirited Away", originalTitle="千と千尋の神隠し", rating=8.538, reviews=16972, synopsis="A young girl, Chihiro, becomes trapped in a strange new world of spirits. When her parents undergo a mysterious transformation, she must call upon the courage she never knew she had to free her family.", releaseDate="2001-07-20", posterPath="/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg", backdropPath="/6oaL4DP75yABrd5EbC4H2zq5ghc.jpg"),
            listOf(MovieTrailer(id="65605c163679a10976465e74", name="Official Trailer [Subtitled]", link="GAp2_0JJskk", site="YouTube", type="Trailer", movieId=129), MovieTrailer(id="65697e6a71f09500feb7e679", name="Ghibli Fest 2023 Trailer", link="F-X_81GZ2Uo", site="YouTube", type="Trailer", movieId=129), MovieTrailer(id="633c313d175051008e17fb08", name="Studio Ghibli Fest 2022 Spot", link="oZzFsv22wqI", site="YouTube", type="Teaser", movieId=129), MovieTrailer(id="62eb43fb8566d2005a486b43", name="Lin Guides Chihiro Through The Bathhouse", link="W5szC4XgR1s", site="YouTube", type="Clip", movieId=129), MovieTrailer(id="630adc3ad051d9007ec7970b", name="this is one of the greatest scenes ever animated", link="jLPNkXglCwQ", site="YouTube", type="Behind the Scenes", movieId=129), MovieTrailer(id="592c5d16c3a36877bc0817da", name="Official Trailer", link="ByXuk9QqQkk", site="YouTube", type="Trailer", movieId=129)),
            listOf(MovieReview(id="5d17c91385702e001eb921db", reviewText="One of the great \"masters\" of the anime art. Somehow, if I would personally associate \"Akira\" to \"self-destruction\", then this anime would be the opposite :)", author="ZeBlah", movieId=129), MovieReview(id="67702ac55f1c4fa473612f7a", reviewText="I quite liked the look of the new house that \"Chihiro\" and her parents were to move to, but she isn't so keen, and being ten had the weight of the world on her shoulders. En route to meet the removal men, her dad takes a wrong turning and soon, together with her mum, end up at the end of a tunnel. What would you do? Go in of course! Luckily it\'s quite a short walk before they find themselves in a small village that\'s full of shops all laid out for a banquet. Mum and dad tuck in but the young girl just wants to get going. Ignored, she explores a little as they gorge themselves and that's when she encounters the young \"Haku\" who suggests that she might like to leave. Snag is - her parents have quite literally now gone a bit porcine and she's going to have to rely on her new friend if she is to rescue them from an impending visit to the abattoir. A job is her first task, and for that she needs a contract with the wart-nosed witch \"Yubaba\" - and so off we go on a series of marvellously creative escapades discovering some coal spirits; that her friend has a secret identity; her boss an huge great baby and the entire place serves as a sort of bathhouse for the enigmatic \"No Face\" whose agenda isn\'t clear to anyone, though we do know he has a rapacious appetite and can make his own gold! The detail of the animation is astonishing and coupled with the mysticism of ancient Japanese folk lore works really well at creating a characterful and engaging story. I just love the lithe dragons of Oriental mythology as opposed to the scaly, stodgy, European ones. The concept of identity underpins much of the narrative here, with not just \"Chihiro\" but pretty much everyone else coming to terms with flaws and strengths in their personalities requiring a degree of human spirit, trust and forgiveness if ever anyone is to thrive and if ever she and her folks are to get to that new house on the hill. It's two hours really goes fly by and I thoroughly enjoyed it.", author="CinemaSerf", movieId=129)),
            true
        )
    ) {}
}