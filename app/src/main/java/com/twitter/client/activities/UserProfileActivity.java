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

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileActivity extends AppCompatActivity {
    public static String TAG = UserProfileActivity.class.getSimpleName();
    public static String ARGS_SELECTED_USER = "SELECTED_USER";

    private User selectedUser;

    @BindView(R.id.handle_text) protected TextView handleText;
    @BindView(R.id.tagline_text) protected TextView taglineText;
    @BindView(R.id.screen_name_text) protected TextView screenNameText;
    @BindView(R.id.following_count_text) protected TextView followingCountText;
    @BindView(R.id.followers_count_text) protected TextView followersCountText;
    @BindView(R.id.profile_bg_image) protected ImageView profileBgImage;
    @BindView(R.id.fab_profile_image) protected FloatingActionButton fabProfileImage;
    @BindView(R.id.sliding_tabs) protected TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        selectedUser = Parcels.unwrap(getIntent().getParcelableExtra(ARGS_SELECTED_USER));

        // Bind views
        ButterKnife.bind(this);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ProfileFragmentPagerAdapter fragmentPagerAdapter = new ProfileFragmentPagerAdapter(getSupportFragmentManager(), UserProfileActivity.this, selectedUser);
        ViewPager viewPager = (ViewPager) findViewById(R.id.container); // cannot bind local variables
        viewPager.setAdapter(fragmentPagerAdapter);

        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);

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
