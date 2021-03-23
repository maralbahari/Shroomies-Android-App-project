package com.example.shroomies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ViewProfile extends Fragment {
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
    private ImageButton sendMessage;
    TabItem apartmentTab, personalTab;
    String profileID;
    User receiverUser;

    public static final int APARTMENT_PER_PAGINATION =10;

    private RecyclerView recyclerView;
    private RecycleViewAdapterApartments apartmentAdapter;
    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    private List<Apartment> apartmentPostList;
    private List<PersonalPostModel> personalPostList;
    FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();

    View v;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_user_profile, container, false);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootRef = FirebaseDatabase.getInstance().getReference();
        authRef = FirebaseAuth.getInstance();
        editProfile = v.findViewById(R.id.edit_profile_button);
        profileImage = v.findViewById(R.id.user_profile_image_view);
        viewUsername = v.findViewById(R.id.user_profile_view_username);
        viewBio = v.findViewById(R.id.user_profile_view_bio);
        profileTab = v.findViewById(R.id.user_profile_tab_layout);
        apartmentTab = v.findViewById(R.id.user_profile_tab_button_apartment);
        personalTab = v.findViewById(R.id.user_profile_tab_button_personal);
        sendMessage = v.findViewById(R.id.send_user_message);
        numberPosts = v.findViewById(R.id.number_posts);

        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        profileID = getArguments().getString("profileID");

        editProfile.setVisibility(View.GONE);
        sendMessage.setVisibility(View.VISIBLE);


        getUserInfo();
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

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(profileID);

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        receiverUser = new User();
                        receiverUser = snapshot.getValue(User.class);
                        Intent intent = new Intent(getActivity(), ChattingActivity.class);
                        intent.putExtra("USERID", receiverUser.getID());
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

     private void  getUserInfo(){
        rootRef = dataRef.getInstance().getReference();
        rootRef.child("Users").child(profileID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    viewUsername.setText(user.getName());
                    viewBio.setText(user.getBio());
                    String url = snapshot.child("image").getValue(String.class);
                    if (user.getImage() != null) {
                        GlideApp.with(getActivity().getApplicationContext())
                                .load(url)
                                .circleCrop()
                                .into(profileImage);
                        profileImage.setPadding(4, 4, 4, 4);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}


    private void getNumPosts(){
        Query query = mDocRef.collection("postApartment").whereEqualTo("userID", profileID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                final long numberOfPosts = task.getResult().size();
                Query query = mDocRef.collection("postPersonal").whereEqualTo("userID", profileID);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        numberPosts.setText(Long.toString(numberOfPosts + task.getResult().size()));
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
}

    private void getApartmentPosts() {
        apartmentPostList = new ArrayList<>();
        apartmentAdapter = new RecycleViewAdapterApartments(apartmentPostList , getActivity(), profileID, false);
        recyclerView.setAdapter(apartmentAdapter);

        Query query = mDocRef.collection("postApartment").orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("userID", profileID).limit(APARTMENT_PER_PAGINATION);
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

    private void getPersonalPosts(){
        personalPostList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostList, getActivity(), profileID, false);
        recyclerView.setAdapter(personalPostRecyclerAdapter);

        Query query = mDocRef.collection("postPersonal").orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("userID", profileID).limit(APARTMENT_PER_PAGINATION);
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

//    use this when for viewing profile
//        viewUsername.setOnClickListener(new View.OnClickListener() {
//            @Override
//        public void onClick(View v) {
//                ViewProfile viewProfile = new ViewProfile();
//                Bundle bundle = new Bundle();
//                bundle.putString("profileID", user.getID());
//                viewProfile.setArguments(bundle);
//                getFragment(viewProfile);
//            }
//        });

}
