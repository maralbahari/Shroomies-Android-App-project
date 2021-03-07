package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.rpc.context.AttributeContext;

import java.util.ArrayList;


public class Request extends Fragment {
   private View v;
   private RecyclerView requestRecyclerView;
   private FirebaseAuth mAuth;
   private DatabaseReference rootRef;
   private ArrayList<User> senderUsers;
   private ArrayList<String> senderIDs;

   private ArrayList<String> receiverIDs;
   private ArrayList<User> receiverUsers;
   private TabLayout requestTab;
   private ShroomiesApartment apartment;
   private ValueEventListener invitationsListener;
   private ValueEventListener requestsListener;
   private ValueEventListener apartmentListener;
   private RecyclerView invitationRecyclerView;
    private RequestAdapter requestAdapter;
    private RequestAdapter invitationAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_my_requests, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        getApartmentDetailsOfCurrentUser();
    return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestRecyclerView = v.findViewById(R.id.request_recyclerview);
        requestTab = v.findViewById(R.id.request_tablayout);
        invitationRecyclerView=v.findViewById(R.id.invitation_recyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        requestRecyclerView.setHasFixedSize(true);
        requestRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        invitationRecyclerView.setLayoutManager(linearLayoutManager1);
        invitationRecyclerView.setHasFixedSize(true);

        senderUsers=new ArrayList<>();
        invitationAdapter= new RequestAdapter(getContext(),senderUsers,false,apartment);
        invitationRecyclerView.setAdapter(invitationAdapter);


        receiverUsers=new ArrayList<>();
        requestAdapter = new RequestAdapter(getActivity() , receiverUsers ,true ,apartment);
        requestRecyclerView.setAdapter(requestAdapter);


        requestTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    requestTab.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                    invitationRecyclerView.setVisibility(View.VISIBLE);
                    requestRecyclerView.setVisibility(View.GONE);
                    getSenderId();
                }
                else if(tab.getPosition()==1){
                    requestTab.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    invitationRecyclerView.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.VISIBLE);
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

    private void getApartmentDetailsOfCurrentUser() {
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    final String apartmentID=snapshot.getValue().toString();
                    apartmentListener=rootRef.child("apartments").child(apartmentID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                apartment=snapshot.getValue(ShroomiesApartment.class);
                                getSenderId();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getSenderId(){
        senderIDs=new ArrayList<>();
        invitationsListener=rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).orderByChild("requestType").equalTo("received").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds:snapshot.getChildren()){
                        senderIDs.add(ds.getKey());
                    }
                    getSenderDetails(senderIDs);
                }else{
                    senderUsers=new ArrayList<>();
                    invitationAdapter= new RequestAdapter(getContext(),senderUsers,false,apartment);
                    invitationRecyclerView.setAdapter(invitationAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getSenderDetails(final ArrayList<String> senderIDs){
        senderUsers=new ArrayList<>();
        invitationAdapter= new RequestAdapter(getContext(),senderUsers,false,apartment);
       invitationRecyclerView.setAdapter(invitationAdapter);
     for(String id: senderIDs){
         rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
             if(snapshot.exists()){
                 User user= snapshot.getValue(User.class);
                 senderUsers.add(user);
             }
             invitationAdapter.notifyDataSetChanged();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
     }
    }
    private void getReceiverID(){
        receiverIDs= new ArrayList<>();
        rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).orderByChild("requestType").equalTo("sent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds:snapshot.getChildren()){
                        receiverIDs.add(ds.getKey());
                    }
                    getReceiverDetails(receiverIDs);
//                    Toast.makeText(getContext(),"req ids:"+receiverIDs.size(),Toast.LENGTH_SHORT).show();

                }else{
                    receiverUsers=new ArrayList<>();
                    requestAdapter= new RequestAdapter(getContext(),receiverUsers,true,apartment);
                    requestRecyclerView.setAdapter(requestAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getReceiverDetails(final ArrayList<String> receiverIDs){
        receiverUsers=new ArrayList<>();
        requestAdapter = new RequestAdapter(getActivity() , receiverUsers ,true ,apartment);
        requestRecyclerView.setAdapter(requestAdapter);
        for(String id: receiverIDs){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootRef.removeEventListener(invitationsListener);
        rootRef.removeEventListener(requestsListener);
        rootRef.removeEventListener(apartmentListener);
    }
}