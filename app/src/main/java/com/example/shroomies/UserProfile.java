package com.example.shroomies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UserProfile extends Fragment {
    private Button editProfile;
    private TextView viewUsername;
    private TextView viewBio;
    private TextView numPosts;
    private TextView posts;
    private ImageView profileImage;
    private FirebaseUser mUser;
    private TabLayout profileTab;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDataref;
    private DatabaseReference rootRef;
    TabItem apartment, personal;
    String profileid;
    private RecyclerView recyclerView;
    private RecycleViewAdapterApartments apartmentAdapter;
    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    private List<Apartment> apartmentPostList;
    private List<PersonalPostModel> postListPersonal;
    FragmentManager fm;
    FragmentTransaction ft;
    View v;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_user_profile, container, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        getUserInfo();
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // intialize views
        editProfile = v.findViewById(R.id.edit_profile_button_my_profile);
        profileImage = v.findViewById(R.id.profile_image);
        viewUsername = v.findViewById(R.id.view_username);
        viewBio = v.findViewById(R.id.view_bio);
        numPosts = v.findViewById(R.id.number_posts);
        profileTab = v.findViewById(R.id.tab_layout_user_profile);
        apartment = v.findViewById(R.id.tab_button_apartment);
        personal = v.findViewById(R.id.tab_button_personal);

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        posts = v.findViewById(R.id.number_posts);


        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        // initially get the apartments
        getApartmentPosts();


        getNumPosts();



        profileTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==1){
                    getPersonalPosts();
                }else if (tab.getPosition()==0){
                    getApartmentPosts();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });





        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment(new EditProfile());
            }
        });
    }



    private void getNumPosts(){
        rootRef.child("postApartment").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if(post.getUserID().equals(profileid)){
                        i++;
                    }

                }
               posts.setText(Integer.toString(i));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getApartmentPosts() {
        apartmentPostList = new ArrayList<>();
        apartmentAdapter = new RecycleViewAdapterApartments(apartmentPostList , getActivity());
        recyclerView.setAdapter(apartmentAdapter);

        profileid = mAuth.getInstance().getCurrentUser().getUid();
        rootRef.child("postApartment").orderByChild("userID").equalTo(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot:
                            snapshot.getChildren()){
                        Apartment apartmentPost = dataSnapshot.getValue(Apartment.class);
                        apartmentPostList.add(apartmentPost);
                    }
                    Collections.reverse(apartmentPostList);
                    apartmentAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getPersonalPosts(){

        postListPersonal = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(postListPersonal , getActivity());
        recyclerView.setAdapter(personalPostRecyclerAdapter);
        rootRef.child("postPersonal").orderByChild("userID").equalTo(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        PersonalPostModel post = dataSnapshot.getValue(PersonalPostModel.class);
                        postListPersonal.add(post);
                    }
                    Collections.reverse(postListPersonal);
                    personalPostRecyclerAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    private void getFragment (Fragment fragment) {
        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }


   void  getUserInfo(){
       rootRef = mDataref.getInstance().getReference();
       rootRef.child("Users").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists()) {
                   User user = snapshot.getValue(User.class);
                   viewUsername.setText(user.getName());
                   viewBio.setText(user.getBio());
                   String url = snapshot.child("image").getValue(String.class);
                   if (user.getImage() != null) {
                       GlideApp.with(getActivity().getApplicationContext())
                               .load(url)
                               .circleCrop()
                               .into(profileImage);

                       profileImage.setPadding(4,4,4,4);
                   }

               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

}