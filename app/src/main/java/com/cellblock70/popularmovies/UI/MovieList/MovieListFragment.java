package com.cellblock70.popularmovies.UI.MovieList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cellblock70.popularmovies.R;
import com.cellblock70.popularmovies.UI.MainActivity;
import com.cellblock70.popularmovies.data.database.CompleteMovie;
import com.cellblock70.popularmovies.data.database.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieListFragment extends Fragment {

    public static final String POSTERS = "posters";
    public static final String MOVIE_IDS = "movieIds";
    public static final String MOVIE_ID = "movie_id";
    private ImageViewAdapter mMovieAdapter;
    private ArrayList<String> posters = new ArrayList<>();
    private int[] movieIds;
    private String[] movieTitles;
    private MovieViewModel movieViewModel;
    SharedPreferences mSharedPreferences;
    String movieListType = "popular";

    public MovieListFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieViewModel = new ViewModelProvider(requireActivity()).get(MovieViewModel.class);
        RecyclerView movieGrid = view.getRootView().findViewById(R.id.movie_grid);
        // set more columns if the device is in landscape
        int columns = getResources().getConfiguration().orientation == OrientationHelper
                .VERTICAL ? 2 : 4;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columns,
                RecyclerView.VERTICAL, false);
        movieGrid.setLayoutManager(layoutManager);
        movieGrid.setHasFixedSize(true);
        mMovieAdapter = new ImageViewAdapter(getContext());
        movieGrid.setAdapter(mMovieAdapter);

        if (savedInstanceState != null) {
            posters = savedInstanceState.getStringArrayList(POSTERS);
            movieIds = savedInstanceState.getIntArray(MOVIE_IDS);
        } else {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            loadMovieList();
        }
    }

    private void loadMovieList() {
        movieListType = mSharedPreferences.getString(getString(R.string.movie_list_type), "popular");
        if (getString(R.string.favorites).equals(movieListType)) {
            getFavoritesFromDatabase();
        } else {
            LiveData<List<Movie>> movieData = movieViewModel.getMovies(movieListType);
            movieData.observe(getViewLifecycleOwner(), movies -> {
                movieIds = new int[movies.size()];
                movieTitles = new String[movies.size()];
                int index = 0;
                for (Movie movie : movies) {
                    posters.add(movie.getPosterPath());
                    movieTitles[index] = movie.getTitle();
                    movieIds[index++] = movie.getId();
                }
                mMovieAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Retrieves the movie poster paths for the user's favorites from the database and loads
     * them into the adapter view.
     */
    private void getFavoritesFromDatabase() {
        // TODO Get and store images in the database instead of loading them every time.

        LiveData<List<CompleteMovie>> favoriteData = movieViewModel.getFavorites();
        favoriteData.observe(getViewLifecycleOwner(), movies -> {
            movieIds = new int[movies.size()];
            movieTitles = new String[movies.size()];
            int index = 0;
            for (CompleteMovie movie : movies) {
                posters.add(movie.getMovie().getPosterPath());
                movieTitles[index] = movie.getMovie().getTitle();
                movieIds[index++] = movie.getMovie().getId();
            }
            mMovieAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(MOVIE_IDS, movieIds);
        outState.putStringArrayList(POSTERS, posters);
    }

    /**
     * An adapter for loading an image into an ImageView.
     */
    private class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.PosterViewHolder> {

        private final Context mContext;

        ImageViewAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout
                    .movie_grid_item, parent, false);
            view.setFocusable(true);
            return new PosterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
            ImageView posterView = holder.posterView;
            posterView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mContext).load(posters.get(position)).into(posterView);
            posterView.setContentDescription(movieTitles[position]);
        }

        @Override
        public int getItemCount() {
            return posters.size();
        }

        class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView posterView;

            PosterViewHolder(View itemView) {
                super(itemView);

                posterView = itemView.findViewById(R.id.activity_movie_grid_item);
                posterView.setScaleType(ImageView.ScaleType.FIT_XY);
                posterView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showMovieDetails(movieIds[getAdapterPosition()]);
            }
        }
    }
}
