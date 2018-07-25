package com.example.android.newsapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * {@link ArticleAdapter} is an {@link ArrayAdapter} that provides the layout for the
 * list of articles based on the data source taken from the {@link Article} objects and
 * parsed in {@link QueryUtils}.
 */
public class ArticleAdapter extends ArrayAdapter<Article> {

    /** String containing the split position of Date **/
    private static final String DATE_SEPARATOR = "T";

    public ArticleAdapter(Activity context, ArrayList<Article> articles) {
        super(context, 0, articles);
    }

    // Class to hold ArrayList Views.
    static class ArticleViewHolder {
        private TextView title;
        private TextView contributor;
        private TextView section;
        private TextView date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the {@link Article} object located at the current position.
        Article currentArticle = getItem(position);

        ArticleViewHolder holder;

        // Checks if the view is being reused, otherwise inflate.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ArticleViewHolder();
            // Finds title, contributor, section and date TextViews.
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.contributor = (TextView) convertView.findViewById(R.id.contributor);
            holder.section = (TextView) convertView.findViewById(R.id.section);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            convertView.setTag(holder);
        } else {
            holder = (ArticleViewHolder) convertView.getTag();
        }

        // Sets title to current Article object.
        holder.title.setText(currentArticle.getTitle());

        // If a contributor is not available, sets the TextView visibility to GONE.
        // Else, sets contributor to current Article object.
        String contributorText = currentArticle.getContributor();
        if(contributorText == null) {
            holder.contributor.setVisibility(View.GONE);
        } else {
            holder.contributor.setText(contributorText);
        }

        // Sets section to current Article object.
        holder.section.setText(currentArticle.getSection());

        String originalDate = currentArticle.getDate();
        String date = null;

        // If date contains a "T", split String here and assign first part to date.
        if(originalDate.contains(DATE_SEPARATOR)) {
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

        return convertView;
    }
}
