package com.twitter.client.storage.models;

import android.text.TextUtils;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.twitter.client.storage.TweetDatabase;
import com.twitter.client.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/*
 * This is a temporary, sample model that demonstrates the basic structure
 * of a SQLite persisted Model object. Check out the DBFlow wiki for more details:
 * https://github.com/codepath/android_guides/wiki/DBFlow-Guide
 *
 * Note: All models **must extend from** `BaseModel` as shown below.
 * 
 */
@Table(database = TweetDatabase.class)
@Parcel(analyze={Tweet.class})
public class Tweet extends BaseModel {

    @PrimaryKey
	@Column
	Long tweetId;

	// Define table fields
	@Column
	String text;

	@Column
	String createdAt;

    @Column
    @ForeignKey(saveForeignKeyModel = true)
    User user;

    @Column
    String relativeTimeStamp;

    @Column
    boolean retweeted;

    @Column
    boolean favorited;

    @Column
    int retweetCount;

    @Column
    int favoriteCount;

    @ColumnIgnore
    List<Media> mediaList;

    @ColumnIgnore
    List<UserMentions> mentionsList;

	public Tweet() {
		super();
	}

	// Parse model from JSON
	public Tweet(JSONObject jsonObject){
		super();

		try {
            this.tweetId = jsonObject.optLong("id");
			this.text = jsonObject.optString("text");
            this.createdAt = jsonObject.optString("created_at");
            this.relativeTimeStamp = MiscUtils.getRelativeTimeAgo(this.createdAt);
            this.retweeted = jsonObject.optBoolean("retweeted");
            this.favorited = jsonObject.optBoolean("favorited");
            this.retweetCount = jsonObject.optInt("retweet_count");
            this.favoriteCount = jsonObject.optInt("favorite_count");

            // media
            JSONObject entitiesObj = jsonObject.optJSONObject("entities");
            JSONArray mediaArray = entitiesObj.optJSONArray("media");
            if (mediaArray != null) {
                this.mediaList = new ArrayList<>();
                for (int j = 0; j < mediaArray.length(); j++) {
                    JSONObject mediaObject = mediaArray.optJSONObject(j);
                    Media media = new Media(mediaObject);
                    //media.setTweet(this);
                    this.mediaList.add(media);
                }
            }

            // user
            JSONObject userObject = jsonObject.optJSONObject("user");
            this.user = new User(userObject);


            // mentions
            JSONArray mentionsArray = entitiesObj.optJSONArray("user_mentions");
            if (mentionsArray != null) {
                this.mentionsList = new ArrayList<>();
                for (int j = 0; j < mentionsArray.length(); j++) {
                    JSONObject userObj = mentionsArray.optJSONObject(j);
                    UserMentions mentions = new UserMentions(userObj);
                    this.mentionsList.add(mentions);
                }
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "mediaList")
    public List<Media> getMediaList() {
        if (mediaList == null || mediaList.isEmpty()) {
            mediaList = SQLite.select()
                    .from(Media.class)
                    .where(Media_Table.tweet_tweetId.eq(tweetId))
                    .queryList();
        }
        return mediaList;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "mentionsList")
    public List<UserMentions> getMentionsList() {
        if (mentionsList == null || mentionsList.isEmpty()) {
            mentionsList = SQLite.select()
                    .from(UserMentions.class)
                    .where(UserMentions_Table.tweet_tweetId.eq(tweetId))
                    .queryList();
        }
        return mentionsList;
    }

    @Override
    public boolean save() {
        boolean result = super.save();

        if (mediaList != null && !mediaList.isEmpty()) {
            for (Media s : mediaList) {
                s.setTweet(this);
                s.save();
            }
        }


        if (mentionsList != null && !mentionsList.isEmpty()) {
            for (UserMentions s : mentionsList) {
                s.setTweet(this);
                s.save();
            }
        }
        return result;
    }

    public Long getTweetId() {
        return tweetId;
    }

    public void setTweetId(Long id) {
        this.tweetId = id;
    }

	public String getText() {
		return text;
	}

	public void setText(String name) {
		this.text = name;
	}

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRelativeTimeStamp() {
        return relativeTimeStamp;
    }

    public void setRelativeTimeStamp(String relativeTimeStamp) {
        this.relativeTimeStamp = relativeTimeStamp;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public boolean hasImage() {
        if (mediaList != null && mediaList.size() > 0) {
            return (mediaList.get(0).getMediaUrl() != null && !TextUtils.isEmpty(mediaList.get(0).getMediaUrl()));
        }
        return false;
    }

    /* The where class in this code below will be marked red until you first compile the project, since the code
	 * for the SampleModel_Table class is generated at compile-time.
	 */
	
//	// Record Finders
	public static Tweet byId(long id) {
		return new Select().from(Tweet.class).where(Tweet_Table.tweetId.eq(id)).querySingle();
	}

	public static List<Tweet> recentItems() {
		return new Select().from(Tweet.class).orderBy(Tweet_Table.tweetId, false).limit(20).queryList();
	}

    public static List<Tweet> getTweets() {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getOldTweets(long oldTweetId) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.tweetId.lessThan(oldTweetId))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getRecentTweets(long newestTweetId) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.tweetId.greaterThan(newestTweetId))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getMentions(long userId) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.tweetId.in(SQLite.select(UserMentions_Table.tweet_tweetId)
                        .from(UserMentions.class)
                        .where(UserMentions_Table.user_userId.is(userId))))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getOldMentions(long oldTweetId, long userId) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.tweetId.in(SQLite.select(UserMentions_Table.tweet_tweetId)
                        .from(UserMentions.class)
                        .where(UserMentions_Table.user_userId.is(userId))
                        .and(UserMentions_Table.tweet_tweetId.lessThan(oldTweetId))))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getRecentMentions(long newestTweetId, long userId) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.tweetId.in(SQLite.select(UserMentions_Table.tweet_tweetId)
                        .from(UserMentions.class)
                        .where(UserMentions_Table.user_userId.is(userId))
                        .and(UserMentions_Table.tweet_tweetId.greaterThan(newestTweetId))))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getTweetsByUser(String screenName) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.user_userId.in(SQLite.select(User_Table.userId)
                        .from(User.class)
                        .where(User_Table.screenName.is(screenName))))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getOldTweetsByUser(long oldTweetId, String screenName) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.tweetId.lessThan(oldTweetId))
                .and(Tweet_Table.user_userId
                        .in(SQLite.select(User_Table.userId)
                                .from(User.class)
                                .where(User_Table.screenName.is(screenName))))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static List<Tweet> getRecentTweetsByUser(long newestTweetId, String screenName) {
        return SQLite.select()
                .from(Tweet.class)
                .leftOuterJoin(Media.class)
                .on(Tweet_Table.tweetId.is(Media_Table.tweet_tweetId))
                .where(Tweet_Table.tweetId.greaterThan(newestTweetId))
                .and(Tweet_Table.user_userId
                        .in(SQLite.select(User_Table.userId)
                                .from(User.class)
                                .where(User_Table.screenName.is(screenName))))
                .orderBy(Tweet_Table.tweetId, false)
                .queryList();
    }

    public static long getMaxTweetId() {
        Tweet tweet = SQLite.select(Tweet_Table.tweetId, Method.max(Tweet_Table.tweetId)).from(Tweet.class).querySingle();

        long max = -1;
        if (tweet != null) {
            max = tweet.getTweetId();
        }
        return max;
    }

    public static long getMinTweetId() {
        Tweet tweet = SQLite.select(Tweet_Table.tweetId, Method.min(Tweet_Table.tweetId)).from(Tweet.class).querySingle();

        long min = -1;
        if (tweet != null) {
            min = tweet.getTweetId();
        }
        return min;
    }
}
