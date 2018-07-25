# Tech News

A news app for articles about technology.

![Screenshot](screenshot.png)

* Built for API 15 or higher
* One activity: MainActivity
* Data parsed using JSON from the Guardian API found here: https://open-platform.theguardian.com/

## Basic structure:

A ListView displays a list of CardViews containing data about each article, such as title, contributor, section and date.
Selecting on a list item will send an implicit intent to open a web browser.
A Loader is used to fetch Article data. If there is no internet connection, a TextView will display "No internet connection."
If there is no data found, a TextView will display "No articles found."
A SwipeRefreshLayout is used so the user can swipe down to refresh the screen.