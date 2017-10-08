package com.twitter.client.storage.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
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
}
