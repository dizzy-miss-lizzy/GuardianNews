package com.example.android.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for requesting, receiving and parsing article data from The Guardian API.
 */
public final class QueryUtils {

    /** Log messages tag **/
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * A private constructor for {@link QueryUtils} because variables and methods are static
     * and can only be accessed from the QueryUtils class name.
     */
    private QueryUtils() {
    }

    /**
     * Query the Guardian dataset and return the {@link Article} objects.
     */
    public static List<Article> fetchArticleData(String requestUrl) {
        // Creates URL object
        URL url = createUrl(requestUrl);

        // Calls the makeHTTPRequest() method and receives a JSON response
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        // Calls the JSON parsing method and creates List of {@ Article}s.
        List<Article> articles = extractFeatureFromJson(jsonResponse);

        return articles;
    }

    /**
     * Returns new URL object from the URL string.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Problem building the URL ", exception);
            return null;
        }
        return url;
    }

    /**
     * Makes an HTTP request to the URL and returns the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, return the response early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        // Opens a connection to get data.
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            // If the request is successful, InputStream is called and the response is parsed.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the article JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} to a String containing the JSON response.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Returns a list of {@link Article} objects built from parsing the JSON response.
     */
    private static List<Article> extractFeatureFromJson(String articleJSON) {
        // Returns early if the JSON string is empty.
        if (TextUtils.isEmpty(articleJSON)) {
            return null;
        }

        // Creates an empty ArrayList that will contain article data.
        List<Article> articles = new ArrayList<>();

        // Try to parse the JSON response. If there is a problem with the JSON formatting,
        // a JSONException exception object is thrown.
        // Catch the exception and print an error message to the logs.
        try {
            // Create a JSONObject from the JSON response string.
            JSONObject baseJsonResponse = new JSONObject(articleJSON);

            // Extract the JSONObject associated with key "response".
            JSONObject articleObject = baseJsonResponse.getJSONObject("response");

            // Extract the JSONArray associated with key "results", which represents
            // a list of information about articles.
            JSONArray results = articleObject.getJSONArray("results");

            // For each article in the results JSONArray, create an {@link Article} object.
            for (int i = 0; i < results.length(); i++) {
                // Get an article at position i from the list of articles
                JSONObject currentArticle = results.getJSONObject(i);

                // Extract the values for the keys "webTitle", "sectionName", "webPublicationDate", and "webUrl".
                String title = currentArticle.getString("webTitle");
                String section = currentArticle.getString("sectionName");
                String date = currentArticle.getString("webPublicationDate");
                String url = currentArticle.getString("webUrl");

                // Extract the JSONArray associated with key "tags", which represents
                // information about the contributor of the article.
                JSONArray tags = currentArticle.getJSONArray("tags");
                String contributor = "";

                if(tags.length() == 0) {
                    contributor = null;
                } else {
                    for (int x = 0; x < tags.length(); x++) {
                        // Get a contributor at position x from the list of tags.
                        JSONObject currentContributor = tags.getJSONObject(x);

                        // Extract the value for the key "webTitle", which represents
                        // the first and last name of the contributor.
                        contributor = currentContributor.getString("webTitle");
                    }
                }

                // Create a new {@link Article} object with the title, section, date, contributor, and url
                // from the JSON response.
                Article article = new Article(title, contributor, section, date, url);

                // Add the new {@link Article} object to the list of articles.
                articles.add(article);
            }
        } catch(JSONException e) {
            Log.e("QueryUtils", "Problem parsing the article JSON results.", e);
        }
        // Returns the list of articles.
        return articles;
    }
}
