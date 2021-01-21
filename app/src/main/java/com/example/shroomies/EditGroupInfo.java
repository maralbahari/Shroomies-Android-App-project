package com.example.shroomies;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditGroupInfo extends AppCompatActivity {
    private static final int PICK_IMAGE_MULTIPLE = 1;
    private FirebaseAuth mAuth;
    private ImageView groupImage;
    private ImageButton addImage;
    private EditText groupChatTitle;
    private RecyclerView groupMembers;
    private DatabaseReference rootRef;
    private Button saveChanges ;
    private StorageReference storageReference;
    StorageReference filePath ;
    private Uri selectedImageUri;
    private Group group;
    private UserRecyclerAdapter membersAdapter;
    private ArrayList<User> membersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_info);
        rootRef= FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        groupImage=findViewById(R.id.group_chat_image);
        groupChatTitle=findViewById(R.id.group_chat_name);
        groupChatTitle.setFocusableInTouchMode(true);
        groupMembers=findViewById(R.id.list_of_selected_members);
        saveChanges=findViewById(R.id.create_group_chat_button);
        addImage = findViewById(R.id.add_group_image_button);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        groupMembers.setHasFixedSize(true);
        groupMembers.setLayoutManager(linearLayoutManager);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            group=bundle.getParcelable("GROUP");
            groupChatTitle.setText(group.getGroupName());

            GlideApp.with(getApplicationContext())
                    .load(group.getGroupImage())
                    .fitCenter()
                    .circleCrop()
                    .into(groupImage);
            groupImage.setPadding(0,0,0,0);
            getMemberDetail(group.getGroupMembers());
        }

        groupChatTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupChatTitle.setEnabled(true);
                groupChatTitle.setFocusableInTouchMode(true);
            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!group.getGroupName().equals(groupChatTitle.getText().toString())){
                    rootRef.child("GroupChats").child(group.getGroupID()).child("groupName").setValue(groupChatTitle.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                           if(selectedImageUri!=null){
                               uploadImageToFirebase(selectedImageUri , group);
                           }else {
                               Intent intent = new Intent(getApplicationContext(), GroupChattingActivity.class);
                               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                               intent.putExtra("GROUPID", group.getGroupID());
                               startActivity(intent);
                           }
                        }
                    });
                }
                if(selectedImageUri !=null){
                    uploadImageToFirebase(selectedImageUri , group);
                }
            }
        });

    }


    private void getMemberDetail(List<String> groupMembersID) {
        membersList = new ArrayList<>();
        membersAdapter=new UserRecyclerAdapter(membersList,getApplicationContext(),"EDIT_GROUP_INFO" , group.getGroupID());
        groupMembers.setAdapter(membersAdapter);
        //look for each members details in firebase from the array we created in line 83 one by one
        for(String memberId: groupMembersID){
            rootRef.child("Users").child(memberId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        User user=snapshot.getValue(User.class);
                        //save each member's details in an array of User type to be able pass to adapter
                        membersList.add(user);
                        membersAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //finally add to the adapter

        }

    }

    private void uploadImageToFirebase(Uri imageUri , final Group group){

        String postUniqueName = getUniqueName();
        filePath = storageReference.child("group profile image").child(imageUri.getLastPathSegment()
                +postUniqueName+".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    task.getResult().getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            rootRef.child("GroupChats").child(group.getGroupID()).child("groupImage").setValue(task.getResult().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent intent = new Intent(getApplicationContext(), GroupChattingActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("GROUPID" , group.getGroupID());
                                    startActivity(intent);
                                }
                            });
                        }
                    });

                }else{
                    //add exception
                }
            }
        });


    }




    private void openGallery() {
        //add permisision denied handlers
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(gallery, PICK_IMAGE_MULTIPLE);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);



            if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
                //get selected photo
                selectedImageUri = data.getData();
                Glide.with(getApplicationContext())
                        .load(selectedImageUri)
                        .fitCenter()
                        .circleCrop()
                        .into(groupImage);

                } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 400);
                }

            }


    private String getUniqueName() {
        //create a unique id for the post by combining the date with uuid
        //get the date first
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calendarDate.getTime());

        //get the time in hours and minutes
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        String saveCurrentTime = currentTime.format(calendarTime.getTime());

        //add the two string together

        return  saveCurrentDate+saveCurrentTime;
    }


}