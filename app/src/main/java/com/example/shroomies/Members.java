package com.example.shroomies;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Members extends Fragment {
    View v;
    Button addMember, leaveRoom;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    RecyclerView membersRecycler;
    ArrayList<String> membersId;
    ArrayList<User> membersList;
    UserAdapter userAdapter;
    ArrayList<String> requestUsersIDs;
    TextView ownerName;
    ImageView ownerImage;
    ImageView msgOwner;
    ArrayList<User> getRequestUsersList;
    ImageView sadShroomie, stars;  // set visibility to gone if there are members in add members
    TextView noMemberTv ;           // same visibility to gone
   ShroomiesApartment apartment;
   private Collection newMemberLists;
   private ValueEventListener apartmentListener;
   private String isPartOfRoomID;
   private ValueEventListener roomIDListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.shroomie_members, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sadShroomie = view.findViewById(R.id.member_sad_shroomie);  // set visibility to gone if there are members
        stars = view.findViewById(R.id.member_star);                // set visibility to gone if there are members
        noMemberTv = view.findViewById(R.id.no_members_tv);         // set visibility to gone if there are members
        addMember=view.findViewById(R.id.add_shroomie_btn);
        leaveRoom=view.findViewById(R.id.leave_room_btn);
        ownerImage=view.findViewById(R.id.owner_image);
        ownerName=view.findViewById(R.id.owner_name);
        msgOwner=view.findViewById(R.id.msg_owner);
        membersRecycler = view.findViewById(R.id.members_recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        membersRecycler.setHasFixedSize(true);
        membersRecycler.setLayoutManager(linearLayoutManager);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            getOwnerDetails(apartment.getOwnerID());
            if(!mAuth.getCurrentUser().getUid().equals(apartment.getOwnerID())){
                leaveRoom.setVisibility(View.VISIBLE);
                msgOwner.setVisibility(View.VISIBLE);
            }
            //this check is for if owner removes a member and that user is in member page so it refershes
            checkIsPartOdRoomID();

            checkMemberExistance(apartment);


        }

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddShroomieMember add=new AddShroomieMember();
                Bundle bundle = new Bundle();
                bundle.putParcelable("APARTMENT_DETAILS",apartment);
                add.setArguments(bundle);
                add.show(getParentFragmentManager(),"add member to apartment");
            }
        });


        leaveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveApartment(apartment.getApartmentID());
            }
        });
        msgOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChattingActivity.class);
                intent.putExtra("USERID",mAuth.getCurrentUser().getUid());
                startActivity(intent);
            }
        });

    }

    private void getOwnerDetails(String ownerID) {
     rootRef.child("Users").child(ownerID).addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot snapshot) {
             if(snapshot.exists()) {
                 User owner = snapshot.getValue(User.class);
                 if(mAuth.getCurrentUser().getUid().equals(apartment.getOwnerID())){
                     ownerName.setText("You");
                 }else{
                     ownerName.setText(owner.getName());
                 }
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
    private void checkMemberExistance(final ShroomiesApartment oldApartment){
        apartmentListener=rootRef.child("apartments").child(oldApartment.getApartmentID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ShroomiesApartment newApartment=snapshot.getValue(ShroomiesApartment.class);
                    newMemberLists=newApartment.getApartmentMembers().values();
                    //if no changes in member list
                    if(newMemberLists.containsAll(oldApartment.getApartmentMembers().values())){
                        getMemberDetail(oldApartment.getApartmentMembers());
                        //if members leave
                    }else{
                        getMemberDetail(newApartment.getApartmentMembers());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
    private void checkIsPartOdRoomID(){
        roomIDListener=rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    isPartOfRoomID=snapshot.getValue().toString();
                    if(!isPartOfRoomID.equals(apartment.getApartmentID())){
                        //TODO go back
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void leaveApartment(final String apartmentID) {
        final CustomLoadingProgressBar customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(),"Leaving room...",R.raw.loading_animation);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        customLoadingProgressBar.show();
        final DatabaseReference ref= rootRef.child("apartments").push();
        final String newApartmentID =ref.getKey();
        final HashMap<String,Object> apartmentDetails=new HashMap<>();
        apartmentDetails.put("apartmentID",newApartmentID);
        apartmentDetails.put("ownerID",mAuth.getCurrentUser().getUid());
        ref.updateChildren(apartmentDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").setValue(newApartmentID).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            rootRef.child("apartments").child(apartmentID).child("apartmentMembers").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    saveToLeftLog(apartment.getApartmentID(),mAuth.getCurrentUser().getUid());
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
            }
        });



    }
    private void saveToLeftLog(final String apartmentID,final String userID){
        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String logID=ref.getKey();
        final HashMap<String,Object> newRecord=new HashMap<>();
        newRecord.put("actor",mAuth.getCurrentUser().getUid());
        newRecord.put("action","left");
        newRecord.put("when", ServerValue.TIMESTAMP);
        newRecord.put("logID",logID);
       ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                }
            }
        });

    }





    private void getMemberDetail(final HashMap<String,String> membersId) {
        membersList = new ArrayList<>();
        userAdapter = new UserAdapter(membersList, getContext(),apartment,getView());
        membersRecycler.setAdapter(userAdapter);
        for (String id: membersId.values()){
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootRef.removeEventListener(apartmentListener);
        rootRef.removeEventListener(roomIDListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        rootRef.removeEventListener(apartmentListener);
        rootRef.removeEventListener(roomIDListener);
    }
}
