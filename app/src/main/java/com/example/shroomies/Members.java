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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Members extends DialogFragment {
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

    private void leaveApartment(String apartmentID) {
        final CustomLoadingProgressBar customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(),"Leaving room...",R.raw.loading_animation);
        customLoadingProgressBar.show();
        rootRef.child("apartments").child(apartmentID).child("apartmentMembers").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
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
        userAdapter = new UserAdapter(membersList, getContext(),false,apartment);
        membersRecycler.setAdapter(userAdapter);
        for (String id: membersId){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        membersList.add(user);
                        Toast.makeText(getContext(),user.getName(),Toast.LENGTH_LONG).show();
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
