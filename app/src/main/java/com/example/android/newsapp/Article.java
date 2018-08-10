package com.example.android.newsapp;

import android.graphics.Bitmap;

/**
 * {@link Article} represents the information about an article and provides the get() methods for the data.
 */
public class Article {

    /** Title of article **/
    private String mTitle;

    /** Contributor of article **/
    private String mContributor;

    /** Section of article **/
    private String mSection;

    /** Date of article **/
    private String mDate;

    /** URL of article **/
    private String mUrl;

    /** Thumbnail image of article **/
    private Bitmap mThumbnail;

    /**
     * Create an Article object
     *
     * @param title of the article
     * @param contributor of the article, displaying first and last name
     * @param section of the article, i.e. Technology
     * @param date of article, displayed as mm-dd-yyyy
     * @param url of article used in an implicit intent
     * @param thumbnail of article that displays image
     */
    public Article(String title, String contributor, String section, String date, String url, Bitmap thumbnail) {
        mTitle = title;
        mContributor = contributor;
        mSection = section;
        mDate = date;
        mUrl = url;
        mThumbnail = thumbnail;
    }

    /** Get the Title of the object **/
    public String getTitle() {
        return mTitle;
    }

    /** Get the Contributor of the object **/
    public String getContributor() {
        return mContributor;
    }

    /** Get the Section of the object **/
    public String getSection() {
        return mSection;
    }

    /** Get the Date of the object **/
    public String getDate() {
        return mDate;
    }

    /** Get the URL of the object **/
    public String getUrl() {
        return mUrl;
    }

    /** Get the Thumbnail of the object **/
    public Bitmap getThumbnail() {
        return mThumbnail;
    }
}
