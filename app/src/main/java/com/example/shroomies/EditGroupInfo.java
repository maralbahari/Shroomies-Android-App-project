package com.example.shroomies;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class EditGroupInfo extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageView groupImage;
    private ImageButton addImage;
    private EditText groupChatTitle;
    private RecyclerView groupMembers;
    private DatabaseReference rootRef;
    private UserRecyclerAdapter userRecyclerAdapter;
    private Button saveChanges;
    private String saveCurrentDate,saveCurrentTime;
    private StorageReference storageReference;
    StorageReference filePath ;
    private Uri imageUri;
    private String groupID;
    private ArrayList<String> groupMembersID;
    private UserRecyclerAdapter membersAdapter;
    private ArrayList<User> membersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_info);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groupID=bundle.getString("GROUPID");
            displayGroupInfo();
        }
        rootRef= FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        groupImage=findViewById(R.id.group_chat_image);
        groupChatTitle=findViewById(R.id.group_chat_name);
        groupMembers=findViewById(R.id.list_of_selected_members);
        saveChanges=findViewById(R.id.create_group_chat_button);
        addImage = findViewById(R.id.icon_add_group_image);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        groupMembers.setHasFixedSize(true);
        groupMembers.setLayoutManager(linearLayoutManager);

    }
    private void displayGroupInfo(){
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
            groupMembers.setAdapter(membersAdapter);
            membersAdapter.notifyDataSetChanged();
        }
    }
}