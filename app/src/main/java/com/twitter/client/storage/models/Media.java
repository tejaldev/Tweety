package com.twitter.client.storage.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
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
@Parcel(analyze={Media.class})
public class Media extends BaseModel {

	@PrimaryKey
	@Column
	Long mediaId;

	@Column
	String type;

    @Column
	String mediaUrl;

	@Column
	String mediaUrlHttps;

    @Column
    @ForeignKey(stubbedRelationship = true)
    Tweet tweet;

	public Media() {
		super();
	}

	// Parse model from JSON
	public Media(JSONObject jsonObject){
		super();

		try {
            this.mediaId = jsonObject.optLong("id");
            this.type = jsonObject.optString("type");
			this.mediaUrl = jsonObject.optString("media_url");
            this.mediaUrlHttps = jsonObject.optString("media_url_https");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long id) {
        this.mediaId = id;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getMediaUrlHttps() {
		return mediaUrlHttps;
	}

	public void setMediaUrlHttps(String mediaUrlHttps) {
		this.mediaUrlHttps = mediaUrlHttps;
	}

	public Tweet getTweet() {
		return tweet;
	}

	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}
}
