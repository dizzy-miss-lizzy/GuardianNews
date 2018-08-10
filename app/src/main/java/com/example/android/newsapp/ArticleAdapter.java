package com.example.android.newsapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * {@link ArticleAdapter} is an {@link ArrayAdapter} that provides the layout for the
 * list of articles based on the data source taken from the {@link Article} objects and
 * parsed in {@link QueryUtils}.
 * <p/>
 * Reference for {@link RecyclerView}: https://medium.com/@thebaileybrew/utilizing-androids-most-underrated-tool-recyclerview-viewholder-72f008627d89
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    /** String containing the split position of Date **/
    private static final String DATE_SEPARATOR = "T";

    /** Layout inflated when data is loaded **/
    private LayoutInflater layoutInflater;

    /** ArrayList containing {@link Article} objects **/
    private ArrayList<Article> articles;

    /**
     * Class containing ArrayList Views.
     */
    class ArticleViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView contributor;
        private TextView section;
        private TextView date;
        private ImageView thumbnail;

        private ArticleViewHolder(View itemView) {
            super(itemView);
            // Finds title, contributor, section and date TextViews and thumbnail ImageView.
            title = (TextView) itemView.findViewById(R.id.title);
            contributor = (TextView) itemView.findViewById(R.id.contributor);
            section = (TextView) itemView.findViewById(R.id.section);
            date = (TextView) itemView.findViewById(R.id.date);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail_image);
        }
    }

    /**
     * {@link ArticleAdapter} constructor.
     */
    public ArticleAdapter(Activity context, ArrayList<Article> articles) {
        this.layoutInflater = LayoutInflater.from(context);
        this.articles = articles;
    }

    /**
     * Inflates the layout with {@link Article} objects.
     */
    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new ArticleViewHolder(view);
    }

    /**
     * Handles finding the {@link Article} position and setting text and bitmap.
     */
    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        // Get the {@link Article} object located at the current position.
        final Article currentArticle = articles.get(position);

        // Sets title to current Article object.
        holder.title.setText(currentArticle.getTitle());

        // If a contributor is not available, sets the TextView visibility to GONE.
        // Else, sets contributor to current Article object.
        String contributorText = currentArticle.getContributor();
        if (contributorText == null) {
            holder.contributor.setVisibility(View.GONE);
        } else {
            holder.contributor.setText(contributorText);
        }

        // Sets section to current Article object.
        holder.section.setText(currentArticle.getSection());

        String originalDate = currentArticle.getDate();
        String date = null;
        // If date contains a "T", split String here and assign first part to date.
        if (originalDate.contains(DATE_SEPARATOR)) {
            String[] parts = originalDate.split(DATE_SEPARATOR);
            date = parts[0];
        }

        // Converts the date to format of "MM-dd-yyyy".
        // Reference: https://stackoverflow.com/questions/35939337/how-to-convert-date-to-a-particular-format-in-android/35939543#35939543
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date newDate = null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        date = spf.format(newDate);

        // Sets text of the date TextView.
        holder.date.setText(date);

        // Gets Thumbnail and sets bitmap to current Article object.
        Bitmap articleImage = currentArticle.getThumbnail();
        holder.thumbnail.setImageBitmap(articleImage);

        // Set an OnClickListener() on the RecyclerView, which sends an implicit intent
        // to a web browser and opens the selected article.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Convert the URL String into a URI object.
                Uri articleUri = Uri.parse(currentArticle.getUrl());

                // Create an intent to view the article URI.
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);

                // Gets PackageManager to query activities that can handle the intent.
                // Reference: https://developer.android.com/training/basics/intents/sending
                PackageManager packageManager = v.getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(websiteIntent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                // If there is an activity, start intent and send to web browser.
                if (isIntentSafe) {
                    v.getContext().startActivity(websiteIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    /**
     * Method used in {@link MainActivity} to clear the adapter of {@link Article} objects.
     * Reference: https://github.com/alejandra-gonzalez/LatestInTechNews/blob/master/app/src/main/java/com/example/android/latestintechnews/ArticleAdapter.java
     */
    public void clear() {
        int size = articles.size();
        if (size > 0) {
            articles.removeAll(articles);
        }
        notifyDataSetChanged();
    }

    /**
     * Method used in {@link MainActivity} to add {@link Article} objects to adapter.
     * End reference.
     */
    public void addAll(List<Article> articleList) {
        articles.addAll(articleList);
    }
}
