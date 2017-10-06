package com.twitter.client.storage;

import android.database.Cursor;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;
import com.twitter.client.TweetApplication;
import com.twitter.client.storage.models.Media;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.storage.models.Tweet_Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * DB Helper class for performing database operations
 *
 * @author tejalpar
 */
public class TweetDatabaseHelper {
    public static String TAG = TweetDatabaseHelper.class.getSimpleName();

    private static TweetDatabaseHelper instance;

    public static TweetDatabaseHelper getInstance() {
        if (instance == null) {
            instance = new TweetDatabaseHelper();
        }
        return instance;
    }

    public void saveTweetsToDB(final JSONArray jsonArray, final Transaction.Success successCallback, final Transaction.Error errorCallback) {
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                try {
                    List<Tweet> tweetModelList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Tweet tweet = new Tweet(jsonObject);
                        tweetModelList.add(tweet);
                    }

                    ProcessModelTransaction<Tweet> processModelTransaction =
                            new ProcessModelTransaction.Builder<>(new ProcessModelTransaction.ProcessModel<Tweet>() {
                                @Override
                                public void processModel(Tweet model, DatabaseWrapper wrapper) {
                                    // call some operation on model here
                                    model.save();
                                }
                            }).processListener(new ProcessModelTransaction.OnModelProcessListener<Tweet>() {
                                @Override
                                public void onModelProcessed(long current, long total, Tweet modifiedModel) {
                                    //modelProcessedCount.incrementAndGet();
                                }
                            }).addAll(tweetModelList).build();
                    Transaction transaction = FlowManager.getDatabase(TweetDatabase.class)
                            .beginTransactionAsync(processModelTransaction)
                            .success(successCallback)
                            .error(errorCallback)
                            .build();
                    transaction.execute();

                    setMaxMinTweetId();

                } catch (JSONException e) {
                    Log.e(TAG, "saveTweetsToDB:: Error saving database: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        // starts a background thread
        new Thread(runnableCode).run();
    }

    public long saveTweetToDB(final JSONObject jsonObject, final Transaction.Success successCallback, final Transaction.Error errorCallback) {
        final Tweet tweet = new Tweet(jsonObject);
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessModelTransaction<Tweet> processModelTransaction =
                            new ProcessModelTransaction.Builder<>(new ProcessModelTransaction.ProcessModel<Tweet>() {
                                @Override
                                public void processModel(Tweet model, DatabaseWrapper wrapper) {
                                    // call some operation on model here
                                    model.save();
                                }
                            }).processListener(new ProcessModelTransaction.OnModelProcessListener<Tweet>() {
                                @Override
                                public void onModelProcessed(long current, long total, Tweet modifiedModel) {
                                    //modelProcessedCount.incrementAndGet();
                                }
                            }).add(tweet).build();
                    Transaction transaction = FlowManager.getDatabase(TweetDatabase.class)
                            .beginTransactionAsync(processModelTransaction)
                            .success(successCallback)
                            .error(errorCallback)
                            .build();
                    transaction.execute();

                    setMaxMinTweetId();

                } catch (Exception e) {
                    Log.e(TAG, "saveTweetsToDB:: Error saving database: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        // starts a background thread
        new Thread(runnableCode).run();

        return tweet.getTweetId();
    }

    public long saveTweetToDB(final Tweet tweet, final Transaction.Success successCallback, final Transaction.Error errorCallback) {
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessModelTransaction<Tweet> processModelTransaction =
                            new ProcessModelTransaction.Builder<>(new ProcessModelTransaction.ProcessModel<Tweet>() {
                                @Override
                                public void processModel(Tweet model, DatabaseWrapper wrapper) {
                                    // call some operation on model here
                                    model.save();
                                }
                            }).processListener(new ProcessModelTransaction.OnModelProcessListener<Tweet>() {
                                @Override
                                public void onModelProcessed(long current, long total, Tweet modifiedModel) {
                                    //modelProcessedCount.incrementAndGet();
                                }
                            }).add(tweet).build();
                    Transaction transaction = FlowManager.getDatabase(TweetDatabase.class)
                            .beginTransactionAsync(processModelTransaction)
                            .success(successCallback)
                            .error(errorCallback)
                            .build();
                    transaction.execute();

                    setMaxMinTweetId();

                } catch (Exception e) {
                    Log.e(TAG, "saveTweetsToDB:: Error saving database: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        // starts a background thread
        new Thread(runnableCode).run();

        return tweet.getTweetId();
    }

    private void setMaxMinTweetId() {
        long maxId = Tweet.getMaxTweetId();
        long minId = Tweet.getMinTweetId();

        if (TweetApplication.getCurrMinTweetId() != -1 && minId > 0 && minId < TweetApplication.getCurrMinTweetId()) {
            TweetApplication.setCurrMinTweetId(minId);
        } else {
            TweetApplication.setCurrMinTweetId(minId);
        }

        if (maxId > TweetApplication.getCurrMaxTweetId()) {
            TweetApplication.setCurrMaxTweetId(maxId);
        }
    }

    public List<Media> loadMediaFromDB() {
        return new Select()
                .from(Media.class)
                .queryList();
    }
}