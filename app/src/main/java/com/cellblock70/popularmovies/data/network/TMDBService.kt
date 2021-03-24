package com.cellblock70.popularmovies.data.network

import com.cellblock70.popularmovies.data.database.Movie
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val basePosterURL = "https://image.tmdb.org"
private const val baseBackdropURL = "https://image.tmdb.org/t/p/w780/"
private const val baseMovieListURL = "https://api.themoviedb.org/"

private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
private val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(baseMovieListURL)
    .client(httpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

val retrofitImage: Retrofit = Retrofit.Builder()
    .baseUrl(basePosterURL)
    .client(httpClient)
    .build()

interface TMDBService {

    @GET("/3/movie/{movieListType}")
    suspend fun getMovies(
        @Path("movieListType") movieListType : String,
        @Query("page") page : Int,
        @Query("language") language : String,
        @Query("api_key") api_key : String) : TmdbListQueryResults

    @GET("/3/movie/{movieId}")
    suspend fun getMovie(
        @Path("movieId") movieId : String,
        @Query("language") language : String,
        @Query("api_key") api_key : String) : Movie

    @GET("/3/movie/{movieId}/reviews")
    suspend fun getReviews(
        @Path("movieId") movieId : Int,
        @Query("language") language : String,
        @Query("api_key") api_key : String) : TmdbMovieReviewQueryResults

    @GET("/3/movie/{movieId}/videos")
    suspend fun getTrailers(
        @Path("movieId") movieId : Int,
        @Query("language") language : String,
        @Query("api_key") api_key : String) : TmdbMovieTrailerQueryResults

}

interface TMDBImageService {

    @GET("/t/p/w500/")
    suspend fun getPoster(
        @Path("posterPath") path : String
    )
}

object TMDBApi {
    val tmdbService : TMDBService by lazy { retrofit.create(TMDBService::class.java)}
    val tmdbImageService : TMDBImageService by lazy { retrofitImage.create(TMDBImageService::class.java) }
}