package com.example.shroomies;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.shroomies.notifications.Data;
import com.example.shroomies.notifications.Sender;
import com.example.shroomies.notifications.Token;
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
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChattingActivity extends AppCompatActivity {
    private boolean firstChat = false;

    private ImageButton sendMessage,addImage;
    private EditText messageBody;
    private Toolbar chattingToolbar;
    private ImageView receiverProfileImage,attacheButton;
    private TextView receiverUsername;
    private RecyclerView chattingRecycler;
    private String receiverID,receiverName;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String senderID;
    private String saveCurrentDate,saveCurrentTime;
    private List<Messages> messagesArrayList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private boolean notify=false;
    private RequestQueue requestQueue;
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    String[] cameraPermissions;
    String[] storagePermissions;
    Uri chosenImage=null;
    StorageReference filePathName;
    private String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chatting);

        receiverUsername=findViewById(R.id.receiver_username);
        mAuth=FirebaseAuth.getInstance();
        senderID=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();
        Bundle extras = getIntent().getExtras();
        if(!(extras==null)){
            receiverID=extras.getString("USERID");
            getUserDetail(receiverID);
        }
        initializeViews();
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToUser();
            }
        });


        retrieveMessages();
       addImage.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                 showImagePickDialog();
           }
       });
    }
    private void initializeViews(){
        chattingToolbar= findViewById(R.id.chat_toolbar);
        setSupportActionBar(chattingToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView=inflater.inflate(R.layout.toolbar_chatting_layout,null);
        sendMessage=findViewById(R.id.send_message_button);
        addImage=findViewById(R.id.choose_file_button);

        messageBody=findViewById(R.id.messeg_body_edit_text);
        chattingRecycler=findViewById(R.id.recycler_view_group_chatting);
        receiverProfileImage=findViewById(R.id.receiver_image_profile);
        chattingRecycler=findViewById(R.id.recycler_view_group_chatting);
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chattingRecycler.setHasFixedSize(true);
        chattingRecycler.setLayoutManager(linearLayoutManager);

        requestQueue= Volley.newRequestQueue(getApplicationContext());
    }
    private void sendMessageToUser(){
        final String messageText=messageBody.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(getApplicationContext(),"please enter a message",Toast.LENGTH_LONG).show();
        }else{
            notify=true;
            // referencing to database which user is send message to whom
           String messageSenderRef="Messages/"+senderID+"/"+receiverID;
           String messageReceiverRef="Messages/"+receiverID+"/"+senderID;
          //create the database ref
            final String messagePushId= rootRef.child("Messages").child(senderID).child(receiverID).push().getKey();
            //now making a unique id for each single message so that they wont be replaced and we save everything
//            String messagePushId=userMessageKey.getKey();
            Calendar calendarDate=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate=currentDate.format(calendarDate.getTime());
            Calendar calendarTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm aa");
            saveCurrentTime=currentTime.format(calendarTime.getTime());
            Map messageTextBody= new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",senderID);
            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
           messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);
           rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if(task.isSuccessful()){
                       messageBody.setText("");
                       chattingRecycler.smoothScrollToPosition(messagesAdapter.getItemCount()-1);
                       if(firstChat){
                           addUserToInbox();
                       }
                   }else{
                       String message =task.getException().getMessage();
                       Toast.makeText(getApplicationContext(),"Error "+message,Toast.LENGTH_SHORT).show();
                       messageBody.setText("");
                   }

               }
           });
        }

       rootRef.child("Users").child(senderID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
              if(snapshot.exists()){
                  User user=snapshot.getValue(User.class);
                  if(notify){
                      sendNotification(receiverID,user.getName(),messageText);
                  }
                  notify=false;
              }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

    }
    private void sendNotification(final String receiverID,final String senderName,final String message) {
        rootRef.child("Tokens").orderByKey().equalTo(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Token token = (Token) ds.getValue(Token.class);
                        Data data = new Data(senderID, senderName + ":" + message, "New Message", receiverID, (R.drawable.ic_mashroom_icon));
                        Sender sender = new Sender(data, token.getToken());
                        try {
                            JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("JSON_RESPONSE","onResponse:"+response.toString());
                                }

                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("JSON_RESPONSE","onResponse:"+error.toString());

                                }
                            })
                            {
                                @Override
                                public Map<String,String> getHeaders() throws AuthFailureError {
                                    Map<String,String> headers= new HashMap<>();
                                    headers.put("Content-type","application/json");
                                    headers.put("Authorization","Key=AAAAyn_kPyQ:APA91bGLxMB-HGP-qd_EPD3wz_apYs4ZJIB2vyAvH5JbaTVlyLExgYn7ye-076FJxjfrhQ-1HJBmptN3RWHY4FoBdY08YRgplZSAN0Mnj6sLbS6imKa7w0rqPsLtc-aXMaPOhlxnXqPs");
                                    return headers;
                                }

                            };

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

   public void retrieveMessages(){
        messagesArrayList = new ArrayList<>();
       messagesAdapter=new MessagesAdapter(messagesArrayList , getApplication());
       chattingRecycler.setAdapter(messagesAdapter);
        rootRef.child("Messages").child(senderID).child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot
                    :snapshot.getChildren()){
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesArrayList.add(messages);
                    }
                    messagesAdapter.notifyDataSetChanged();


                }
                if (messagesArrayList.size()==0|| messagesArrayList==null){

                    firstChat= true;
                }else{
                    // scroll to the end of the recycler if there are messages
                    chattingRecycler.smoothScrollToPosition(messagesAdapter.getItemCount()-1);
                }

                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}

   private void showImagePickDialog(){
        String[] options={"Camera","Gallery"};
       AlertDialog.Builder builder=new AlertDialog.Builder(this);
       builder.setTitle("Choose Image from");
       builder.setItems(options, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               //camera
               if(which==0){
                   if(!checkCameraPermisson()){
                       requestCameraPermission();
                   }
                   else{
                       pickFromCamera();
                   }
               }
               //gallery
               if(which==1){
                   if(!checkStoragePermisson()){
                       requestStoragePermission();
                   }else{
                       pickFromGallery();
                   }
               }
           }
       });
       builder.create().show();
   }
   private void pickFromCamera(){
       ContentValues cv= new ContentValues();
       cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
       cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Describe");
        chosenImage=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

       Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       intent.putExtra(MediaStore.EXTRA_OUTPUT,chosenImage);
       startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

   }
   private void pickFromGallery(){
       Intent intent=new Intent(Intent.ACTION_PICK);
       intent.setType("image/*");
       startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
   }
   private boolean checkStoragePermisson(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;
   }
   private void requestStoragePermission(){
       ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
   }
   private boolean checkCameraPermisson(){
        boolean resultCam= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean resultStorage= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return resultCam && resultStorage;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }
//this method is called when user oress allow or deny form permission request dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){

                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else{
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                        Toast.makeText(this,"Camera and storage both permissions are neccessary....",Toast.LENGTH_SHORT).show();
                    }
                }else{

                }
            }break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }else{
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                        Toast.makeText(this," storage permission is neccessary....",Toast.LENGTH_SHORT).show();
                        pickFromGallery();

                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                chosenImage=data.getData();
                //Save to firebase
                sendImageMessage(chosenImage);
            }else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                sendImageMessage(chosenImage);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    private void sendImageMessage(Uri image){
        notify=true;
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("sending image...");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        filePathName=storageReference.child("chating images").child(image.getLastPathSegment()
                +System.currentTimeMillis()+".jpg");
               filePathName.putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if(task.isSuccessful()){
                          progressDialog.dismiss();
                           imageUrl=task.getResult().getMetadata().getReference().getPath().toString();
                           addToRealTimeDataBase(imageUrl);
                       }

                   }
               });

    }
    private void addToRealTimeDataBase(String url){

        String messageSenderRef="Messages/"+senderID+"/"+receiverID;
        String messageReceiverRef="Messages/"+receiverID+"/"+senderID;
        //create the database ref
        final String messagePushId= rootRef.child("Messages").child(senderID).child(receiverID).push().getKey();
        //now making a unique id for each single message so that they wont be replaced and we save everything
//            String messagePushId=userMessageKey.getKey();
        Calendar calendarDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(calendarDate.getTime());
        Calendar calendarTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm aa");
        saveCurrentTime=currentTime.format(calendarTime.getTime());
        Map messageTextBody= new HashMap();
        messageTextBody.put("message",url);
        messageTextBody.put("time",saveCurrentTime);
        messageTextBody.put("date",saveCurrentDate);
        messageTextBody.put("type","image");
        messageTextBody.put("from",senderID);
        Map messageBodyDetails=new HashMap();
        messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
        messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);
        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    messageBody.setText("");
                }else{
                    String message =task.getException().getMessage();
                    Toast.makeText(getApplicationContext(),"Error "+message,Toast.LENGTH_SHORT).show();
                    messageBody.setText("");
                }

            }
        });
        //send Notification
        rootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user=snapshot.getValue(User.class);
                    if(notify){
                        sendNotification(receiverID,user.getName(),"Sent you a photo..");

                    }
                    notify=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserDetail(final String recieverID){
        rootRef.child("Users").child(recieverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User recieverUser = snapshot.getValue(User.class);
                    GlideApp.with(getApplicationContext())
                            .load(recieverUser.getImage())
                            .fitCenter()
                            .circleCrop()
                            .into(receiverProfileImage);
                    receiverUsername.setText(recieverUser.getName());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void addUserToInbox( ){
        // put the reciver details in the senders private chat list and vice versa
        HashMap<String, Object> receiverDetails = new HashMap<>();
        final HashMap<String, Object> senderDetails = new HashMap<>();
        senderDetails.put("receiverID",senderID);
        receiverDetails.put("receiverID",receiverID);
        rootRef.child("PrivateChatList").child(senderID).child(receiverID).setValue(receiverDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootRef.child("PrivateChatList").child(receiverID).child(mAuth.getInstance().getCurrentUser().getUid()).setValue(senderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            }
                        }
                    });

                }
            }
        });

    }
}
