package com.example.shroomies;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Members extends DialogFragment {
    View v;
    Button addMember, leaveRoom;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    RecyclerView membersRecycler;
    String apartmentID = "";
    ArrayList<String> membersId;
    ArrayList<User> membersList;
    UserAdapter userAdapter;
    ArrayList<String> requestUsersIDs;
    TextView ownerName;
    ImageView ownerImage;
    ArrayList<User> getRequestUsersList;
    ImageView sadShroomie, stars;  // set visibility to gone if there are members in add members
    TextView noMemberTv ;           // same visibility to gone
   ShroomiesApartment apartment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.shroomie_members, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        return v;
    }



    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sadShroomie = view.findViewById(R.id.member_sad_shroomie);  // set visibility to gone if there are members
        stars = view.findViewById(R.id.member_star);                // set visibility to gone if there are members
        noMemberTv = view.findViewById(R.id.no_members_tv);         // set visibility to gone if there are members
        addMember=view.findViewById(R.id.add_shroomie_btn);
        leaveRoom=v.findViewById(R.id.leave_room_btn);
        ownerImage=v.findViewById(R.id.owner_image);
        ownerName=v.findViewById(R.id.owner_name);
        membersRecycler = v.findViewById(R.id.members_recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        membersRecycler.setHasFixedSize(true);
        membersRecycler.setLayoutManager(linearLayoutManager);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            getOwnerDetails(apartment.getOwnerID());
            if(apartment.getMembersID()!=null){
                getMemberDetail(apartment.getMembersID());
            }
        }
//        setOwnerDetails(ownerName,ownerImage,apartmentID);
        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddShroomieMember add=new AddShroomieMember();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("MEMBERID",membersId);
                add.setArguments(bundle);
                add.show(getParentFragmentManager(),"add member to apartment");
            }
        });


        leaveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveApartment();
            }
        });

    }

    private void getOwnerDetails(String ownerID) {
     rootRef.child("Users").child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot snapshot) {
             if(snapshot.exists()) {
                 User owner = snapshot.getValue(User.class);
                 ownerName.setText(owner.getName());
                                    if(!owner.getImage().isEmpty()){
                                        GlideApp.with(getContext())
                                                .load(owner.getImage())
                                                .fitCenter()
                                                .circleCrop()
                                                .into(ownerImage);
                                        ownerImage.setPadding(2,2,2,2);
             }
             }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError error) {

         }
     });
    }

    private void leaveApartment() {
        final CustomLoadingProgressBar customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(),"Leaving room...",R.raw.loading_animation);
        customLoadingProgressBar.show();
        rootRef.child("apartments").child(apartmentID).child("apartmentMembers").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").setValue(mAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    customLoadingProgressBar.dismiss();
                        Intent intent = new Intent(getContext(),MainActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        customLoadingProgressBar.dismiss();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                customLoadingProgressBar.dismiss();
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }
//    private void getMember(String apartmentID){
//        membersId = new ArrayList<>();
//
//        rootRef.child("apartments").child(apartmentID).child("apartmentMembers").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
//
//                    for (DataSnapshot sp : snapshot.getChildren()){
//
//                        membersId.add(sp.getValue().toString());
//
//
//
//                    }
//                    getMemberDetail(membersId);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }



    private void getMemberDetail(final ArrayList<String> membersId) {
        membersList = new ArrayList<>();
        userAdapter = new UserAdapter(membersList, getContext(),false);
        membersRecycler.setAdapter(userAdapter);
        for (String id: membersId){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        membersList.add(user);
                    }
                    userAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }


//    private void getUserRoomId(){
//        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    apartmentID=snapshot.getValue().toString();
//                    if(mAuth.getCurrentUser().getUid().equals(apartmentID)){
//                        leaveRoom.setVisibility(View.GONE);
//                        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if(snapshot.exists()){
//                                    User owner= snapshot.getValue(User.class);
//                                    ownerName.setText(owner.getName());
//                                    if(!owner.getImage().isEmpty()){
//                                        GlideApp.with(getContext())
//                                                .load(owner.getImage())
//                                                .fitCenter()
//                                                .circleCrop()
//                                                .into(ownerImage);
//                                        ownerImage.setPadding(2,2,2,2);
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//                    }
//                    getMember(apartmentID);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


}
