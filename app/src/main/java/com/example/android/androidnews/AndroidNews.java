package com.example.android.androidnews;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.logging.Handler;

public class AndroidNews extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = AndroidNews.class.getSimpleName();
    private ListView listView;
    private TextView empty_list_item;
    private static final String TAG = AndroidNews.class.getSimpleName();
    private String LIST_INSTANCE_STATE;
    private String mListInstanceState;
    private Parcelable p;
    private Handler mHandler;
    private SwipeRefreshLayout mswipeRefreshlayout;
    private String USGS_REQUEST_URL;
    private ArrayList<News> bList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //swipe layout
        mswipeRefreshlayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mswipeRefreshlayout.setOnRefreshListener(this);
        listView = (ListView) findViewById(R.id.listView2);
        empty_list_item = (TextView) findViewById(R.id.empty_list_item);
        if (savedInstanceState == null || !savedInstanceState.containsKey("books")) {
            bList = new ArrayList<News>();
        } else {
            bList = savedInstanceState.getParcelableArrayList("books");
            NewslistAdapter adapter = new NewslistAdapter(AndroidNews.this, bList);
            listView.setAdapter(adapter);
        }
        ConnectivityManager connectivity = (ConnectivityManager) AndroidNews.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = connectivity.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected()) {
            Toast.makeText(getApplicationContext(), "Good internet connection", Toast.LENGTH_SHORT).show();
            String USGS_REQUEST_URL = "http://content.guardianapis.com/search?q=politics&api-key=15e01aed-19a7-4f1d-b44b-669ac5f9667e&show-tags=contributor";
            Log.d(TAG, USGS_REQUEST_URL);
            NewsAsyncTask task = new NewsAsyncTask();
            task.execute(USGS_REQUEST_URL);
        } else if (nInfo == null) {
            Toast.makeText(getApplicationContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("books", bList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        Toast.makeText(getApplicationContext(), "Swipe to refresh ", Toast.LENGTH_SHORT).show();
        NewsAsyncTask task = new NewsAsyncTask();
        task.execute(USGS_REQUEST_URL);
        mswipeRefreshlayout.setRefreshing(false);
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with  the response.
     */
    private class NewsAsyncTask extends AsyncTask<String, Void, List<News>> {
        @Override
        protected List<News> doInBackground(String... urls) {
            // Create URL object
            URL url = createUrl(urls[0]);
            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            if (jsonResponse != null) {
                // Extract relevant fields from the JSON response and create an {@link Event} object
                List<News> newses = extractFeatureFromJson(jsonResponse);
                // Return the {@link Event} object as the result fo the {@link BookAsyncTask}
                return newses;
            }
            return null;
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = null;
            if (url == null) {
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                // Check for server related problems
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else if (urlConnection.getResponseCode() == 401) {
                    return jsonResponse;
                }
            } catch (IOException e) {
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
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

        private List<News> extractFeatureFromJson(String newsJSON) {
            String mytitle;
            String myauthor;
            String mydate;
            String myurl;
            if (TextUtils.isEmpty(newsJSON)) {
                return null;
            }
            bList = new ArrayList<>();
            try {
                // build up a list of News objects with the corresponding data.
                JSONObject baseJsonResponse = new JSONObject(newsJSON);
                JSONObject newsArray = baseJsonResponse.getJSONObject("response");
                JSONArray newsArray2 = newsArray.getJSONArray("results");
                for (int i = 0; i < newsArray2.length(); i++) {
                    JSONObject currentNews = newsArray2.getJSONObject(i);
                    mytitle = currentNews.getString("webTitle");
                    mydate = currentNews.getString("sectionName");
                    myurl = currentNews.getString("webUrl");
                    JSONArray forauthor = currentNews.getJSONArray("tags");
                    for (int j = 0; j < forauthor.length(); j++) {
                        JSONObject forauthor1 = forauthor.getJSONObject(j);
                        myauthor = forauthor1.getString("webTitle");
                        if (forauthor1 == null) {
                            myauthor = "No authors found";
                        }
                        Log.d(TAG, "author = " + myauthor);
                        Log.d(TAG, "title=" + mytitle);
                        Log.d(TAG, "date=" + mydate);
                        News news = new News(mytitle, myauthor, mydate, myurl);
                        bList.add(news);
                    }
                }
                return bList;
            } catch (JSONException e) {
                // If an error is thrown when executing any of the above statements in the "try" block,
                // catch the exception here, so the app doesn't crash. Print a log message
                // with the message from the exception.
                Log.e("QueryUtils", "Problem parsing the News JSON results", e);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<News> news) {
            super.onPostExecute(news);
            if (news != null) {
                Log.d(TAG, "size = " + news.size());
                final NewslistAdapter adapter = new NewslistAdapter(AndroidNews.this, news);
                listView.setAdapter(adapter);
                //   answerField.setText("");
                // On click of the items in the list
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        News currentnew1 = adapter.getItem(position);
                        // Convert the String URL into a URI object (to pass into the Intent constructor)
                        Uri NewsUri = Uri.parse(currentnew1.getUrl());
                        // Create a new intent to view the News URI
                        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, NewsUri);
                        // Send the intent to launch a new activity
                        startActivity(websiteIntent);
                    }
                });
            }
        }
    }
}





