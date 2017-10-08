package com.twitter.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.twitter.client.activities.UserProfileActivity;
import com.twitter.client.adapters.TweetFragmentPagerAdapter;
import com.twitter.client.fragments.ComposeDialogFragment;
import com.twitter.client.fragments.HomeTimeLineFragment;
import com.twitter.client.storage.models.Tweet;
import com.twitter.client.transformations.CircularTransformation;

import org.parceler.Parcels;

public class TweetListActivity extends AppCompatActivity implements ComposeDialogFragment.TweetPostCompletionListener {
    public static String TAG = TweetListActivity.class.getSimpleName();

    private TabLayout tabLayout;
    private TweetFragmentPagerAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.ic_tweety_toolbar);
        setSupportActionBar(toolbar);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        fragmentPagerAdapter = new TweetFragmentPagerAdapter(getSupportFragmentManager(), TweetListActivity.this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(fragmentPagerAdapter);

        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Support implicit intent
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                showComposeDialogFragment(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_list, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_view_profile);
        Glide.with(this)
                .load(Uri.parse(TweetApplication.getLoggedInUser().getProfileImageUrl()))
                .bitmapTransform(new CircularTransformation(this))
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        menuItem.setIcon(resource);
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_compose) {
            // in case of implicit intent we send intent otherwise null
            showComposeDialogFragment(null);
            return true;
        }
        if (id == R.id.action_view_profile) {
            showUserProfileActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCompleted(Tweet createdTweet) {
        int position = tabLayout.getSelectedTabPosition();
        Fragment fragment = fragmentPagerAdapter.getItem(tabLayout.getSelectedTabPosition());
        if (fragment != null) {
            switch (position) {
                case 0:
                    ((HomeTimeLineFragment) fragment).addNewData(createdTweet);
                    break;
            }
        }
    }

    @Override
    public void onPostFailure(Throwable throwable) {
        Log.e(TAG, "onPostFailure:: Failed to post tweet: " + throwable.getMessage(), throwable);
        Toast.makeText(this, "Failed to post tweet. Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void showUserProfileActivity() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.ARGS_SELECTED_USER, Parcels.wrap(TweetApplication.getLoggedInUser()));
        startActivity(intent);
    }

    private void showComposeDialogFragment(Intent intent) {
        ComposeDialogFragment dialogFragment = ComposeDialogFragment.newInstance("Compose new tweet", this, intent);
        dialogFragment.show(getFragmentManager(), "Compose");
    }
}
