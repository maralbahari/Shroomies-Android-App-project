package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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

public class GroupInfoActivity extends AppCompatActivity {
    private Button leave,delete,addParticipants,edit,backToChattingActivity;
    private Toolbar groupInfoToolbar;
    private RecyclerView groupMembersRecycler;
    private ImageView groupImage;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String groupID;
    private ArrayList<String> groupMembersID;
    private UserRecyclerAdapter membersAdapter;
    private ArrayList<User> membersList;
    private TextView numberOfParticipants;
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
       groupImage=findViewById(R.id.group_chat_info_image);
       numberOfParticipants=findViewById(R.id.number_of_participants);
        backToChattingActivity=findViewById(R.id.back_button_group_info);
       rootRef= FirebaseDatabase.getInstance().getReference();
       mAuth=FirebaseAuth.getInstance();
        Bundle extras = getIntent().getExtras();
        if(!(extras==null)){
            groupID=extras.getString("GROUPID");
            membersList=(extras.getParcelableArrayList("ListOfSelectedUsers"));
            loadGroupDetail();
            if(membersList!=null){
                addNewPatricipantToFirebase(membersList);
            }
        }
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit group chat here
                Intent intent= new Intent(getApplicationContext(),EditGroupInfo.class);
                intent.putExtra("GROUPID",groupID);
                startActivity(intent);
            }
        });
      addParticipants.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              CreateChatGroupFragment createChatGroupFragment=new CreateChatGroupFragment();
              Bundle bundle= new Bundle();
              bundle.putBoolean("FromGroupInfo",true);
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
        backToChattingActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(),GroupChattingActivity.class);
                intent.putExtra("GROUPID",groupID);
                startActivity(intent);
            }
        });

    }
    private void leaveGroup(){
        rootRef.child("GroupChats").child(groupID).child("groupMembers").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(getApplicationContext(),"you have left the group",Toast.LENGTH_SHORT).show();
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
    private void loadGroupDetail(){
        rootRef.child("GroupChat").child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot
                            : snapshot.getChildren()) {
                        Group group = dataSnapshot.getValue(Group.class);
                        GlideApp.with(getApplicationContext())
                                .load(group.getImage())
                                .transform(new RoundedCorners(1))
                                .fitCenter()
                                .centerCrop()
                                .into(groupImage);
                        //save members ID in array
                        groupMembersID= (ArrayList<String>) group.groupMembers;

                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //look for each members details in firebase from the array we created in line 83 one by one
        for(String memberId: groupMembersID){
            rootRef.child("Users").child(memberId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        User user=snapshot.getValue(User.class);
                        //save each member's details in an array of User type to be able pass to adapter
                        membersList.add(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //finally add to the adapter
            membersAdapter=new UserRecyclerAdapter(membersList,getApplicationContext(),false);
            groupMembersRecycler.setAdapter(membersAdapter);
            membersAdapter.notifyDataSetChanged();
            numberOfParticipants.setText(numberOfParticipants.getText().toString()+" ("+membersAdapter.getItemCount()+") ");
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
    private void addNewPatricipantToFirebase(ArrayList<User> membersList){
        ArrayList<String> ids=null;
        for(User userAdded:membersList){
          ids.add(userAdded.getID());
        }
        HashMap<String,Object> addUsers=new HashMap<>();
        addUsers.put("groupMembers",addUsers);
        rootRef.child("GroupChats").child(groupID).updateChildren(addUsers).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"added members Successfully",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}