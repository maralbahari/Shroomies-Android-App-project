package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;


public class Favorite extends Fragment {

    TabLayout tabFav;
    TabItem tabApt, tabPost;
    ViewPager VPfav;


   View v;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_my_favorite, container, false);
    return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabFav = (TabLayout) v.findViewById(R.id.tab_layout_Fav);
        tabApt = (TabItem) v.findViewById(R.id.tab_button_fav_apartment);
        tabPost = (TabItem) v.findViewById(R.id.tab_button_fav_personal);
        VPfav = (ViewPager) v.findViewById(R.id.VP_fav);
        final FavoritePageAdapter pageAdapter = new FavoritePageAdapter(getChildFragmentManager(), tabFav.getTabCount());
        VPfav.setAdapter(pageAdapter);

        tabFav.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                VPfav.setCurrentItem(tab.getPosition());

                if ((tab.getPosition() == 0 )|| (tab.getPosition() == 1))
                    pageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        VPfav.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabFav));


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


}