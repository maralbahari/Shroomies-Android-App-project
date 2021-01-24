package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Favorite extends Fragment {

    TabLayout tabLayoutFavorite;
    List<PersonalPostModel> personalPostModelList;
    List<Apartment> apartmentList;
    RecyclerView favAptRecyclerView;
    RecycleViewAdapterApartments favAptRecyclerAdapter;
    TextView favText;

    View v;
    FirebaseUser firebaseUser;
    ArrayList<String> apartmentIds;
    DatabaseReference rootRef;
    ArrayList<String> personalPostIds;
    PersonalPostRecyclerAdapter personalPostRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_my_favorite, container, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        favText = v.findViewById(R.id.FavNoAptText);
        favText.setVisibility(View.GONE);

    return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayoutFavorite = v.findViewById(R.id.tab_layout_Fav);
        favAptRecyclerView = v.findViewById(R.id.fav_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        favAptRecyclerView.setLayoutManager(linearLayoutManager);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getApartments();



        tabLayoutFavorite.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    getApartments();
                    favText.setVisibility(View.GONE);


                }
                else if(tab.getPosition()==1){
                    getPersonal();
                    favText.setVisibility(View.GONE);
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

    void getPersonal(){
        personalPostIds = new ArrayList<>();
        personalPostModelList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList , getActivity());
        favAptRecyclerView.setAdapter(personalPostRecyclerAdapter);
        final String CurrUserId = firebaseUser.getUid();
        rootRef.child("Favorite").child(CurrUserId).child("PersonalPost").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String postId = dataSnapshot.getKey();
                        personalPostIds.add(postId);
                    }

                    getPersonalPostFromId(personalPostIds);

                }
                else {
                    favText = v.findViewById(R.id.FavNoAptText);
                    favText.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_LONG);

            }
        });



    }



    void getApartments(){
        apartmentIds = new ArrayList<>();
        apartmentList = new ArrayList<>();
        favAptRecyclerAdapter = new RecycleViewAdapterApartments(apartmentList , getActivity());
        favAptRecyclerView.setAdapter(favAptRecyclerAdapter);
        final String CurrUserId = firebaseUser.getUid();
        rootRef.child("Favorite").child(CurrUserId).child("ApartmentPost").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for(DataSnapshot dataSnapshot
                    : snapshot.getChildren()){
                        //get  the apartment post ids
                        // once u have the id
                        // find the post in the apartment posts section
                        String apartmentId = dataSnapshot.getKey();
                        apartmentIds.add(apartmentId);
                    }
                    // once all apartments are obtained
                    //get the apartments
                    getApartmentPostFromId(apartmentIds);

                }
                else {
                    favText = v.findViewById(R.id.FavNoAptText);
                    favText.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_LONG);

            }


        });
    }

    private void getApartmentPostFromId(ArrayList<String> apartmentIds) {
        for (String id
        :apartmentIds){
            rootRef.child("postApartment").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Apartment favApt = snapshot.getValue(Apartment.class);
                        apartmentList.add(favApt);
                        // once the list has been populated
                        // notify the adapter
                        favAptRecyclerAdapter.notifyDataSetChanged();
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_LONG);

                }
            });
        }

    }
    private void getPersonalPostFromId(ArrayList<String> personalPostIds) {
    for (String id: personalPostIds){
        rootRef.child("postPersonal").child(id).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                PersonalPostModel favPersonal = snapshot.getValue(PersonalPostModel.class);
                personalPostModelList.add(favPersonal);
                personalPostRecyclerAdapter.notifyDataSetChanged();


            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_LONG);

            }
        });
    }

    }



}