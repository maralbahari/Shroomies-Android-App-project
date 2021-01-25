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

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Request extends Fragment {
   View v;
   RecyclerView requestRecyvlerView;
   FirebaseAuth mAuth;
   DatabaseReference rootRef;
   private ArrayList<User> senderUsers;
   private ArrayList<String> senderIDs;
   private RequestAdapter requestAdapter;
   private ArrayList<String> receiverIDs;
   private ArrayList<User> receiverUsers;
   TabLayout requestTab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_my_requests, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
    return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestRecyvlerView = v.findViewById(R.id.request_recyclerview);
        requestTab = v.findViewById(R.id.request_tablayout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        requestRecyvlerView.setHasFixedSize(true);
        requestRecyvlerView.setLayoutManager(linearLayoutManager);
        getSenderId();
        requestTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    getSenderId();
                }
                else if(tab.getPosition()==1){
                    getReceiverID();
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


    private void getSenderId(){
        senderIDs=new ArrayList<>();
        rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).orderByChild("requestType").equalTo("received").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds:snapshot.getChildren()){
                        senderIDs.add(ds.getKey());
                        getSenderDetails(senderIDs);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getSenderDetails(final ArrayList<String> senderIDs){
        senderUsers=new ArrayList<>();
     requestAdapter= new RequestAdapter(getContext(),senderUsers,false);
     requestRecyvlerView.setAdapter(requestAdapter);
     for(String id: senderIDs){
         rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
             senderUsers.clear();
             if(snapshot.exists()){
                 User user= snapshot.getValue(User.class);
                 senderUsers.add(user);
             }
             requestAdapter.notifyDataSetChanged();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
     }
    }
    private void getReceiverID(){
       receiverIDs=new ArrayList<>();
        rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).orderByChild("requestType").equalTo("sent").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds:snapshot.getChildren()){
                        receiverIDs.add(ds.getKey());
                        getReceiverDetails(receiverIDs);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getReceiverDetails(final ArrayList<String> receiverIDs){
        receiverUsers=new ArrayList<>();
        requestAdapter= new RequestAdapter(getContext(),receiverUsers,true);
        requestRecyvlerView.setAdapter(requestAdapter);
        for(String id: receiverIDs){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    receiverUsers.clear();
                    if(snapshot.exists()){
                        User user= snapshot.getValue(User.class);
                        receiverUsers.add(user);
                    }
                    requestAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}