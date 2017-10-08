package com.twitter.client.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.twitter.client.fragments.UserTimeLineFragment;
import com.twitter.client.storage.models.User;

/**
 * @author tejalpar
 */
public class ProfileFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Tweet", "Fav"};

    private User selectedUser;
    private Context context;
    private UserTimeLineFragment userTimeLineFragment;

    public ProfileFragmentPagerAdapter(FragmentManager fm, Context context, User selectedUser) {
        super(fm);
        this.context = context;
        this.selectedUser = selectedUser;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new Fragment();

            case 0:
            default:
                if (userTimeLineFragment == null) {
                    userTimeLineFragment = UserTimeLineFragment.newInstance(selectedUser.getScreenName());
                }
                return userTimeLineFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
