package com.twitter.client;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.twitter.client.network.TweetClient;
import com.twitter.client.network.response.models.User;

import android.app.Application;
import android.content.Context;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     RestClient client = RestApplication.getRestClient();
 *     // use client to send requests to API
 *
 */
public class TweetApplication extends Application {
    private static long currMinTweetId;
	private static Context context;
    private static User loggedInUser;

	@Override
	public void onCreate() {
		super.onCreate();

		FlowManager.init(new FlowConfig.Builder(this).build());
		FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);

		TweetApplication.context = this;
        TweetApplication.currMinTweetId = -1;
	}

	public static TweetClient getTweetRestClient() {
		return (TweetClient) TweetClient.getInstance(TweetClient.class, TweetApplication.context);
	}

    public static long getCurrMinTweetId() {
        return currMinTweetId;
    }

    public static void setCurrMinTweetId(long minTweetId) {
        currMinTweetId = minTweetId;
    }

    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }
}