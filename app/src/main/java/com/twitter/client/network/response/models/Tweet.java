
package com.twitter.client.network.response.models;

import android.text.TextUtils;

import com.twitter.client.TweetApplication;
import com.twitter.client.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    //@SerializedName("created_at")
    private String createdAt;

    //@SerializedName("id")
    private long id;

    //@SerializedName("text")
    private String text;

    //@SerializedName("entities")
    private Entities entities;

    //@SerializedName("user")
    private User user;

    //@SerializedName("retweet_count")
    private int retweetCount;

    //@SerializedName("favorite_count")
    private int favoriteCount;

    //@SerializedName("favorited")
    private boolean favorited;

    //@SerializedName("retweeted")
    private boolean retweeted;

    private String relativeTimeStamp;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Entities getEntities() {
        return entities;
    }

    public void setEntities(Entities entities) {
        this.entities = entities;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public String getRelativeTimeStamp() {
        return relativeTimeStamp;
    }

    public void setRelativeTimeStamp(String relativeTimeStamp) {
        this.relativeTimeStamp = relativeTimeStamp;
    }

    public boolean hasImage() {
        if (getEntities() != null && getEntities().getMedia() != null && getEntities().getMedia().size() > 0) {
            return (getEntities().getMedia().get(0).getMediaUrl() != null && !TextUtils.isEmpty(getEntities().getMedia().get(0).getMediaUrl()));
        }
        return false;
    }

    public static List<Tweet> parseTweetListFromJson(JSONArray jsonArray) {
        List<Tweet> tweets = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {

            // Deserialize json into object fields
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Tweet tweet = parseTweetFromJson(jsonObject);
                tweets.add(tweet);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        // Return new object
        return tweets;
    }


    public static Tweet parseTweetFromJson(JSONObject jsonObject) {
        Tweet tweet = null;

        try {
            tweet = new Tweet();

            tweet.id = jsonObject.optLong("id");

            if (tweet.id < TweetApplication.getCurrMinTweetId()) {
                TweetApplication.setCurrMinTweetId(tweet.id);
            } else if (tweet.id > TweetApplication.getCurrMaxTweetId()) {
                TweetApplication.setCurrMaxTweetId(tweet.id);
            }

            // for relative timestamp
            tweet.createdAt = jsonObject.optString("created_at");
            tweet.setRelativeTimeStamp(MiscUtils.getRelativeTimeAgo(tweet.createdAt));

            // title
            tweet.text = jsonObject.optString("text");

            tweet.retweeted = jsonObject.optBoolean("retweeted");
            tweet.favorited = jsonObject.optBoolean("favorited");
            tweet.retweetCount = jsonObject.optInt("retweet_count");
            tweet.favoriteCount = jsonObject.optInt("favorite_count");

            JSONObject user = jsonObject.optJSONObject("user");
            tweet.user = new User();
            // user name
            tweet.user.setName(user.optString("name"));
            // used for handle @screen_name
            tweet.user.setScreenName(user.optString("screen_name"));
            //tweet.user.setUrl(user.optString("url"));

            // avatar image
            tweet.user.setProfileImageUrl(user.optString("profile_image_url"));

            // extract media urls -->  main image
            JSONObject entitiesObj = jsonObject.optJSONObject("entities");
            JSONArray mediaArray = entitiesObj.optJSONArray("media");
            if (mediaArray != null) {
                Entities entities = new Entities();
                List<Medium> mediaList = new ArrayList<>();
                for (int j = 0; j < mediaArray.length(); j++) {
                    JSONObject mediaObject = mediaArray.optJSONObject(j);

                    Medium media = new Medium();
                    media.setMediaUrl(mediaObject.optString("media_url"));
                    mediaList.add(media);
                }
                entities.setMedia(mediaList);
                tweet.setEntities(entities);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return tweet;
    }
}
