package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Favorite extends Fragment {

    TabLayout tabFav;
    TabItem tabApt, tabPost;
    ArrayList<PersonalPostModel> personalPostModelList;
    ArrayList <Apartment> apartmentList;
    RecyclerView favRecyclerView;
    PersonalPostRecyclerAdapter favPersonalPostRecycler;


   View v;
     PersonalPostRecyclerAdapter personalPostRecyclerAdapter;

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

        favRecyclerView = v.findViewById(R.id.fav_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        favRecyclerView.setLayoutManager(linearLayoutManager);

        tabFav.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){

                }
                else if(tab.getPosition()==1){
                    retriveFavPersonalPost();
                    personalPostModelList = new ArrayList<>();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });



    }

    private void retriveFavPersonalPost() {
        personalPostModelList = new ArrayList<>();
        favPersonalPostRecycler = new PersonalPostRecyclerAdapter(personalPostModelList, getContext());
        favRecyclerView.setAdapter(favPersonalPostRecycler);
        DatabaseReference myPersonalDatabaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference favPer = myPersonalDatabaseRef.child("Favorite").child("PersonalPost");
        favPer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                personalPostModelList.clear();
                if(snapshot.exists()){
                    for (DataSnapshot snapShot: snapshot.getChildren()){
                        PersonalPostModel favPersonal = snapShot.getValue(PersonalPostModel.class);
                        personalPostModelList.add(favPersonal);
                    }
                  favPersonalPostRecycler.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


}