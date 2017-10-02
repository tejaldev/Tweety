package com.twitter.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.twitter.client.activities.TweetDetailActivity;
import com.twitter.client.adapters.TweetAdapter;
import com.twitter.client.adapters.TweetStatusActionHelper;
import com.twitter.client.fragments.ComposeDialogFragment;
import com.twitter.client.listeners.EndlessRecyclerViewScrollListener;
import com.twitter.client.network.response.models.Tweet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TweetListActivity extends AppCompatActivity implements TweetAdapter.TweetItemClickListener,
        ComposeDialogFragment.TweetPostCompletionListener, TweetStatusActionHelper.OnStatusUpdatedListener {
    public static String TAG = TweetListActivity.class.getSimpleName();
    public static final int TWEET_DETAIL_STATUS = 1;

    private Handler handler;
    private TweetAdapter tweetAdapter;
    private RecyclerView tweetRecyclerView;
    private SwipeRefreshLayout swipeRefreshTweetLayout;
    private TweetStatusActionHelper statusActionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.ic_tweety_toolbar);
        setSupportActionBar(toolbar);

        handler = new Handler();
        statusActionHelper = new TweetStatusActionHelper(this);

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

        swipeRefreshTweetLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_tweets);
        swipeRefreshTweetLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewTweets();
            }
        });

        // Support implicit intent
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                showComposeDialogFragment(intent);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showComposeDialogFragment(null);
            }
        });
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
//        if (id == R.id.action_compose) {
//            showComposeDialogFragment(null);
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTweetItemClickListener(View view, Tweet selectedTweet, int position) {
        showTweetDetailActivity(selectedTweet, position);
    }

    @Override
    public void onReplyClickListener(View view, Tweet selectedTweet, int position) {
        showTweetDetailActivity(selectedTweet, position);
    }

    @Override
    public void onRetweetClickListener(View view, Tweet selectedTweet, int position) {
        statusActionHelper.handleRetweetStatusAction(selectedTweet, position);
    }

    @Override
    public void onFavoriteClickListener(View view, Tweet selectedTweet, int position) {
        statusActionHelper.handleFavoritedStatusAction(selectedTweet, position);
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
                        Log.d(TAG, "loadMoreTweets:: Response received successfully.");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.d(TAG, "loadMoreTweets:: Response received successfully.");
                        insertNewPageData(Tweet.parseTweetListFromJson(response));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e(TAG, "loadMoreTweets:: Failed to receive api response: " + responseString, throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, "loadMoreTweets:: Failed to receive api response: " + errorResponse, throwable);
                    }
                });
            }
        };
        // Run the above code block on the main thread after 2 seconds
        handler.postDelayed(runnableCode, 2000);
    }

    /**
     * Fetches new tweets using rest client
     */
    private void fetchNewTweets() {
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                RequestParams params = new RequestParams();
                params.put("since_id", TweetApplication.getCurrMaxTweetId());
                TweetApplication.getTweetRestClient().getHomeTimelineTweets(params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.d(TAG, "fetchNewTweets:: Response received successfully.");
                        insertNewFetchedData(Tweet.parseTweetListFromJson(response));
                        stopRefreshing();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e(TAG, "fetchNewTweets:: Failed to receive api response: " + responseString, throwable);
                        stopRefreshing();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, "fetchNewTweets:: Failed to receive api response: " + errorResponse, throwable);
                        stopRefreshing();
                    }
                });
            }
        };
        handler.postDelayed(runnableCode, 2000);
    }

    private void setupAdapter(List<Tweet> tweets) {
        tweetAdapter = new TweetAdapter(tweets, this);
        tweetRecyclerView.setAdapter(tweetAdapter);
    }

    /**
     * Appends next page data at the bottom of list
     * @param tweets
     */
    private void insertNewPageData(final List<Tweet> tweets) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tweetAdapter.setMoreData(tweets);
                tweetAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Inserts new tweets at the top of list
     * @param tweets
     */
    private void insertNewFetchedData(final List<Tweet> tweets) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tweetAdapter.setNewData(tweets);
                tweetAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Adds new tweet at the top of list
     * @param tweet
     */
    private void addNewData(final Tweet tweet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tweetAdapter.addNewItem(tweet);
                tweetAdapter.notifyDataSetChanged();
                tweetRecyclerView.scrollToPosition(0);
            }
        });
    }

    /**
     * Updates existing tweet data in adapter
     * @param tweet
     * @param adapterPosition
     */
    private void updateDataInAdapter(final Tweet tweet, final int adapterPosition) {
        // refresh tweet info in adapter
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tweetAdapter.updateData(adapterPosition, tweet);
                tweetAdapter.notifyDataSetChanged();
            }
        });
    }

    private void stopRefreshing() {
        swipeRefreshTweetLayout.setRefreshing(false);
    }

    private void showComposeDialogFragment(Intent intent) {
        ComposeDialogFragment dialogFragment = ComposeDialogFragment.newInstance("Compose new tweet", this, intent);
        dialogFragment.show(getFragmentManager(), "Compose");
    }

    private void showTweetDetailActivity(Tweet selectedTweet, int position) {
        Intent intent = new Intent(this, TweetDetailActivity.class);
        intent.putExtra(TweetDetailActivity.ARG_SELECTED_TWEET, Parcels.wrap(selectedTweet));
        intent.putExtra(TweetDetailActivity.ARG_SELECTED_TWEET_POS, position);
        startActivityForResult(intent, TWEET_DETAIL_STATUS);
    }

    @Override
    public void onPostCompleted(Tweet createdTweet) {
        addNewData(createdTweet);
    }

    @Override
    public void onPostFailure(Throwable throwable) {
        Log.e(TAG, "onPostFailure:: Failed to post tweet: " + throwable.getMessage(), throwable);
        Toast.makeText(this, "Failed to post tweet. Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TWEET_DETAIL_STATUS:
                if (resultCode == RESULT_OK) handleTweetDetailResult(data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleTweetDetailResult(Intent data) {
        Tweet tweet = Parcels.unwrap(data.getParcelableExtra(TweetDetailActivity.ARG_SELECTED_TWEET));
        int position = data.getIntExtra(TweetDetailActivity.ARG_SELECTED_TWEET_POS, -1);

        if (data.getBooleanExtra(TweetDetailActivity.ARG_IS_TWEET_MODIFIED, false)) {
            // refresh tweet info in adapter
            updateDataInAdapter(tweet, position);
        }
        if (data.getBooleanExtra(TweetDetailActivity.ARG_IS_TWEET_REPLIED, false)) {
            // insert tweet info in adapter
            Tweet repliedTweet = Parcels.unwrap(data.getParcelableExtra(TweetDetailActivity.ARG_REPLIED_TWEET_INFO));
            addNewData(repliedTweet);
        }
    }

    @Override
    public void onRetweetActionSuccess(Tweet tweet, boolean isUndoAction, int adapterPosition) {
        if (!isUndoAction) {
            //
            Tweet originalTweet = tweetAdapter.getItemAt(adapterPosition);
            originalTweet.setRetweeted(tweet.isRetweeted());
            originalTweet.setRetweetCount(tweet.getRetweetCount());

            updateDataInAdapter(originalTweet, adapterPosition);

        } else {
            // refresh tweet info in adapter
            int count = tweet.getRetweetCount();
            tweet.setRetweetCount(--count);
            tweet.setRetweeted(false);
            updateDataInAdapter(tweet, adapterPosition);
        }

        Toast.makeText(this, "Retweet status updated.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRetweetActionFailure(String error, boolean isUndoAction) {
        Log.e(TAG, "Failed to update retweet status. Error: " + error);
    }

    @Override
    public void onFavoritedActionSuccess(Tweet tweet, boolean isUndoAction, int adapterPosition) {
        updateDataInAdapter(tweet, adapterPosition); // refresh tweet info in adapter
        Toast.makeText(this, "Favorite status updated.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoritedActionFailure(String error, boolean isUndoAction) {
        Log.e(TAG, "Failed to update favorite status. Error: " + error);
    }
}
