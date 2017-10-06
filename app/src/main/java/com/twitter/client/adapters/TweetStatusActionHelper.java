package com.twitter.client.adapters;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.twitter.client.TweetApplication;
import com.twitter.client.storage.models.Tweet;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * API helper for posting retweet and fav status
 *
 * @author tejalpar
 */
public class TweetStatusActionHelper {
    public static String TAG = TweetStatusActionHelper.class.getSimpleName();

    private OnStatusUpdatedListener statusUpdatedListener;

    public interface OnStatusUpdatedListener {
        void onRetweetActionSuccess(Tweet updatedTweet, boolean isUndoAction, int adapterPosition);

        void onRetweetActionFailure(String error, boolean isUndoAction);

        void onFavoritedActionSuccess(Tweet updatedTweet, boolean isUndoAction, int adapterPosition);

        void onFavoritedActionFailure(String error, boolean isUndoAction);
    }

    public TweetStatusActionHelper(OnStatusUpdatedListener listener) {
        statusUpdatedListener = listener;
    }

    public void handleRetweetStatusAction(final Tweet selectedTweet, final int adapterPosition) {
        if (selectedTweet.isRetweeted()) {
            // user wants to undo retweet action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    TweetApplication.getTweetRestClient().postUnReTweetStatus(String.valueOf(selectedTweet.getTweetId()), null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                            Log.d(TAG, "UndoRetweet:: Undo retweet successful.");
                            statusUpdatedListener.onRetweetActionSuccess(new Tweet(response), true, adapterPosition);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "UndoRetweet:: Failed to undo retweet. Error: " + responseString, throwable);
                            statusUpdatedListener.onRetweetActionFailure(responseString, true);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "UndoRetweet:: Failed to undo retweet. Error: " + errorResponse, throwable);
                            statusUpdatedListener.onRetweetActionFailure(throwable.getMessage(), true);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();

        } else {
            // user wants to perform retweet action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    TweetApplication.getTweetRestClient().postReTweetStatus(String.valueOf(selectedTweet.getTweetId()), null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                            Log.d(TAG, "Retweet:: Retweet successful.");
                            statusUpdatedListener.onRetweetActionSuccess(new Tweet(response), false, adapterPosition);
                            //statusUpdatedListener.onRetweetActionSuccess(Tweet.parseTweetFromJson(response), false, adapterPosition);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "Retweet:: Failed to retweet. Error: " + responseString, throwable);
                            statusUpdatedListener.onRetweetActionFailure(responseString, false);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Retweet:: Failed to retweet. Error: " + errorResponse, throwable);
                            statusUpdatedListener.onRetweetActionFailure(throwable.getMessage(), false);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();
        }
    }

    public void handleFavoritedStatusAction(final Tweet selectedTweet, final int adapterPosition) {
        if (selectedTweet.isFavorited()) {
            // user wants to undo favorite action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    RequestParams params = new RequestParams();
                    params.put("id", String.valueOf(selectedTweet.getTweetId()));
                    TweetApplication.getTweetRestClient().postFavoritesDestroyStatus(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                            Log.d(TAG, "UndoFavorite:: Undo favorite successful.");
                            statusUpdatedListener.onFavoritedActionSuccess(new Tweet(response), true, adapterPosition);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "UndoFavorite:: Failed to undo retweet. Error: " + responseString, throwable);
                            statusUpdatedListener.onFavoritedActionFailure(responseString, true);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "UndoFavorite:: Failed to undo retweet. Error: " + errorResponse, throwable);
                            statusUpdatedListener.onFavoritedActionFailure(throwable.getMessage(), true);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();

        } else {
            // user wants to perform favorite action
            Runnable runnableCode = new Runnable() {
                @Override
                public void run() {
                    RequestParams params = new RequestParams();
                    params.put("id", String.valueOf(selectedTweet.getTweetId()));
                    TweetApplication.getTweetRestClient().postFavoritesCreateStatus(params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                            Log.d(TAG, "Favorite:: Favorite action successful.");
                            statusUpdatedListener.onFavoritedActionSuccess(new Tweet(response), false, adapterPosition);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "Favorite:: Failed to favorite. Error: " + responseString, throwable);
                            statusUpdatedListener.onFavoritedActionFailure(responseString, false);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Favorite:: Failed to favorite. Error: " + errorResponse, throwable);
                            statusUpdatedListener.onFavoritedActionFailure(throwable.getMessage(), false);
                        }
                    });
                }
            };
            new Thread(runnableCode).run();
        }
    }
}
