package com.twitter.client.storage.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.twitter.client.storage.TweetDatabase;

import org.json.JSONObject;
import org.parceler.Parcel;

/*
 * This is a temporary, sample model that demonstrates the basic structure
 * of a SQLite persisted Model object. Check out the DBFlow wiki for more details:
 * https://github.com/codepath/android_guides/wiki/DBFlow-Guide
 *
 * Note: All models **must extend from** `BaseModel` as shown below.
 * 
 */
@Table(database = TweetDatabase.class)
@Parcel(analyze={UserMentions.class})
public class UserMentions extends BaseModel {

    @Column
    @PrimaryKey
    @ForeignKey(stubbedRelationship = true)
    Tweet tweet;

	@Column
    @PrimaryKey
	@ForeignKey(stubbedRelationship = true)
	User user;

	public UserMentions() {
		super();
	}

	// Parse model from JSON
	public UserMentions(JSONObject jsonObject){
		super();

		try {
            this.user = new User(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Tweet getTweet() {
		return tweet;
	}

	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static long getMaxMentionsTweetId() {
        UserMentions mentions = SQLite.select(UserMentions_Table.tweet_tweetId, Method.max(UserMentions_Table.tweet_tweetId)).from(UserMentions.class).querySingle();

        long max = -1;
        if (mentions != null) {
            max = mentions.getTweet().getTweetId();
        }
        return max;
    }

    public static long getMinMentionsTweetId() {
        UserMentions mentions = SQLite.select(UserMentions_Table.tweet_tweetId, Method.min(UserMentions_Table.tweet_tweetId)).from(UserMentions.class).querySingle();

        long min = -1;
        if (mentions != null) {
            min = mentions.getTweet().getTweetId();
        }
        return min;
    }
}
