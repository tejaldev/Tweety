package com.twitter.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.twitter.client.R;
import com.twitter.client.TweetApplication;
import com.twitter.client.adapters.TweetAdapter;
import com.twitter.client.adapters.TweetStatusActionHelper;
import com.twitter.client.listeners.EndlessRecyclerViewScrollListener;
import com.twitter.client.storage.TweetDatabaseHelper;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Displays search query results
 *
 * @author tejalpar
 */
public class SearchActivity extends AppCompatActivity implements TweetAdapter.TweetItemClickListener, TweetStatusActionHelper.OnStatusUpdatedListener {
    public static String TAG = SearchActivity.class.getSimpleName();
    public static String ARGS_SEARCH_QUERY = "SEARCH_QUERY";

    public static final int TWEET_DETAIL_STATUS = 1;

    private long currMaxId;
    private long currSinceId;
    private String searchQuery;

    protected Handler handler;
    protected TweetAdapter tweetAdapter;
    @BindView(R.id.progress_bar) protected ProgressBar progressBar;
    @BindView(R.id.tweets_recycler_view) protected RecyclerView tweetRecyclerView;
    @BindView(R.id.swipe_refresh_tweets) protected SwipeRefreshLayout swipeRefreshTweetLayout;
    protected TweetStatusActionHelper statusActionHelper;
    protected EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_search_item);
        toolbar.setNavigationIcon(R.drawable.ic_back_nav);
        setSupportActionBar(toolbar);

        searchQuery = getIntent().getStringExtra(ARGS_SEARCH_QUERY);

        // Bind views
        ButterKnife.bind(this);
        setupRootView();
        makeDelayedSearchRequests(searchQuery);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the toolbar NavigationIcon as up/home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
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

    @Override
    public void onAvatarImageClickListener(View view, Tweet selectedTweet, int position) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.ARGS_SELECTED_USER, Parcels.wrap(selectedTweet.getUser()));
        startActivity(intent);
    }

    protected void setupRootView() {
        handler = new Handler();
        statusActionHelper = new TweetStatusActionHelper(this);
        setupScrollListenerWithLayoutMgr();
        swipeRefreshTweetLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewTweets();
            }
        });
    }

    protected void setupScrollListenerWithLayoutMgr() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        tweetRecyclerView.setLayoutManager(layoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Refer below link to understand how to fetch next page of tweets
                // https://developer.twitter.com/en/docs/tweets/timelines/guides/working-with-timelines
                makeDelayedNextPageRequests(currMaxId - 1);
            }
        };
        // Adds the scroll listener to RecyclerView
        tweetRecyclerView.addOnScrollListener(scrollListener);
    }

    protected void setupAdapter(final List<Tweet> tweets) {
        final TweetAdapter.TweetItemClickListener listener = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tweetAdapter != null) {
                    // adapter change
                    tweetAdapter.replaceItems(tweets);
                    tweetAdapter.notifyDataSetChanged();
                } else {
                    tweetAdapter = new TweetAdapter(tweets, listener);
                    tweetRecyclerView.setAdapter(tweetAdapter);
                }
                hideProgressBar();
            }
        });
    }

    /**
     * Appends next page data at the bottom of list
     * @param tweets
     */
    protected void insertNewPageData(final List<Tweet> tweets) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tweetAdapter.setMoreData(tweets);
                tweetAdapter.notifyDataSetChanged();
                hideProgressBar();
            }
        });
    }

    /**
     * Inserts new tweets at the top of list
     * @param tweets
     */
    protected void insertNewFetchedData(final List<Tweet> tweets) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tweetAdapter.setNewData(tweets);
                tweetAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Declared public so the activity can call this method to add new data
     *
     * Adds new tweet at the top of list
     * @param tweet
     */
    public void addNewData(final Tweet tweet) {
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

    protected void stopRefreshing() {
        swipeRefreshTweetLayout.setRefreshing(false);
    }

    protected void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showTweetDetailActivity(Tweet selectedTweet, int position) {
        Intent intent = new Intent(this, TweetDetailActivity.class);
        intent.putExtra(TweetDetailActivity.ARG_SELECTED_TWEET, Parcels.wrap(selectedTweet));
        intent.putExtra(TweetDetailActivity.ARG_SELECTED_TWEET_POS, position);
        startActivityForResult(intent, TWEET_DETAIL_STATUS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TWEET_DETAIL_STATUS:
                if (resultCode == Activity.RESULT_OK) handleTweetDetailResult(data);
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
        Tweet tweetToUpdate;
        if (!isUndoAction) {
            //
            tweetToUpdate = tweetAdapter.getItemAt(adapterPosition);
            tweetToUpdate.setRetweeted(tweet.isRetweeted());
            tweetToUpdate.setRetweetCount(tweet.getRetweetCount());
            updateDataInAdapter(tweetToUpdate, adapterPosition);

        } else {
            // refresh tweet info in adapter
            int count = tweet.getRetweetCount();
            tweet.setRetweetCount(--count);
            tweet.setRetweeted(false);
            updateDataInAdapter(tweet, adapterPosition);
            tweetToUpdate = tweet;
        }
        Toast.makeText(this, "Retweet status updated.", Toast.LENGTH_SHORT).show();

        TweetDatabaseHelper.getInstance().saveTweetToDB(tweetToUpdate, new Transaction.Success() {
            @Override
            public void onSuccess(@NonNull Transaction transaction) {
                Log.d(TAG, "Tweets saved to database.");
            }
        }, new Transaction.Error() {
            @Override
            public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
            }
        });
    }

    @Override
    public void onRetweetActionFailure(String error, boolean isUndoAction) {
        Log.e(TAG, "Failed to update retweet status. Error: " + error);
    }

    @Override
    public void onFavoritedActionSuccess(Tweet tweet, boolean isUndoAction, int adapterPosition) {
        updateDataInAdapter(tweet, adapterPosition); // refresh tweet info in adapter
        Toast.makeText(this, "Favorite status updated.", Toast.LENGTH_SHORT).show();
        TweetDatabaseHelper.getInstance().saveTweetToDB(tweet, new Transaction.Success() {
            @Override
            public void onSuccess(@NonNull Transaction transaction) {
                Log.d(TAG, "Tweets saved to database.");
            }
        }, new Transaction.Error() {
            @Override
            public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
            }
        });
    }

    @Override
    public void onFavoritedActionFailure(String error, boolean isUndoAction) {
        Log.e(TAG, "Failed to update favorite status. Error: " + error);
    }

    /**
     * Sends search request
     */
    private void makeDelayedSearchRequests(final String searchQuery) {
        if (MiscUtils.isNetworkAvailable(this)) {
            // reset state before starting a new search
            scrollListener.resetState();

            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    showProgressBar();
                    RequestParams params = new RequestParams();
                    params.put("q", searchQuery);
                    params.put("count", 20);
                    TweetApplication.getTweetRestClient().getSearchQueryResults(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "SearchTweets:: Response received successfully.");
                            setupAdapter(parseTweetsFromSearchResult(response));
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.d(TAG, "SearchTweets:: Response received successfully.");
                            hideProgressBar();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "SearchTweets:: Failed to receive api response: " + responseString, throwable);
                            hideProgressBar();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "SearchTweets:: Failed to receive api response: " + errorResponse, throwable);
                            hideProgressBar();
                        }
                    });
                }
            };
            // Run the above code block on the main thread after 0.5 secs
            handler.postDelayed(runnableCode, 500);
        }  else {
            Toast.makeText(this, "No network available. Cannot search for tweets.", Toast.LENGTH_LONG).show();
            hideProgressBar();
        }
    }

    /**
     * Fetches new page
     */
    private void makeDelayedNextPageRequests(final long maxId) {
        if (MiscUtils.isNetworkAvailable(this)) {
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    showProgressBar();
                    RequestParams params = new RequestParams();
                    params.put("q", searchQuery);
                    params.put("count", 20);
                    params.put("max_id", maxId);
                    TweetApplication.getTweetRestClient().getSearchQueryResults(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "SearchTweetNextPage:: Response received successfully.");
                            insertNewPageData(parseTweetsFromSearchResult(response));
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.d(TAG, "SearchTweetNextPage:: Response received successfully.");
                            hideProgressBar();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "SearchTweetNextPage:: Failed to receive api response: " + responseString, throwable);
                            hideProgressBar();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "SearchTweetNextPage:: Failed to receive api response: " + errorResponse, throwable);
                            hideProgressBar();
                        }
                    });
                }
            };
            // Run the above code block on the main thread after 2 seconds
            handler.postDelayed(runnableCode, 1000);
        }  else {
            Toast.makeText(this, "No network available. Cannot load more tweets.", Toast.LENGTH_LONG).show();
            hideProgressBar();
        }
    }

    /**
     * Fetches new tweets using rest client - Swipe refresh
     */
    private void fetchNewTweets() {
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                RequestParams params = new RequestParams();
                params.put("q", searchQuery);
                params.put("count", 20);
                params.put("since_id", currSinceId);
                TweetApplication.getTweetRestClient().getSearchQueryResults(params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, "SearchTweetNextPage:: Response received successfully.");
                        insertNewFetchedData(parseTweetsFromSearchResult(response));
                        stopRefreshing();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.d(TAG, "fetchNewTweets:: Response received successfully.");
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

    private List<Tweet> parseTweetsFromSearchResult(final JSONObject response) {
        List<Tweet> tweetModelList = new ArrayList<>();
        try {
            JSONArray jsonArray = response.optJSONArray("statuses");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Tweet tweet = new Tweet(jsonObject);
                    tweetModelList.add(tweet);
                }
            }
            currMaxId = parseMaxIdInfoFromSearchResult(response);
            currSinceId = parseSinceIdInfoFromSearchResult(response);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing search response: " + e);
            e.printStackTrace();
        }
        return tweetModelList;
    }

    private long parseMaxIdInfoFromSearchResult(final JSONObject response) {
        long maxId = 0;
        JSONObject searchMetadata = response.optJSONObject("search_metadata");
        if (searchMetadata != null) {
            searchMetadata.optLong("max_id");
        }
        return maxId;
    }

    private long parseSinceIdInfoFromSearchResult(final JSONObject response) {
        long sinceId = 0;
        JSONObject searchMetadata = response.optJSONObject("search_metadata");
        if (searchMetadata != null) {
            searchMetadata.optLong("since_id");
        }
        return sinceId;
    }
}
