package com.example.android.newsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This app displays a {@link ListView} of {@link Article}s retrieved from the Guardian API.
 * Title, contributor, section and date are displayed for each item.
 * Clicking an item will open an implicit intent to view the article in a web browser.
 */
public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Article>> {

    /** Log messages tag **/
    private static final String LOG_TAG = MainActivity.class.getName();

    /** URL for news article data from the Guardian dataset **/
    private static final String GUARDIAN_URL =
            "https://content.guardianapis.com/search?section=technology&page-size=15&show-tags=contributor&api-key=c47c39bd-f229-4b85-8a38-6c006753efc0";

    /** Adapter for the list of articles **/
    private ArticleAdapter mArticleAdapter;

    /** Static value for the article loader ID **/
    private static final int ARTICLE_LOADER_ID = 1;

    /** TextView that is displayed when the list returns empty **/
    private TextView mEmptyState;

    /** ProgressBar that is displayed upon start of app **/
    private ProgressBar mProgressBar;

    /** SwipeRefreshLayout for pulling top of screen to refresh data **/
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link SwipeRefreshLayout} and {@link ListView} in the layout.
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        ListView articleListView = (ListView) findViewById(R.id.list_view);

        // Create a new adapter that takes input from {@link
        mArticleAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        // Set the adapter on the {@link ListView} to populate user interface.
        articleListView.setAdapter(mArticleAdapter);

        // Set an OnItemClickListener() on the ListView, which sends an implicit intent
        // to a web browser and opens the selected article.
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find the current article that was clicked.
                Article currentArticle = mArticleAdapter.getItem(position);

                // Convert the URL String into a URI object.
                Uri articleUri = Uri.parse(currentArticle.getUrl());

                // Create an intent to view the article URI.
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);

                // Send the intent to a web browser.
                startActivity(websiteIntent);
            }
        });

        // Set an onRefreshListener(), which refreshes the data when the user
        // pulls from the top of the screen.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Calls checkNetwork() and sets boolean to false when finished.
                swipeRefreshLayout.setRefreshing(true);
                checkNetwork();
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        // Displays a blank screen when app is started.
        mEmptyState = (TextView) findViewById(R.id.empty_view);
        articleListView.setEmptyView(mEmptyState);

        // Calls method that handles network connectivity.
        checkNetwork();
    }

    /**
     * Method that handles connecting to a network and initializing the Loader.
     */
    private void checkNetwork() {
        // Get a reference to the ConnectivityManager and checks the state of network connectivity.
        ConnectivityManager connectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default network.
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();

        // If there is a connection, retrieve data.
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader and pass this activity, which contains the LoaderCallbacks interface.
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            // If there is no connection, hide ProgressBar.
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mProgressBar.setVisibility(View.GONE);

            // And display the EmptyState View with a "No internet connection." TextView.
            mEmptyState.setText(R.string.no_internet);
        }
    }

    /**
     * Handles creating the Loader and passes in the Guardian URL to {@link ArticleLoader}.
     */
    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        return new ArticleLoader(this, GUARDIAN_URL);
    }

    /**
     * Called when {@link ArticleLoader} is finished fetching data.
     */
    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {

        // Hides ProgressBar when data is loaded
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        // Sets the EmptyState TextView to display "No articles found."
        mEmptyState.setText(R.string.no_articles_found);

        // Clears the adapter of previous data
        mArticleAdapter.clear();

        // If there is a valid list of {@link Article}s, add them to the adapter
        // and update the ListView.
        if (articles != null && !articles.isEmpty()) {
            mArticleAdapter.addAll(articles);
        }
}

    /**
     * Called to clear the adapter if the Loader resets, such as the user switches
     * to a different app.
     */
    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        Log.i(LOG_TAG, "onLoaderReset() called");
        mArticleAdapter.clear();
    }
}