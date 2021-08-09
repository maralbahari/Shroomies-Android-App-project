package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class UserProfile extends Fragment {
    private Button editProfile;
    private TextView viewUsername;
    private TextView viewBio;
    private TextView numberPosts;
    private ImageView profileImage;
    private FirebaseUser userRef;
    private TabLayout profileTab;

    private FirebaseAuth authRef;
    private FirebaseDatabase dataRef;
    private DatabaseReference rootRef;
    FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();

    private ImageButton sendUserMessage;
    TabItem viewApartment, viewPersonal;
    String profileID;
    private RecyclerView recyclerView;
    private RecycleViewAdapterApartments apartmentAdapter;
    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    private List<Apartment> apartmentPostList;
    private List<PersonalPostModel> personalPostList;

    FragmentManager fm;
    FragmentTransaction ft;
    View v;

    final String userUid =  authRef.getInstance().getCurrentUser().getUid();
    public static final int APARTMENT_PER_PAGINATION =10;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_user_profile, container, false);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize views
        rootRef = FirebaseDatabase.getInstance().getReference();
        authRef = FirebaseAuth.getInstance();
        editProfile = v.findViewById(R.id.edit_profile_button);
        profileImage = v.findViewById(R.id.user_profile_image_view);
        viewUsername = v.findViewById(R.id.user_profile_view_username);
        viewBio = v.findViewById(R.id.user_profile_view_bio);
        profileTab = v.findViewById(R.id.user_profile_tab_layout);
        viewApartment = v.findViewById(R.id.user_profile_tab_button_apartment);
        viewPersonal = v.findViewById(R.id.user_profile_tab_button_personal);

        sendUserMessage = v.findViewById(R.id.send_user_message);
        sendUserMessage.setVisibility(View.GONE);

        numberPosts = v.findViewById(R.id.number_posts);

        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);


        getUserInfo();
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


    private void getNumPosts() {
        Query query = mDocRef.collection("postApartment").whereEqualTo("userID", userUid);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                final long numberOfPosts = value.size();
                Query query = mDocRef.collection("postPersonal").whereEqualTo("userID", userUid);
                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        numberPosts.setText(Long.toString(numberOfPosts + value.size()));
                    }
                });
            }
        });
    }

    private void getApartmentPosts() {
        apartmentPostList = new ArrayList<>();
        apartmentAdapter = new RecycleViewAdapterApartments(apartmentPostList , getActivity(), userUid,false);
        recyclerView.setAdapter(apartmentAdapter);

        Query query = mDocRef.collection("postApartment").orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("userID", userUid).limit(APARTMENT_PER_PAGINATION);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Apartment apartmentPosts = document.toObject(Apartment.class);
                    apartmentPosts.setApartmentID(document.getId());
                    apartmentPostList.add(apartmentPosts);
                }
                apartmentAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getPersonalPosts() {

        personalPostList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostList, getActivity(), userUid, false);
        recyclerView.setAdapter(personalPostRecyclerAdapter);

        final String userUid = authRef.getInstance().getCurrentUser().getUid();
        Query query = mDocRef.collection("postPersonal").orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("userID", userUid).limit(APARTMENT_PER_PAGINATION);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    PersonalPostModel personalPosts = document.toObject(PersonalPostModel.class);
                    personalPosts.setId(document.getId());
                    personalPostList.add(personalPosts);
                }
                personalPostRecyclerAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void  getUserInfo(){
        rootRef = dataRef.getInstance().getReference();
        rootRef.child("Users").child(authRef.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    final User user = snapshot.getValue(User.class);
                    viewUsername.setText(user.getName());
                    viewBio.setText(user.getBio());
                    String url = snapshot.child("image").getValue(String.class);
                    if (user.getImage() != null) {
                        GlideApp.with(getActivity().getApplicationContext())
                                .load(url)
                                .circleCrop()
                                .into(profileImage);

                        profileImage.setPadding(3,3,3,3);
                    }

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

}