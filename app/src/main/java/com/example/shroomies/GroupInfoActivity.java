package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class GroupInfoActivity extends AppCompatActivity {
    private Button leave,delete,addParticipants,edit,backToChattingActivity;
    private Toolbar groupInfoToolbar;
    private RecyclerView groupMembersRecycler;
    private ImageView groupImage;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String groupID;
    private List<String> groupMembersID;
    private UserRecyclerAdapter membersAdapter;
    private ArrayList<User> membersList;
    private ArrayList<User> newUserList;
    private TextView numberOfParticipants;
    private Group group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        leave= findViewById(R.id.leave_group_chat_button);
        delete=findViewById(R.id.delete_group_chat_button);
        addParticipants=findViewById(R.id.add_member_group_chat);
        edit=findViewById(R.id.edit_group_chat);
        groupInfoToolbar= findViewById(R.id.groupIndfo_toolbar);
        setSupportActionBar(groupInfoToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
       groupMembersRecycler=findViewById(R.id.participants_recycler);
        groupMembersRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplication());
        groupMembersRecycler.setLayoutManager(linearLayoutManager);
       groupImage=findViewById(R.id.group_chat_info_image);
       numberOfParticipants=findViewById(R.id.number_of_participants);
       rootRef= FirebaseDatabase.getInstance().getReference();
       mAuth=FirebaseAuth.getInstance();
        Bundle extras = getIntent().getExtras();
        if(!(extras==null)){
            groupID=extras.getString("GROUPID");
            newUserList=(extras.getParcelableArrayList("ListOfSelectedUsers"));
            loadGroupDetail();
            if(newUserList!=null){
                addNewPatricipantToFirebase(newUserList);
            }

        }
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit group chat here
                Intent intent= new Intent(getApplicationContext(),EditGroupInfo.class);
                intent.putExtra("GROUP" , group);
                startActivity(intent);
            }
        });
      addParticipants.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              CreateChatGroupFragment createChatGroupFragment=new CreateChatGroupFragment();
              Bundle bundle= new Bundle();
              bundle.putBoolean("FromGroupInfo",true);
              bundle.putString("GROUPID" , groupID);
              createChatGroupFragment.setArguments(bundle);
              createChatGroupFragment.show(getSupportFragmentManager(),"create group dialog 1");
          }
      });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create progress dialog to make sure user wants to delete
                deleteGroup();
            }
        });

       leave.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //add progress dialog
               leaveGroup();
           }
       });
//        backToChattingActivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent= new Intent(getApplicationContext(),GroupChattingActivity.class);
//                intent.putExtra("GROUPID",groupID);
//                startActivity(intent);
//            }
//        });

    }
    private void leaveGroup(){
        final String userID = mAuth.getInstance().getCurrentUser().getUid();
//        find the index of the current user in the group members list
        rootRef.child("GroupChats").child(groupID).child("groupMembers").orderByValue().equalTo(userID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // once the index is found remove it
                rootRef.child("GroupChats").child(groupID).child("groupMembers").child(snapshot.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        rootRef.child("GroupChatList").child(mAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                HashMap <String , Object> groups  = new HashMap<>();
                                for (DataSnapshot dataSnapshot
                                :snapshot.getChildren()){
                                    if(!dataSnapshot.getKey().equals(groupID)){
                                        groups.put(dataSnapshot.getKey() , dataSnapshot.getValue());
                                    }
                                }
                                rootRef.child("GroupChats").child(mAuth.getInstance().getCurrentUser().getUid()).setValue(groups);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override public void onCancelled(@NonNull DatabaseError error) {

            }});
    }
    private void loadGroupDetail(){

        rootRef.child("GroupChats").child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    group = snapshot.getValue(Group.class);
                        //save members ID in array
                    groupMembersID=  group.getGroupMembers();
                    membersList = new ArrayList<>();
                    getMemberDetail(groupMembersID);

                    if(!group.groupImage.isEmpty()) {
                        GlideApp.with(getApplicationContext())
                                .load(group.getGroupImage())
                                .fitCenter()
                                .centerCrop()
                                .into(groupImage);
                        groupImage.setPadding(0, 0, 0, 0);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    private void getMemberDetail(List<String> groupMembersID) {
        membersAdapter=new UserRecyclerAdapter(membersList,getApplicationContext(),"GROUP_INFO_ACTIVITY");
        groupMembersRecycler.setAdapter(membersAdapter);
        //look for each members details in firebase from the array we created in line 83 one by one
        numberOfParticipants.setText("Participants"+" ("+groupMembersID.size()+")");
            for (String memberId
            :groupMembersID) {
                if(memberId!=null) {


                    rootRef.child("Users").child(memberId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                //save each member's details in an array of User type to be able pass to adapter
                                membersList.add(user);
                                membersAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }

            }

    }
    private void deleteGroup(){
     rootRef.child("GroupChats").child(groupID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
         @Override
         public void onSuccess(Void aVoid) {
             Toast.makeText(getApplicationContext(),"group deleted",Toast.LENGTH_SHORT).show();
             Intent intent= new Intent(getApplicationContext(),MessageInbox.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(intent);
             finish();
         }
     }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception e) {
             Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
         }
     });
    }

    private void addNewPatricipantToFirebase(ArrayList<User> membersList) {
        final List<String> ids = new ArrayList<>();
        for (User userAdded : membersList) {
            ids.add(userAdded.getUserID());
        }
        rootRef.child("GroupChats").child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Group group = snapshot.getValue(Group.class);

                    List<String> currentMembers = group.getGroupMembers();
                    for (String id
                    :ids){
                        if(!currentMembers.contains(id)){
                            currentMembers.add(id);
                        }
                    }
                    HashMap<String, Object> addUsers = new HashMap<>();
                    addUsers.put("groupMembers", currentMembers);
                    updateGroupMembers(addUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateGroupMembers(final HashMap<String, Object> addUsers) {
        rootRef.child("GroupChats").child(groupID).updateChildren(addUsers).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                for (String memberId
                :(ArrayList<String>)addUsers.get("groupMembers")){
                    HashMap<String,Object> newGroup = new HashMap<>();
                    newGroup.put(groupID , group.getGroupName());
                    rootRef.child("GroupChatList").child(memberId).updateChildren(newGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "successfully added users", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

}