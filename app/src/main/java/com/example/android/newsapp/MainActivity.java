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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This app displays a {@link RecyclerView} of {@link Article}s retrieved from the Guardian API.
 * Title, contributor, section, date and thumbnail image are displayed for each item.
 * Clicking an item will open an implicit intent to view the article in a web browser.
 *
 * Icons courtesy of Material Icons: https://material.io/tools/icons/?style=baseline
 * Reference for Navigation Drawer: https://developer.android.com/training/implementing-navigation/nav-drawer
 */
public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<Article>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /** Log messages tag **/
    private static final String LOG_TAG = MainActivity.class.getName();

    /** URL Strings for news article data from the Guardian **/
    private static final String SCHEME = "https";
    private static final String AUTHORITY = "content.guardianapis.com";

    /** String that holds sections listed in Navigation Drawer, initialized to default data **/
    private static String navSection = "us-news";

    /** Adapter for the list of articles **/
    private ArticleAdapter mArticleAdapter;

    /** Static value for the article loader ID **/
    private static final int ARTICLE_LOADER_ID = 1;

    /** SwipeRefreshLayout for pulling top of screen to refresh data **/
    private SwipeRefreshLayout swipeRefreshLayout;

    /** Handles the Navigation Drawer **/
    private DrawerLayout drawerLayout;

    /** TextView and ImageView displayed when the list returns empty **/
    private TextView mEmptyState;
    private ImageView mEmptyStateImage;

    /** ProgressBar displayed while loading data **/
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Finds ToolBar and sets as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sets ActionBar hamburger icon and Up Button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);

        // Finds reference to {@link SwipeRefreshLayout} and {@link RecyclerView} in the layout.
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        RecyclerView articleRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Finds reference to Empty State Views.
        mEmptyState = (TextView) findViewById(R.id.empty_view);
        mEmptyStateImage = (ImageView) findViewById(R.id.empty_view_image);

        // Finds reference to {@link DrawerLayout} and {@link NavigationView} for Navigation Drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Sets default section (US News) as highlighted in Navigation Drawer upon startup.
        navigationView.getMenu().findItem(R.id.us_news).setChecked(true);

        // Sets an OnNavigationItemSelectedListener() to open news sections
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Sets item as selected to highlight
                        menuItem.setChecked(true);
                        // Closes drawer when item is clicked
                        drawerLayout.closeDrawers();
                        // Sets navSection as selected section key
                        switch (menuItem.getItemId()) {
                            case R.id.us_news:
                                navSection = "us-news";
                                break;
                            case R.id.world_news:
                                navSection = "world";
                                break;
                            case R.id.environment:
                                navSection = "environment";
                                break;
                            case R.id.sports:
                                navSection = "sport";
                                break;
                            case R.id.business:
                                navSection = "business";
                                break;
                            case R.id.tech:
                                navSection = "technology";
                                break;
                            case R.id.science:
                                navSection = "science";
                                break;
                            case R.id.film:
                                navSection = "film";
                                break;
                            case R.id.books:
                                navSection = "books";
                                break;
                            case R.id.music:
                                navSection = "music";
                                break;
                            case R.id.settings:
                                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                                break;
                        }
                        // Displays ProgressBar while loading data
                        mProgressBar.setVisibility(View.VISIBLE);
                        checkNetwork();
                        return true;
                    }
                });

        // Finds reference to LinearLayoutManager to handle RecyclerView.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        articleRecyclerView.setLayoutManager(linearLayoutManager);

        // Create a new adapter that takes input from {@link Article}.
        mArticleAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        // Set the adapter on the {@link RecyclerView} to populate user interface.
        articleRecyclerView.setAdapter(mArticleAdapter);

        // Finds reference to SharedPreferences and sets a listener for when changes are made by the user.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        // Calls method to handle refreshing data.
        swipeToRefresh();

        // Calls method to handle network connectivity.
        checkNetwork();
    }

    /**
     * Set an onRefreshListener() to refresh the data when the user swipes down on screen.
     * Reference: https://developer.android.com/training/swipe/respond-refresh-request
     */
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
     * Method that handles connecting to network and initializing the Loader.
     */
    private void checkNetwork() {
        // Finds reference to ConnectivityManager and checks network connectivity state.
        ConnectivityManager connectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Gets details on current active network.
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();

        // If there is a connection, retrieve data.
        if (networkInfo != null && networkInfo.isConnected()) {
            // Finds reference to the LoaderManager to interact with loader.
            LoaderManager loaderManager = getLoaderManager();

            if(mArticleAdapter.getItemCount() <= 0) {
                // Initialize the loader and pass this activity, which contains the LoaderCallbacks interface.
                loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
            } else {
                mArticleAdapter.clear();
                loaderManager.restartLoader(ARTICLE_LOADER_ID, null, this);
            }

        } else {
            // If there is no connection, hide ProgressBar.
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mProgressBar.setVisibility(View.GONE);

            // And display a no internet connection error message.
            mEmptyStateImage.setImageResource(R.drawable.baseline_wifi_off_black_48);
            mEmptyState.setText(R.string.no_internet);

            swipeToRefresh();
        }
    }

    /**
     * Handles listening to Preference changes and restarts Loader.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(getString(R.string.order_by_key)) ||
                key.equals(getString(R.string.page_size_key)) ||
                key.equals(getString(R.string.keyword_key))) {
            // Clears RecyclerView since a new query will be sent.
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

        String orderBy = sharedPreferences.getString(
                getString(R.string.order_by_key),
                getString(R.string.order_by_default));

        String pageSize = sharedPreferences.getString(
                getString(R.string.page_size_key),
                getString(R.string.page_size_default));

        String keyword = sharedPreferences.getString(
                getString(R.string.keyword_key),
                getString(R.string.keyword_default));

        // Builds the URI string to https://content.guardianapis.com
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(SCHEME);
        uriBuilder.authority(AUTHORITY);

        // Appends section path and query parameters
        if (!navSection.isEmpty()) {
            uriBuilder.appendPath(navSection);
        }
        uriBuilder.appendQueryParameter("page-size", pageSize);
        uriBuilder.appendQueryParameter("show-tags", "contributor");
        uriBuilder.appendQueryParameter("show-fields", "thumbnail");
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("q", keyword);
        uriBuilder.appendQueryParameter("api-key", "c47c39bd-f229-4b85-8a38-6c006753efc0");

        // Returns completed URI
        return new ArticleLoader(this, uriBuilder.toString());
    }

    /**
     * Called when {@link ArticleLoader} is finished fetching data.
     */
    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        // Hides ProgressBar when data is loaded.
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        // If there are no articles, displays an error message.
        mEmptyStateImage.setImageResource(R.drawable.baseline_error_outline_black_48);
        mEmptyState.setText(R.string.no_articles_found);

        swipeToRefresh();

        // Clears the adapter of previous data
        mArticleAdapter.clear();

        // If there is a list of {@link Article}s, add them to the adapter and update the RecyclerView.
        if (articles != null && !articles.isEmpty()) {
            mEmptyStateImage.setVisibility(View.GONE);
            mEmptyState.setVisibility(View.GONE);
            mArticleAdapter.addAll(articles);
        }
    }

    /**
     * Clears the adapter if the Loader resets, such as the user switches to a different app.
     */
    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        mArticleAdapter.clear();
    }

    /**
     * Handles opening the Navigation Drawer from the ToolBar icon.
     */
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