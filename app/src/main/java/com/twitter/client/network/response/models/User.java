
package com.twitter.client.network.response.models;


import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

    //@SerializedName("id")
    private long id;

    //@SerializedName("name")
    private String name;

    //@SerializedName("screen_name")
    private String screenName;

    //@SerializedName("profile_image_url")
    private String profileImageUrl;

    //@SerializedName("profile_image_url_https")
    private String profileImageUrlHttps;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrlHttps() {
        return profileImageUrlHttps;
    }

    public void setProfileImageUrlHttps(String profileImageUrlHttps) {
        this.profileImageUrlHttps = profileImageUrlHttps;
    }

    public String getUserHandle() {
        return "@" + screenName;
    }

    public static User parseUserInfoFromJson(JSONObject responseObject) {
        User user = new User();
        user.id = responseObject.optLong("id");
        user.name = responseObject.optString("name");
        user.screenName = responseObject.optString("screen_name");
        user.profileImageUrl = responseObject.optString("profile_image_url");

        return user;
    }

}
