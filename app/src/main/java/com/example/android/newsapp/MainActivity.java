package com.example.android.newsapp;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This app displays a {@link RecyclerView} of {@link Article}s retrieved from the Guardian API.
 * Title, contributor, section and date are displayed for each item.
 * Clicking an item will open an implicit intent to view the article in a web browser.
 * <p/>
 * Empty state icons courtesy of Material Icons: https://material.io/tools/icons/?style=baseline
 *
 * Reference for Navigation Drawer: https://developer.android.com/training/implementing-navigation/nav-drawer
 */
public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Article>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /** Log messages tag **/
    private static final String LOG_TAG = MainActivity.class.getName();

    /** URL for news article data from the Guardian dataset **/
    private static final String GUARDIAN_URL =
            "https://content.guardianapis.com/search?api-key=c47c39bd-f229-4b85-8a38-6c006753efc0";

    /** Adapter for the list of articles **/
    private ArticleAdapter mArticleAdapter;

    /** Static value for the article loader ID **/
    private static final int ARTICLE_LOADER_ID = 1;

    /** TextView and ImageView displayed when the list returns empty **/
    private TextView mEmptyState;
    private ImageView mEmptyStateImage;

    /** ProgressBar that is displayed upon start of app **/
    private ProgressBar mProgressBar;

    /** SwipeRefreshLayout for pulling top of screen to refresh data **/
    private SwipeRefreshLayout swipeRefreshLayout;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        switch(menuItem.getItemId()) {
                            case R.id.science:
                                // Fill with code to pull new query
                                break;
                            case R.id.settings:
                                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                                break;
                        }

                        return true;
                    }
                });

        // Find a reference to the {@link SwipeRefreshLayout} and {@link RecyclerView} in the layout.
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        RecyclerView articleRecyclerView = (RecyclerView) findViewById(R.id.list_view);

        // Create a new adapter that takes input from {@link
        mArticleAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        articleRecyclerView.setLayoutManager(linearLayoutManager);

        // Set the adapter on the {@link ListView} to populate user interface.
        articleRecyclerView.setAdapter(mArticleAdapter);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.registerOnSharedPreferenceChangeListener(this);

        swipeToRefresh();

        // Displays a blank screen when app is started.
        mEmptyState = (TextView) findViewById(R.id.empty_view);

        // Calls method that handles network connectivity.
        checkNetwork();
    }

    // Set an onRefreshListener(), which refreshes the data when the user
    // pulls from the top of the screen.
    private void swipeToRefresh() {
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
            mEmptyStateImage = (ImageView) findViewById(R.id.empty_view_image);
            mEmptyStateImage.setImageResource(R.drawable.baseline_wifi_off_black_48);
            mEmptyState.setText(R.string.no_internet);

            swipeToRefresh();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(getString(R.string.section_key)) ||
                key.equals(getString(R.string.order_by_key)) ||
                key.equals(getString(R.string.page_size_key)) ||
                key.equals(getString(R.string.keyword_key))) {
            // Clear the RecyclerView since a new query will be sent
            mArticleAdapter.clear();

            mEmptyStateImage.setVisibility(View.GONE);
            mEmptyState.setVisibility(View.GONE);

            mProgressBar.setVisibility(View.VISIBLE);

            getLoaderManager().restartLoader(ARTICLE_LOADER_ID, null, this);
        }
    }

    /**
     * Handles creating the Loader and passes in the Guardian URL to {@link ArticleLoader}.
     */
    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String section = sharedPreferences.getString(
                getString(R.string.section_key),
                getString(R.string.section_default));

        String orderBy = sharedPreferences.getString(
                getString(R.string.order_by_key),
                getString(R.string.order_by_default));

        String pageSize = sharedPreferences.getString(
                getString(R.string.page_size_key),
                getString(R.string.page_size_default));

        String keyword = sharedPreferences.getString(
                getString(R.string.keyword_key),
                getString(R.string.keyword_default));

        Uri baseUri = Uri.parse(GUARDIAN_URL);

        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("section", section);
        uriBuilder.appendQueryParameter("page-size", pageSize);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-fields", "thumbnail");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("q", keyword);

        return new ArticleLoader(this, uriBuilder.toString());
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
        mEmptyStateImage = (ImageView) findViewById(R.id.empty_view_image);
        mEmptyStateImage.setImageResource(R.drawable.baseline_error_outline_black_48);
        mEmptyState.setText(R.string.no_articles_found);

        swipeToRefresh();

        // Clears the adapter of previous data
        mArticleAdapter.clear();

        // If there is a valid list of {@link Article}s, add them to the adapter
        // and update the ListView.
        if (articles != null && !articles.isEmpty()) {
            mEmptyStateImage.setVisibility(View.GONE);
            mEmptyState.setVisibility(View.GONE);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}