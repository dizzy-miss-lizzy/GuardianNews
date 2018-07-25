package com.example.android.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Loads a list of articles using an AsyncTaskLoader, which performs the network request
 * to the URL.
 */
public class ArticleLoader extends AsyncTaskLoader<List<Article>> {

    /** Log messages tag **/
    private static final String LOG_TAG = ArticleLoader.class.getName();

    /** Query URL **/
    private String mQueryUrl;

    /** {@link ArticleLoader} constructor.
     *
     * @param context of the activity
     * @param queryUrl to load data
     */
    public ArticleLoader(Context context, String queryUrl) {
        super(context);
        mQueryUrl = queryUrl;
    }

    /**
     * Starts the loading of data in a background thread.
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This performs on a background thread. It fetches the Article data from {@link QueryUtils}
     * and returns the data.
     */
    @Override
    public List<Article> loadInBackground() {
        if (mQueryUrl == null) {
            return null;
        }
        // Calls the {@link QueryUtils} fetchArticleData() method to send network request,
        // parse its response and extract the data.
        List<Article> articles = QueryUtils.fetchArticleData(mQueryUrl);
        return articles;
    }
}
