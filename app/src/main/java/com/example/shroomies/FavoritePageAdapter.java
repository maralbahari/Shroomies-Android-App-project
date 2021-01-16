package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FavoritePageAdapter extends FragmentPagerAdapter {

    int tabCount;

    public FavoritePageAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        tabCount = behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new FavAptTab();
            case 1: return new FavTabPersonal();
            default: return null;

        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
