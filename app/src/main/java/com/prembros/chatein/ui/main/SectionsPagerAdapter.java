package com.prembros.chatein.ui.main;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionsPagerAdapter extends FragmentPagerAdapter {

    SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return RequestsFragment.newInstance();
            case 1:
                return ChatsFragment.newInstance();
            case 2:
                return FriendsFragment.newInstance();
        }
        return null;
    }

    @Override public int getCount() {
        return 3;
    }

    @Nullable @Override public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
        }
        return super.getPageTitle(position);
    }
}