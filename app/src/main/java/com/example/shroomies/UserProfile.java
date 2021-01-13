package com.example.shroomies;

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
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDataref;
    private DatabaseReference mRootref;

    final String userUid =  mAuth.getInstance().getCurrentUser().getUid();

    String profileid;
    private RecyclerView recyclerView;
    private PhotosAdapter photosAdapter;
    private List<Post> postList;

    FragmentManager fm;
    FragmentTransaction ft;

   View v;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_user_profile, container, false);
        editProfile = v.findViewById(R.id.edit_profile_button_my_profile);
        profileImage = v.findViewById(R.id.profile_image);
        viewUsername = v.findViewById(R.id.view_username);
        viewBio = v.findViewById(R.id.view_bio);
        numPosts = v.findViewById(R.id.number_posts);
//        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
//        profileid = prefs.getString("profileid", "none");

        posts = v.findViewById(R.id.number_posts);
        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        photosAdapter = new PhotosAdapter(getContext(), postList);
        recyclerView.setAdapter(photosAdapter);
        recyclerView.setVisibility(View.VISIBLE);

        //getNumPosts();
        getPosts();


        mRootref = mDataref.getInstance().getReference();
        mRootref.child("Users").child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                viewUsername.setText(user.getName());
                viewBio.setText(user.getBio());
                String url = snapshot.child("image").getValue(String.class);
                GlideApp.with(getActivity().getApplicationContext())
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                        .into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }

//    private void getNumPosts(){
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                int i=0;
//                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
//                    Post post = snapshot.getValue(Post.class);
//                    if(post.getUserID().equals(profileid)){
//                        i++;
//                    }
//                }
//                posts.setText("" +i+ " Posts");
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void getPosts(){
        profileid =  mAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getUserID().equals(profileid)){
                        postList.add(post);
                    }
                    }
                Collections.reverse(postList);
                photosAdapter.notifyDataSetChanged();
          }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment(new EditProfile());
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