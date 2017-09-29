package com.twitter.client;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.twitter.client.adapters.TweetAdapter;
import com.twitter.client.fragments.ComposeDialogFragment;
import com.twitter.client.listeners.EndlessRecyclerViewScrollListener;
import com.twitter.client.network.response.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TweetListActivity extends AppCompatActivity implements TweetAdapter.TweetItemClickListener, ComposeDialogFragment.TweetPostCompletionListener {
    public static String TAG = TweetListActivity.class.getSimpleName();

    private Handler handler;
    private TweetAdapter tweetAdapter;
    private RecyclerView tweetRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.ic_tweety_toolbar);
        setSupportActionBar(toolbar);

        handler = new Handler();

        tweetRecyclerView = (RecyclerView) findViewById(R.id.tweets_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        tweetRecyclerView.setLayoutManager(layoutManager);
        loadTweets();

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Refer below link to understand how to fetch next page of tweets
                // https://developer.twitter.com/en/docs/tweets/timelines/guides/working-with-timelines
                makeDelayedNextPageRequests(TweetApplication.getCurrMinTweetId() - 1);
            }
        };
        // Adds the scroll listener to RecyclerView
        tweetRecyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_compose) {
            showComposeDialogFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTweetItemClickListener(View view, Tweet selectedArticle) {

    }

    private void loadTweets() {
        TweetApplication.getTweetRestClient().getHomeTimelineTweets(null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "loadTweets:: Response received successfully.");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "loadTweets:: Response received successfully.");
                setupAdapter(Tweet.parseTweetListFromJson(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "loadTweets:: Failed to receive api response: " + responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "loadTweets:: Failed to receive api response: " + errorResponse, throwable);
            }
        });
    }

    private void makeDelayedNextPageRequests(final long maxId) {
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                RequestParams params = new RequestParams();
                params.put("max_id", maxId);
                TweetApplication.getTweetRestClient().getHomeTimelineTweets(params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, "loadTweets:: Response received successfully.");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.d(TAG, "loadTweets:: Response received successfully.");
                        insertNewPageData(Tweet.parseTweetListFromJson(response));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e(TAG, "loadTweets:: Failed to receive api response: " + responseString, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, "loadTweets:: Failed to receive api response: " + errorResponse, throwable);
                    }
                });
            }
        };
        // Run the above code block on the main thread after 2 seconds
        handler.postDelayed(runnableCode, 2000);
    }

    private void setupAdapter(List<Tweet> tweets) {
        tweetAdapter = new TweetAdapter(tweets, this);
        tweetRecyclerView.setAdapter(tweetAdapter);
    }

    private void insertNewPageData(final List<Tweet> tweets) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tweetAdapter.setMoreData(tweets);
                tweetAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showComposeDialogFragment() {
        ComposeDialogFragment dialogFragment = ComposeDialogFragment.newInstance("Compose new tweet", this);
        dialogFragment.show(getFragmentManager(), "Compose");
    }

    @Override
    public void onPostCompleted(Tweet createdTweet) {
        tweetAdapter.addNewItem(createdTweet);
        tweetAdapter.notifyDataSetChanged();
        tweetRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onPostFailure(Throwable throwable) {
        Log.e(TAG, "onPostFailure:: Failed to post tweet: " + throwable.getMessage(), throwable);
        Toast.makeText(this, "Failed to post tweet. Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
    }
}
