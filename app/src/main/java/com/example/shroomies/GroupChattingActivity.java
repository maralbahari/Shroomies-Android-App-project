package com.example.shroomies;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChattingActivity extends AppCompatActivity {
    private ImageButton sendMessage,addImage;
    private EditText messageBody;
    private Toolbar chattingToolbar;
    private ImageView groupImage;
    private TextView groupNameTextview;
    private RecyclerView chattingRecycler;
    private String groupID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String senderID;
    private String saveCurrentDate,saveCurrentTime;
    private Group group;
    private List<Group> groupMessagesArrayList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    private static final int STORAGE_REQUEST_CODE=200;

    private static final int IMAGE_PICK_GALLERY_CODE=400;
    String[] cameraPermissions;
    String[] storagePermissions;
    Uri chosenImage=null;
    StorageReference filePathName;
    private String imageUrl;
    private boolean notification;
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
//        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        groupNameTextview=findViewById(R.id.receiver_username);
//        mAuth=FirebaseAuth.getInstance();
//        senderID=mAuth.getInstance().getCurrentUser().getUid();
//        rootRef= FirebaseDatabase.getInstance().getReference();
//        chattingToolbar= findViewById(R.id.chat_toolbar);
//        setSupportActionBar(chattingToolbar);
//        ActionBar actionBar=getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
//        actionBar.setDisplayShowTitleEnabled(false);
//        //hides the title from the action bar
//
//        final LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View actionbarView=inflater.inflate(R.layout.toolbar_chatting_layout,null);
//        sendMessage=findViewById(R.id.send_message_button);
//        addImage=findViewById(R.id.choose_file_button);
//        messageBody=findViewById(R.id.messeg_body_edit_text);
//        chattingRecycler=findViewById(R.id.recycler_view_group_chatting);
//        groupImage=findViewById(R.id.receiver_image_profile);
//        chattingRecycler=findViewById(R.id.recycler_view_group_chatting);
//        linearLayoutManager=new LinearLayoutManager(this);
//        linearLayoutManager.setStackFromEnd(true);
//        chattingRecycler.setHasFixedSize(true);
//        chattingRecycler.setLayoutManager(linearLayoutManager);
//        chattingRecycler.setAdapter(groupMessagesAdapter);
//        requestQueue= Volley.newRequestQueue(getApplicationContext());
//
//        Bundle extras = getIntent().getExtras();
//        if(!(extras==null)){
//            groupID=extras.getString("GROUPID");
//
//            //gets the  group details and sets them to the group variable
//            getGroupDetails();
//            loadMessages();
//        }
//        groupImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(getApplicationContext(),GroupInfoActivity.class);
//                intent.putExtra("GROUPID",groupID);
//                startActivity(intent);
//
//            }
//        });

//        sendMessage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessageToGroup();
//            }
//        });
//        addImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showImagePickDialog(); }
//        });
    }
//    public void sendMessageToGroup(){
//        String messageText=messageBody.getText().toString();
//        if(TextUtils.isEmpty(messageText)){
//            Toast.makeText(getApplicationContext(),"please enter a message",Toast.LENGTH_LONG).show();
//        }else {
//            notification=true;
//            DatabaseReference reference = rootRef.child("GroupChats").child(groupID).child("Messages").push();
//            //now making a unique id for each single message so that they wont be replaced and we save everything
//            String messagePushId=reference.getKey();
//            Calendar calendarDate=Calendar.getInstance();
//            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
//            saveCurrentDate=currentDate.format(calendarDate.getTime());
//            Calendar calendarTime=Calendar.getInstance();
//            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm aa");
//            saveCurrentTime=currentTime.format(calendarTime.getTime());
//            //unique name for each message
//            final String uniqueName=messagePushId+saveCurrentDate+saveCurrentTime;
//            Map messageDetails= new HashMap();
//            messageDetails.put("message",messageText);
//            messageDetails.put("time",saveCurrentTime);
//            messageDetails.put("date",saveCurrentDate);
//            messageDetails.put("type","text");
//            messageDetails.put("from",senderID);
//            HashMap<String, Object> seenBy = new HashMap<>();
//            // add each members id with false next to it
//            // this is going to act like the is isseen in the chatting  activity
//            for(String id
//                    :group.getGroupMembers()){
//                seenBy.put(id ,"false");
//            }
//
//            messageDetails.put("seenBy" ,seenBy );
//
//
//            reference.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if(task.isSuccessful()){
//                        messageBody.setText("");
//                        chattingRecycler.smoothScrollToPosition(groupMessagesAdapter.getItemCount()-1);
//                    }else{
//                        String message =task.getException().getMessage();
//                        Toast.makeText(getApplicationContext(),"Error "+message,Toast.LENGTH_SHORT).show();
//                        messageBody.setText("");
//                    }
//                }
//            });
//            if(notification){
//
//                for (String id
//                     :group.getGroupMembers()){
////                    sendNotification(id,group.getGroupName(), messageText);
//                }
//            }else{
//                notification=false;
//
//            }
//        }
//        }
//        void loadMessages(){
//            groupMessagesArrayList = new ArrayList<>();
//            groupMessagesAdapter=new GroupMessagesAdapter(groupMessagesArrayList,getApplicationContext());
//            chattingRecycler.setAdapter(groupMessagesAdapter);
//            rootRef.child("GroupChats").child(groupID).child("Messages").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    groupMessagesArrayList.clear();
//                    if(snapshot.exists()){
//                        for(DataSnapshot dataSnapshot
//                                :snapshot.getChildren()) {
//                            Group groupMessages = dataSnapshot.getValue(Group.class);
//                            groupMessagesArrayList.add(groupMessages);
//                        }
//                        groupMessagesAdapter.notifyDataSetChanged();
//                        chattingRecycler.smoothScrollToPosition(groupMessagesAdapter.getItemCount()-1);
//
//                    }else{ }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
//        private void getGroupDetails(){
//            rootRef.child("GroupChats").child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if(snapshot.exists()){
//                            group = snapshot.getValue(Group.class);
//                            groupNameTextview.setText(group.getGroupName());
//
//
//                            if(!group.getImage().isEmpty()) {
//                                GlideApp.with(getApplicationContext())
//                                        .load(group.getGroupImage())
//                                        .fitCenter()
//                                        .circleCrop()
//                                        .into(groupImage);
//                                groupImage.setPadding(0,0,0,0);
//                            }
//                            groupNameTextview.setText(group.getGroupName());
//                            messageSeen();
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
//    private void showImagePickDialog(){
//        String[] options={"Gallery"};
//        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//        builder.setTitle("Choose Image from");
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //gallery
//                if(which==0){
//                    if(!checkStoragePermisson()){
//                        requestStoragePermission();
//                    }else{
//                        pickFromGallery();
//                    }
//                }
//            }
//        });
//        builder.create().show();
//    }
//
//    private void pickFromGallery(){
//        Intent intent=new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
//    }
//    private boolean checkStoragePermisson(){
//        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
//
//        return result;
//    }
//    private void requestStoragePermission(){
//        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
//    }
//    //this method is called when user oress allow or deny form permission request dialog
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode){
//
//            case STORAGE_REQUEST_CODE:{
//                if(grantResults.length>0){
//                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
//                    if(storageAccepted){
//                        pickFromGallery();
//                    }else{
//                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
//                        Toast.makeText(this," storage permission is neccessary....",Toast.LENGTH_SHORT).show();
//                        pickFromGallery();
//
//                    }
//                }
//            }
//            break;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if(resultCode==RESULT_OK){
//            if(requestCode==IMAGE_PICK_GALLERY_CODE){
//                chosenImage=data.getData();
//                //Save to firebase
//                sendImageMessage(chosenImage);
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//
//    }
//    private void sendImageMessage(Uri image){
//        notification=true;
//        final ProgressDialog progressDialog=new ProgressDialog(this);
//        progressDialog.setMessage("sending image...");
//        progressDialog.show();
//        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
//        filePathName=storageReference.child("chating images").child(image.getLastPathSegment()
//                +System.currentTimeMillis()+".jpg");
//        filePathName.putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if(task.isSuccessful()){
//                    progressDialog.dismiss();
//                    imageUrl=task.getResult().getMetadata().getReference().getPath().toString();
//                    addToRealTimeDataBase(imageUrl);
//                }
//
//            }
//        });
//
//    }
//    private void addToRealTimeDataBase(String url){
//
//        DatabaseReference reference = rootRef.child("GroupChats").child(groupID).child("Messages").push();
//        //now making a unique id for each single message so that they wont be replaced and we save everything
//        String messagePushId=reference.getKey();
//        Calendar calendarDate=Calendar.getInstance();
//        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
//        saveCurrentDate=currentDate.format(calendarDate.getTime());
//        Calendar calendarTime=Calendar.getInstance();
//        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm aa");
//        saveCurrentTime=currentTime.format(calendarTime.getTime());
//        //unique name for each message
//        final String uniqueName=messagePushId+saveCurrentDate+saveCurrentTime;
//        Map messageDetails= new HashMap();
//        messageDetails.put("message",url);
//        messageDetails.put("time",saveCurrentTime);
//        messageDetails.put("date",saveCurrentDate);
//        messageDetails.put("type","image");
//        messageDetails.put("from",senderID);
//
//        // create a seen by node and add it inside the message
//        HashMap<String, Object> seenBy = new HashMap<>();
//        // add each members id with false next to it
//        // this is going to act like the is isseen in the chatting  activity
//        for(String id
//        :group.getGroupMembers()){
//            seenBy.put(id ,"false");
//        }
//
//        messageDetails.put("seenBy" ,seenBy );
//
//
//        reference.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//                    Toast.makeText(getApplicationContext(),"id: "+uniqueName ,Toast.LENGTH_SHORT).show();
//                    messageBody.setText("");
//                }else{
//                    String message =task.getException().getMessage();
//                    Toast.makeText(getApplicationContext(),"Error "+message,Toast.LENGTH_SHORT).show();
//                    messageBody.setText("");
//                }
//            }
//        });
//
//        if(notification){
//
//            for (String id
//                    :group.getGroupMembers()){
////                sendNotification(id,group.getGroupName(), "image");
//            }
//        }else{
//            notification=false;
//
//        }
//    }
//    private void messageSeen() {
//        rootRef.child("GroupChats").child(groupID).child("Messages").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    for(DataSnapshot dataSnapshot
//                            :snapshot.getChildren()){
//                        dataSnapshot.child("seenBy").child(mAuth.getCurrentUser().getUid()).getRef().setValue("true");
//                    }
////                    MainActivity.setBadgeToNumberOfNotifications(rootRef, mAuth);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
}
