package com.cellblock70.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment {

    private ArrayAdapter<ImageView> mMovieAdapter;
    private List<String> posters = new ArrayList<>();
    private JSONArray movieArray;

    public MovieListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        mMovieAdapter = new ImageViewAdapter();

        final GridView movieGrid = (GridView) rootView.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(mMovieAdapter);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String movieDetailString = null;
                try {
                    movieDetailString = movieArray.getString(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent downloadIntent = new Intent(getActivity(),
                        MovieDetails.class).putExtra(Intent.EXTRA_TEXT, movieDetailString);
                startActivity(downloadIntent);
            }
        });

        AsyncTask<Void, Void, Void> task = new PopularMovieTask();
        task.execute();

        return rootView;
    }

    private class PopularMovieTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = PopularMovieTask.class.getSimpleName();

        private  final String BASE_URL = getString(R.string.movie_list_url);
        private final String PAGE = getString(R.string.page);
        private final String API_KEY = getString(R.string.api_key);
        private final String LANGUAGE = getString(R.string.language_param);
        private static final String TMDB_API_KEY = BuildConfig.TMDB_MAP_API_KEY;
        private final String language = getString(R.string.language);

        @Override
        protected Void doInBackground(Void... voids) {
            String jsonString = null;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                    (getContext());
            String movieListType = sharedPreferences.getString(getString(R.string.movie_list_type), "popular");

            try {
                Uri uri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(movieListType)
                        .appendQueryParameter(PAGE, Integer.toString(1))
                        .appendQueryParameter(LANGUAGE, language)
                        .appendQueryParameter(API_KEY, TMDB_API_KEY)
                        .build();

                URL url = new URL(uri.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Read the input stream into a String
                InputStream inputStream = connection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.e(LOG_TAG, "Null input stream");
                    return null;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    Log.e(LOG_TAG, "Buffer length is 0");
                    return null;
                }
                jsonString = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            try {
                JSONObject object = new JSONObject(jsonString);
                movieArray = object.getJSONArray(getString(R.string.results));

                for (int i = 0; i < movieArray.length(); i++){
                    JSONObject movie = (JSONObject) movieArray.get(i);
                    String posterPath = (String) movie.get(getString(R.string.poster_path));

                        posters.add(getString(R.string.base_image_url)
                                + getString(R.string.poster_size) + posterPath);
                    }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            mMovieAdapter.clear();
            for (int i = 0; i < posters.size(); i++) {
                ImageView imageView = new ImageView(getActivity());
                mMovieAdapter.add(imageView);
            }
        }
    }

    /**
     * An adapter for loading an image into an ImageView.
     */
    private class ImageViewAdapter extends ArrayAdapter<ImageView>{

        ImageViewAdapter() {
            super(getActivity(), R.layout.activity_movie_grid_item);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent){

            ImageView imageView;
            if (convertView == null){
                imageView = new ImageView(getContext());
            }
            else{
                imageView = (ImageView) convertView;
            }
            // Find the path to the poster in the posters list and load it into the ImageView.
            Picasso.with(getContext()).load(posters.get(position)).into(imageView);
            return imageView;
        }
    }
}
