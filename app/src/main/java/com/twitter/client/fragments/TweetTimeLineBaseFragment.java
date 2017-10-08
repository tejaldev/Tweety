package com.twitter.client.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.twitter.client.R;
import com.twitter.client.TweetApplication;
import com.twitter.client.activities.TweetDetailActivity;
import com.twitter.client.activities.UserProfileActivity;
import com.twitter.client.adapters.TweetAdapter;
import com.twitter.client.adapters.TweetStatusActionHelper;
import com.twitter.client.listeners.EndlessRecyclerViewScrollListener;
import com.twitter.client.storage.TweetDatabaseHelper;
import com.twitter.client.storage.models.Tweet;

import org.parceler.Parcels;

import java.util.List;

/**
 * Main fragment holding all list view display logic
 *
 * @author tejalpar
 */
public abstract class TweetTimeLineBaseFragment extends Fragment implements TweetAdapter.TweetItemClickListener, TweetStatusActionHelper.OnStatusUpdatedListener {
    public static String TAG = TweetTimeLineBaseFragment.class.getSimpleName();
    public static final int TWEET_DETAIL_STATUS = 1;

    protected Handler handler;
    protected TweetAdapter tweetAdapter;
    protected ProgressBar progressBar;
    protected RecyclerView tweetRecyclerView;
    protected SwipeRefreshLayout swipeRefreshTweetLayout;
    protected TweetStatusActionHelper statusActionHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_tweet_list, container, false);
        setupRootView(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadTweets();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        Intent intent = new Intent(this.getContext(), UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.ARGS_SELECTED_USER, Parcels.wrap(selectedTweet.getUser()));
        startActivity(intent);
    }

    protected void setupRootView(View rootView) {
        handler = new Handler();
        statusActionHelper = new TweetStatusActionHelper(this);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        tweetRecyclerView = (RecyclerView) rootView.findViewById(R.id.tweets_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        tweetRecyclerView.setLayoutManager(layoutManager);

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

        swipeRefreshTweetLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_tweets);
        swipeRefreshTweetLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewTweets();
            }
        });
    }

    protected void setupAdapter(final List<Tweet> tweets) {
        final TweetAdapter.TweetItemClickListener listener = this;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                tweetAdapter = new TweetAdapter(tweets, listener);
                tweetRecyclerView.setAdapter(tweetAdapter);
            }
        });
    }

    /**
     * Appends next page data at the bottom of list
     * @param tweets
     */
    protected void insertNewPageData(final List<Tweet> tweets) {
        getActivity().runOnUiThread(new Runnable() {
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
        getActivity().runOnUiThread(new Runnable() {
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
        getActivity().runOnUiThread(new Runnable() {
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
        getActivity().runOnUiThread(new Runnable() {
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
        Intent intent = new Intent(this.getContext(), TweetDetailActivity.class);
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
        Toast.makeText(this.getContext(), "Retweet status updated.", Toast.LENGTH_SHORT).show();

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
        Toast.makeText(this.getContext(), "Favorite status updated.", Toast.LENGTH_SHORT).show();
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
     * Fetches tweets
     */
    protected abstract void loadTweets();

    /**
     * Fetches new page
     */
    protected abstract void makeDelayedNextPageRequests(final long maxId);

    /**
     * Fetches new tweets using rest client - Swipe refresh
     */
    protected abstract void fetchNewTweets();
}
