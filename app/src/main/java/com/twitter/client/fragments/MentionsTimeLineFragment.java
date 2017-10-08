package com.twitter.client.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.twitter.client.TweetApplication;
import com.twitter.client.listeners.EndlessRecyclerViewScrollListener;
import com.twitter.client.storage.TweetDatabaseHelper;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.storage.models.UserMentions;
import com.twitter.client.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Fragment shows mentions timeline tweets
 *
 * @author tejalpar
 */
public class MentionsTimeLineFragment extends TweetTimeLineBaseFragment  {
    public static String TAG = MentionsTimeLineFragment.class.getSimpleName();

    public MentionsTimeLineFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MentionsTimeLineFragment newInstance() {
        MentionsTimeLineFragment fragment = new MentionsTimeLineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void setupScrollListenerWithLayoutMgr() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        tweetRecyclerView.setLayoutManager(layoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Refer below link to understand how to fetch next page of tweets
                // https://developer.twitter.com/en/docs/tweets/timelines/guides/working-with-timelines
                makeDelayedNextPageRequests(UserMentions.getMinMentionsTweetId() - 1);
            }
        };
        // Adds the scroll listener to RecyclerView
        tweetRecyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    protected void loadTweets() {
        //fetch tweets
        //if no network then fetch from db
        if (MiscUtils.isNetworkAvailable(this.getContext())) {
            showProgressBar();
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    TweetApplication.getTweetRestClient().getMentionsTimelineTweets(null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "loadTweets:: Response received successfully.");
                            hideProgressBar();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.d(TAG, "loadTweets:: Response received successfully.");
                            TweetDatabaseHelper.getInstance().saveTweetsToDB(response, new Transaction.Success() {
                                @Override
                                public void onSuccess(@NonNull Transaction transaction) {
                                    Log.d(TAG, "Tweets saved to database.");
                                    setupAdapter(com.twitter.client.storage.models.Tweet.getMentions(TweetApplication.getLoggedInUser().getUserId()));
                                }
                            }, new Transaction.Error() {
                                @Override
                                public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                                    Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
                                    hideProgressBar();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "loadTweets:: Failed to receive api response: " + responseString, throwable);
                            hideProgressBar();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "loadTweets:: Failed to receive api response: " + errorResponse, throwable);
                            hideProgressBar();
                        }
                    });
                }
            };
            handler.postDelayed(runnableCode, 1000);

        } else {
            Toast.makeText(this.getContext(), "No network available. Loading offline tweets.", Toast.LENGTH_LONG).show();
            setupAdapter(com.twitter.client.storage.models.Tweet.getTweets());
        }
    }

    @Override
    protected void makeDelayedNextPageRequests(final long maxId) {
        if (MiscUtils.isNetworkAvailable(this.getContext())) {
            showProgressBar();
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    RequestParams params = new RequestParams();
                    params.put("max_id", maxId);
                    TweetApplication.getTweetRestClient().getMentionsTimelineTweets(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(TAG, "loadMoreTweets:: Response received successfully.");
                            hideProgressBar();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.d(TAG, "loadMoreTweets:: Response received successfully.");
                            TweetDatabaseHelper.getInstance().saveTweetsToDB(response, new Transaction.Success() {
                                @Override
                                public void onSuccess(@NonNull Transaction transaction) {
                                    Log.d(TAG, "Tweets saved to database.");
                                    insertNewPageData(com.twitter.client.storage.models.Tweet.getOldMentions(maxId, TweetApplication.getLoggedInUser().getUserId()));
                                }
                            }, new Transaction.Error() {
                                @Override
                                public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                                    Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
                                    hideProgressBar();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "loadMoreTweets:: Failed to receive api response: " + responseString, throwable);
                            hideProgressBar();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "loadMoreTweets:: Failed to receive api response: " + errorResponse, throwable);
                            hideProgressBar();
                        }
                    });
                }
            };
            // Run the above code block on the main thread after 2 seconds
            handler.postDelayed(runnableCode, 1000);
        }  else {
            Toast.makeText(this.getContext(), "No network available. Cannot load more mentions.", Toast.LENGTH_LONG).show();
            hideProgressBar();
        }
    }

    @Override
    protected void fetchNewTweets() {
        if (MiscUtils.isNetworkAvailable(this.getContext())) {
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    RequestParams params = new RequestParams();
                    params.put("since_id", TweetApplication.getCurrMaxTweetId());
                    TweetApplication.getTweetRestClient().getMentionsTimelineTweets(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.d(TAG, "fetchNewTweets:: Response received successfully.");
                            TweetDatabaseHelper.getInstance().saveTweetsToDB(response, new Transaction.Success() {
                                @Override
                                public void onSuccess(@NonNull Transaction transaction) {
                                    Log.d(TAG, "Tweets saved to database.");
                                    insertNewFetchedData(Tweet.getRecentMentions(UserMentions.getMaxMentionsTweetId(), TweetApplication.getLoggedInUser().getUserId()));
                                    stopRefreshing();
                                }
                            }, new Transaction.Error() {
                                @Override
                                public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                                    Log.e(TAG, "Error occurred while saving tweets to database: " + error.getMessage());
                                    stopRefreshing();
                                }
                            });
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
        } else {
            Toast.makeText(this.getContext(), "No network available. Cannot fetch new tweets.", Toast.LENGTH_LONG).show();
            stopRefreshing();
        }
    }
}
