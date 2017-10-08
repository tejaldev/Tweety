package com.twitter.client.fragments;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.twitter.client.R;
import com.twitter.client.Testing;
import com.twitter.client.TweetApplication;
import com.twitter.client.adapters.TweetAdapter;
import com.twitter.client.adapters.TweetStatusActionHelper;
import com.twitter.client.listeners.EndlessRecyclerViewScrollListener;
import com.twitter.client.storage.TweetDatabaseHelper;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Fragment shows mentions timeline tweets
 *
 * @author tejalpar
 */
public class MentionsTimeLineFragment extends Fragment implements TweetAdapter.TweetItemClickListener, TweetStatusActionHelper.OnStatusUpdatedListener {
    public static String TAG = MentionsTimeLineFragment.class.getSimpleName();

    private TweetAdapter tweetAdapter;
    private RecyclerView tweetRecyclerView;
    private SwipeRefreshLayout swipeRefreshTweetLayout;
    private TweetStatusActionHelper statusActionHelper;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_tweet_list, container, false);

        statusActionHelper = new TweetStatusActionHelper(this);

        tweetRecyclerView = (RecyclerView) rootView.findViewById(R.id.tweets_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        tweetRecyclerView.setLayoutManager(layoutManager);
        loadMentions();

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Refer below link to understand how to fetch next page of tweets
                // https://developer.twitter.com/en/docs/tweets/timelines/guides/working-with-timelines
                //makeDelayedNextPageRequests(TweetApplication.getCurrMinTweetId() - 1);
            }
        };
        // Adds the scroll listener to RecyclerView
        tweetRecyclerView.addOnScrollListener(scrollListener);

        swipeRefreshTweetLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_tweets);
        swipeRefreshTweetLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //fetchNewTweets();
            }
        });
        return rootView;
    }


    private void loadMentions() {
        //fetch tweets
        //if no network then fetch from db
        if (MiscUtils.isNetworkAvailable(this.getContext())) {
            // fetch tweets
            TweetApplication.getTweetRestClient().getMentionsTimelineTweets(null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "loadTweets:: Response received successfully.");
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
                        }
                    });
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

        } else {
            Toast.makeText(this.getContext(), "No network available. Loading offline tweets.", Toast.LENGTH_LONG).show();
            setupAdapter(com.twitter.client.storage.models.Tweet.getTweets());
        }

//        try {
//            setupAdapter(com.twitter.client.storage.models.Tweet.getMentions());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void setupAdapter(List<Tweet> tweets) {
        tweetAdapter = new TweetAdapter(tweets, this);
        tweetRecyclerView.setAdapter(tweetAdapter);
    }

    @Override
    public void onRetweetActionSuccess(Tweet updatedTweet, boolean isUndoAction, int adapterPosition) {

    }

    @Override
    public void onRetweetActionFailure(String error, boolean isUndoAction) {

    }

    @Override
    public void onFavoritedActionSuccess(Tweet updatedTweet, boolean isUndoAction, int adapterPosition) {

    }

    @Override
    public void onFavoritedActionFailure(String error, boolean isUndoAction) {

    }

    @Override
    public void onTweetItemClickListener(View view, Tweet selectedTweet, int position) {

    }

    @Override
    public void onReplyClickListener(View view, Tweet selectedTweet, int position) {

    }

    @Override
    public void onRetweetClickListener(View view, Tweet selectedTweet, int position) {

    }

    @Override
    public void onFavoriteClickListener(View view, Tweet selectedTweet, int position) {

    }

    @Override
    public void onAvatarImageClickListener(View view, Tweet selectedTweet, int position) {

    }
}
