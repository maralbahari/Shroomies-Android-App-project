package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class UserProfile extends Fragment {
    private TextView textViewName;
    private TextView viewBio;
    private TextView numberPosts;
    private ImageView profileImage;
    private TextView textViewUsername;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private FirebaseFirestore mDocRef;
    private RecyclerView recyclerView;
    private RecycleViewAdapterApartments apartmentAdapter;
    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    private List<Apartment> apartmentPostList;
    private List<PersonalPostModel> personalPostList;
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 2;
    private View v;
    private User user;
    public static final int APARTMENT_PER_PAGINATION =10;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_user_profile, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        mDocRef= FirebaseFirestore.getInstance();
        user=new User();
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize views
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        Button editProfile = v.findViewById(R.id.edit_profile_button);
        profileImage = v.findViewById(R.id.user_profile_image_view);
        textViewName = v.findViewById(R.id.user_profile_text_view_name);
        viewBio = v.findViewById(R.id.user_profile_view_bio);
        textViewUsername=v.findViewById(R.id.user_profile_username_text_view);
        TabLayout profileTab = v.findViewById(R.id.user_profile_tab_layout);
        numberPosts = v.findViewById(R.id.number_posts);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        FirebaseUser firebaseUser= mAuth.getCurrentUser();
        if (firebaseUser!=null) {
            String userUid=firebaseUser.getUid();
            getUserInfo(userUid);
            getApartmentPosts(userUid);
            getNumPosts(userUid);
            profileTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(tab.getPosition()==1){
                        getPersonalPosts(userUid);
                    }else if (tab.getPosition()==0){
                        getApartmentPosts(userUid);
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




        editProfile.setOnClickListener(v -> {
            EditProfile editProfileDialog=new EditProfile();
            editProfileDialog.setTargetFragment(UserProfile.this,DIALOG_FRAGMENT_REQUEST_CODE);
            editProfileDialog.show(getParentFragmentManager() ,null);
        });
    }


    private void getNumPosts(String userUid) {
        Query query = mDocRef.collection(Config.APARTMENT_POST).whereEqualTo(Config.userID, userUid);
        query.addSnapshotListener((value, error) -> {
            if (value!=null) {
                final long numberOfPosts = value.size();
                Query query1 = mDocRef.collection(Config.PERSONAL_POST).whereEqualTo(Config.userID, userUid);
                query1.addSnapshotListener((value1, error1) -> {
                    if (value1 != null) {
                        numberPosts.setText(Long.toString(numberOfPosts + value1.size()));
                    }
                });
            }
        });
    }

    private void getApartmentPosts(String userUid) {
        apartmentPostList = new ArrayList<>();
        apartmentAdapter = new RecycleViewAdapterApartments(apartmentPostList, getContext(), false, true);
        recyclerView.setAdapter(apartmentAdapter);
        Query query = mDocRef.collection(Config.APARTMENT_POST).orderBy(Config.TIME_STAMP, Query.Direction.DESCENDING).whereEqualTo(Config.userID, userUid).limit(APARTMENT_PER_PAGINATION);
        query.get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot document : task.getResult()) {
                Apartment apartmentPosts = document.toObject(Apartment.class);
                apartmentPosts.setApartmentID(document.getId());
                apartmentPostList.add(apartmentPosts);
            }
            apartmentAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {

        });
    }

    private void getPersonalPosts(String userUid) {

        personalPostList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostList, getActivity(), false, true);
        recyclerView.setAdapter(personalPostRecyclerAdapter);
        Query query = mDocRef.collection(Config.PERSONAL_POST).orderBy(Config.TIME_STAMP, Query.Direction.DESCENDING).whereEqualTo(Config.userID, userUid).limit(APARTMENT_PER_PAGINATION);
        query.get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot document : task.getResult()) {
                PersonalPostModel personalPosts = document.toObject(PersonalPostModel.class);
                personalPosts.setPostID(document.getId());
                personalPostList.add(personalPosts);
            }
            personalPostRecyclerAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {

        });
    }

    private void  getUserInfo(String userUid){
        rootRef.child(Config.users).child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                     user = snapshot.getValue(User.class);
                     if (user!=null) {
                         textViewName.setText(user.getName());
                         textViewUsername.setText(user.getUsername());
                         if (!user.getBio().equals("")) {
                             viewBio.setText(user.getBio());
                         }
                         if (user.getImage() != null) {
                             GlideApp.with(getActivity().getApplicationContext())
                                     .load(user.getImage())
                                     .transform(new CircleCrop())
                                     .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                                     .into(profileImage);
                         }
                     }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}