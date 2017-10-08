package com.twitter.client.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.twitter.client.TweetApplication;
import com.twitter.client.storage.TweetDatabaseHelper;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Shows user profile info
 *
 * @author tejalpar
 */
public class UserTimeLineFragment extends TweetTimeLineBaseFragment {
    public static String TAG = UserTimeLineFragment.class.getSimpleName();
    public static String ARGS_USER_SCREEN_NAME = "USER_SCREEN_NAME";

    private String userScreenName;

    public UserTimeLineFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static UserTimeLineFragment newInstance(String userScreenName) {
        UserTimeLineFragment fragment = new UserTimeLineFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_USER_SCREEN_NAME, userScreenName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        userScreenName = getArguments().getString(ARGS_USER_SCREEN_NAME);
        return rootView;
    }

    @Override
    protected void loadTweets() {
        //fetch tweets
        // if no network then fetch from db
        if (MiscUtils.isNetworkAvailable(this.getContext())) {
            showProgressBar();
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    RequestParams params = new RequestParams();
                    params.put("screen_name", userScreenName);
                    // fetch tweets
                    TweetApplication.getTweetRestClient().getUserTimelineTweets(params, new JsonHttpResponseHandler() {
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
                                    setupAdapter(com.twitter.client.storage.models.Tweet.getTweetsByUser(userScreenName));
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
            setupAdapter(com.twitter.client.storage.models.Tweet.getTweetsByUser(userScreenName));
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
                    params.put("screen_name", userScreenName);
                    TweetApplication.getTweetRestClient().getUserTimelineTweets(params, new JsonHttpResponseHandler() {
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
                                    insertNewPageData(com.twitter.client.storage.models.Tweet.getOldTweetsByUser(maxId, userScreenName));
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
            Toast.makeText(this.getContext(), "No network available. Cannot load more tweets.", Toast.LENGTH_LONG).show();
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
                    params.put("screen_name", userScreenName);
                    TweetApplication.getTweetRestClient().getUserTimelineTweets(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.d(TAG, "fetchNewTweets:: Response received successfully.");
                            TweetDatabaseHelper.getInstance().saveTweetsToDB(response, new Transaction.Success() {
                                @Override
                                public void onSuccess(@NonNull Transaction transaction) {
                                    Log.d(TAG, "Tweets saved to database.");
                                    insertNewFetchedData(Tweet.getRecentTweetsByUser(TweetApplication.getCurrMaxTweetId(), userScreenName));
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
