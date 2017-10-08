package com.twitter.client.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.twitter.client.fragments.HomeTimeLineFragment;
import com.twitter.client.fragments.MentionsTimeLineFragment;

/**
 * @author tejalpar
 */
public class TweetFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Home", "Mentions" };

    private Context context;
    private HomeTimeLineFragment homeTimeLineFragment;
    private MentionsTimeLineFragment mentionsTimeLineFragment;

    public TweetFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                if (mentionsTimeLineFragment == null) {
                    mentionsTimeLineFragment = MentionsTimeLineFragment.newInstance();
                }
                return mentionsTimeLineFragment;

            case 0:
            default:
                if (homeTimeLineFragment == null) {
                    homeTimeLineFragment = HomeTimeLineFragment.newInstance();
                }
                return homeTimeLineFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
