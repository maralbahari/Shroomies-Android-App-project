package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    ArrayList<User> getRequestUsersList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.shroomie_members, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        getUserRoomId();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addMember=view.findViewById(R.id.add_shroomie_btn);
        leaveRoom=view.findViewById(R.id.leave_room_btn);
        membersRecycler = view.findViewById(R.id.members_recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        membersRecycler.setHasFixedSize(true);
        membersRecycler.setLayoutManager(linearLayoutManager);

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddShroomieMember add=new AddShroomieMember();
                add.show(getParentFragmentManager(),"add member to apartment");
            }
        });
        getMember();
        if(mAuth.getCurrentUser().getUid().equals(apartmentID)){
            leaveRoom.setVisibility(View.GONE);
        }
        leaveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveApartment();
            }
        });

    }

    private void leaveApartment() {
        rootRef.child("apartments").child(apartmentID).child("apartmentMembers").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").setValue(mAuth.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),"you have left the room",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }
    private void getMember(){
        membersId = new ArrayList<>();

        rootRef.child("apartments").child(apartmentID).child("apartmentMembers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot sp : snapshot.getChildren()){
                        membersId.add(sp.getKey());
                        getMemberDetail(membersId);



                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void getMemberDetail(final ArrayList<String> membersId) {
        membersList = new ArrayList<>();
        userAdapter = new UserAdapter(membersList, getContext(),false);
        membersRecycler.setAdapter(userAdapter);
        for (String id: membersId){
            rootRef.child("Users").child(id).addValueEventListener(new ValueEventListener() {
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


    private void getUserRoomId(){
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    apartmentID=snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
