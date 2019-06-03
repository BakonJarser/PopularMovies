package com.cellblock70.popularmovies.data.network;

import android.content.Context;

import com.cellblock70.popularmovies.AppExecutors;

public class MovieNetworkDataSource {

    private static final String LOG_TAG = "MovieNetworkDataSource";

    private static MovieNetworkDataSource instance;
    private Context context;
    private AppExecutors appExecutors;

    private MovieNetworkDataSource(Context context, AppExecutors executors) {
        this.context = context;
        this.appExecutors = executors;
    }

    public static MovieNetworkDataSource getInstance(Context context, AppExecutors executors) {
        if (instance == null) {
            synchronized (MovieNetworkDataSource.class) {
                if (instance == null) {
                    instance = new MovieNetworkDataSource(context, executors);
                }
            }
        }
        return instance;
    }
}
