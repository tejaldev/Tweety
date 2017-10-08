package com.twitter.client.storage.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.twitter.client.storage.TweetDatabase;

import org.json.JSONException;
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
@Parcel(analyze={User.class})
public class User extends BaseModel {

	@PrimaryKey
	@Column
	Long userId;

	// Define table fields
	@Column
	private String name;

    @Column
	private String screenName;

	@Column
	private String profileImageUrl;

    @Column
    private String profileBackgroundImageUrl;

    @Column
    private String profileBannerUrl;

    @Column
    private int followersCount;

    @Column
    private int followingCount;

    @Column
    private String description;


	public User() {
		super();
	}

	// Parse model from JSON
	public User(JSONObject jsonObject){
		super();

		try {
            this.userId = jsonObject.optLong("id");
            this.name = jsonObject.optString("name");
            this.screenName = jsonObject.optString("screen_name");
            this.description = jsonObject.optString("description");
            this.followingCount = jsonObject.optInt("friends_count");
            this.followersCount = jsonObject.optInt("followers_count");
            this.profileImageUrl = jsonObject.optString("profile_image_url");
            this.profileBannerUrl = jsonObject.optString("profile_banner_url");
            this.profileBackgroundImageUrl = jsonObject.optString("profile_background_image_url");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long id) {
        this.userId = id;
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

	public String getUserHandle() {
		return "@" + this.screenName;
	}

    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    public void setProfileBackgroundImageUrl(String profileBackgroundImageUrl) {
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    public void setProfileBannerUrl(String profileBannerUrl) {
        this.profileBannerUrl = profileBannerUrl;
    }

    // Helper method
	public static User parseUserInfoFromJson(JSONObject responseObject) {
        User user = new User();
        user.userId = responseObject.optLong("id");
        user.name = responseObject.optString("name");
        user.screenName = responseObject.optString("screen_name");
        user.description = responseObject.optString("description");
        user.followingCount = responseObject.optInt("friends_count");
        user.followersCount = responseObject.optInt("followers_count");
        user.profileImageUrl = responseObject.optString("profile_image_url");
        user.profileBannerUrl = responseObject.optString("profile_banner_url");
        user.profileBackgroundImageUrl = responseObject.optString("profile_background_image_url");

		return user;
	}

	/* The where class in this code below will be marked red until you first compile the project, since the code 
	 * for the SampleModel_Table class is generated at compile-time.
	 */
	
//	// Record Finders
//	public static Tweet byId(long id) {
//		return new Select().from(User.class).where(User_Table.id.eq(id)).querySingle();
//	}
}
