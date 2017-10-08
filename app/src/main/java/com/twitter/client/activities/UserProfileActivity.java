package com.twitter.client.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.twitter.client.R;
import com.twitter.client.adapters.ProfileFragmentPagerAdapter;
import com.twitter.client.storage.models.User;
import com.twitter.client.transformations.CircularTransformation;

import org.parceler.Parcels;

public class UserProfileActivity extends AppCompatActivity {
    public static String TAG = UserProfileActivity.class.getSimpleName();
    public static String ARGS_SELECTED_USER = "SELECTED_USER";

    private User selectedUser;

    private TextView handleText;
    private TextView taglineText;
    private TextView screenNameText;
    private TextView followingCountText;
    private TextView followersCountText;
    private ImageView profileBgImage;
    private FloatingActionButton fabProfileImage;

    private TabLayout tabLayout;
    private ProfileFragmentPagerAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        selectedUser = Parcels.unwrap(getIntent().getParcelableExtra(ARGS_SELECTED_USER));

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        fragmentPagerAdapter = new ProfileFragmentPagerAdapter(getSupportFragmentManager(), UserProfileActivity.this, selectedUser);
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(fragmentPagerAdapter);

        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        handleText = (TextView) findViewById(R.id.handle_text);
        taglineText = (TextView) findViewById(R.id.tagline_text);
        screenNameText = (TextView) findViewById(R.id.screen_name_text);
        followingCountText = (TextView) findViewById(R.id.following_count_text);
        followersCountText = (TextView) findViewById(R.id.followers_count_text);
        profileBgImage = (ImageView) findViewById(R.id.profile_bg_image);
        fabProfileImage = (FloatingActionButton) findViewById(R.id.fab_profile_image);

        bindDataToView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_tweet_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindDataToView() {
        if (selectedUser != null) {

            handleText.setText(selectedUser.getUserHandle());
            taglineText.setText(selectedUser.getDescription());
            screenNameText.setText(selectedUser.getScreenName());
            followersCountText.setText(String.valueOf(selectedUser.getFollowersCount()));
            followingCountText.setText(String.valueOf(selectedUser.getFollowingCount()));

            // profile
            Glide.with(fabProfileImage.getContext())
                    .load(Uri.parse(selectedUser.getProfileImageUrl()))
                    .bitmapTransform(new CircularTransformation(fabProfileImage.getContext()))
                    .placeholder(R.drawable.placeholder_image)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(final GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fabProfileImage.setImageDrawable(resource);
                                }
                            });
                        }
                    });

            // background
            Glide.with(this)
                    .load(Uri.parse(selectedUser.getProfileBannerUrl()))
                    .placeholder(R.drawable.placeholder_image)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(final GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    profileBgImage.setImageDrawable(resource);
                                }
                            });
                        }
                    });
        }
    }
}
