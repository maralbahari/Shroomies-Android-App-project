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
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.virgilsecurity.android.ethree.interaction.EThree;
import com.virgilsecurity.common.callback.OnResultListener;
import com.virgilsecurity.sdk.cards.Card;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
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
    private String receiverID;
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
    private ValueEventListener seenLisenter;
    private EThree eThree;
    private Card recepientVirgilCard;
    private Card senderVirgilCard;
    private int messageEndPosition = 0;


//    @Override
//    protected void onStop() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onStop();
//    }
//
//    @Override
//    public void onBackPressed() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onBackPressed();
//
//    }
//
//
//
//    @Override
//    protected void onPause() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onPause();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        rootRef.removeEventListener(seenLisenter);
//        super.onDestroy();
//    }


    @Override
    protected void onStart() {
        super.onStart();


    }

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
            //initialize ethree
            eThree = LoginActivity.eThree;
            // get the user pubilc key
            getRecepientCard(receiverID);
            getSenderCard();
            getUserDetail(receiverID);
        }
        initializeViews();
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToUser();
            }
        });



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
        chattingRecycler.setHasFixedSize(true);
        linearLayoutManager.setStackFromEnd(true);
        chattingRecycler.setLayoutManager(linearLayoutManager);

        requestQueue= Volley.newRequestQueue(getApplicationContext());
    }
    private void sendMessageToUser(){
        String encryptedMessage = null;
        final String messageText= messageBody.getText().toString();
        if(TextUtils.isEmpty(messageText)) {
            Toast.makeText(getApplicationContext(), "please enter a message", Toast.LENGTH_LONG).show();
        }else {

            encryptedMessage = eThree.authEncrypt(messageText , recepientVirgilCard);

            messageBody.setText("");
            // encrypt the message using the static
            // ethree instance from the login activity

            notify = true;
            // referencing to database which user is send message to whom
            String messageSenderRef = "Messages/" + senderID + "/" + receiverID;
            String messageReceiverRef = "Messages/" + receiverID + "/" + senderID;
            //create the database ref
            final String messagePushId = rootRef.child("Messages").child(senderID).child(receiverID).push().getKey();
            //now making a unique id for each single message so that they wont be replaced and we save everything
//            String messagePushId=userMessageKey.getKey();
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calendarDate.getTime());
            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calendarTime.getTime());
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", encryptedMessage);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderID);
            messageTextBody.put("isSeen", "false");
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);
            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {

                        chattingRecycler.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
                        if (firstChat) {
                            addUserToInbox();
                        }
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(getApplicationContext(), "Error " + message, Toast.LENGTH_SHORT).show();

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
                      // TODO decrypt the notification
//                      sendNotification(receiverID,user.getName(),encryptedMessage);
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
        rootRef.child("Token").orderByKey().equalTo(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Token token = (Token) ds.getValue(Token.class);
                        Data data = new Data(senderID, senderName + ":" + message, "New Message", receiverID, (R.drawable.ic_notification_icon));
                        Sender sender = new Sender(data, token.getToken());
                        try {
                            JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_LONG).show();
//                                    Log.d("JSON_RESPONSE","onResponse:"+response.toString());
                                }

                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG).show();
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
                            requestQueue.add(jsonObjectRequest);
                            requestQueue.start();
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
        messagesAdapter=new MessagesAdapter(messagesArrayList , getApplication() ,recepientVirgilCard , senderVirgilCard);
        chattingRecycler.setAdapter(messagesAdapter);
        rootRef.child("Messages").child(senderID).child(receiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                        Messages message = snapshot.getValue(Messages.class);
                        messagesArrayList.add(message);

                        // decrypt the message and store in place of the encrypted message
//                        Toast.makeText(getApplicationContext() , eThree.authDecrypt(message.getText() , senderVirgilCard ) , Toast.LENGTH_SHORT).show();
                        if(message.getType().equals("text")) {
                            if (message.getFrom().equals(mAuth.getCurrentUser().getUid())) {
                                message.setText(eThree.authDecrypt(message.getText(), senderVirgilCard));
                            } else {
                                message.setText(eThree.authDecrypt(message.getText(), recepientVirgilCard));
                            }
                        }
                    messagesAdapter.notifyItemInserted(messageEndPosition);
                    messageEndPosition+=1;
                    chattingRecycler.smoothScrollToPosition(chattingRecycler.getAdapter().getItemCount());

                }

                if (messagesArrayList.size()==0|| messagesArrayList==null){

                    firstChat= true;
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}


    private void showImagePickDialog(){
        String[] options={"Gallery"};
       AlertDialog.Builder builder=new AlertDialog.Builder(this);
       builder.setTitle("Choose Image from");
       builder.setItems(options, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               if(which==0){
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
//this method is called when user oress allow or deny form permission request dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
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
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    private void sendImageMessage(Uri image){
        byte[] inputData = new byte[0];
        notify=true;
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("sending image...");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        try {
            InputStream iStream = getContentResolver().openInputStream(image);
            try {
              inputData = getBytes(iStream);
            }
            catch (IOException e) {
                e.printStackTrace();
                //TODO handle error
            }
//            finally {
//                try {
//                    iStream.close();
//                }catch (IOException e){
//
//                }
//            }
        }catch (FileNotFoundException e){
            //TODO handle error
        }
        filePathName= storageReference.child("chatting images").child(image.getLastPathSegment()
                +System.currentTimeMillis());
        filePathName.child("image").putFile(image);
        filePathName.child("bytes").putBytes(inputData);
        List<byte[]> encryptedResult =  encryptImageMessage(inputData);


        // put the encrypted image to firebase storage
        // the key must be stored in the real time database

               filePathName.putBytes(encryptedResult.get(1)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if(task.isSuccessful()){
                          progressDialog.dismiss();
                           imageUrl=task.getResult().getMetadata().getReference().getPath().toString();
                           addToRealTimeDataBase(imageUrl , encryptedResult.get(0));
                       }
                   }
               });

    }
    private void addToRealTimeDataBase(String url , byte[] streamKeyData){
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
        messageTextBody.put("streamKeyData" , Base64.getEncoder().encodeToString(streamKeyData));
        messageTextBody.put("message",url);
        messageTextBody.put("time",saveCurrentTime);
        messageTextBody.put("date",saveCurrentDate);
        messageTextBody.put("type","image");
        messageTextBody.put("from",senderID);
        messageTextBody.put("isSeen" , "false");
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
        rootRef.child("Users").child(recieverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User recieverUser = snapshot.getValue(User.class);
                    if(!recieverUser.getImage().isEmpty()) {

                        GlideApp.with(getApplicationContext())
                                .load(recieverUser.getImage())
                                .fitCenter()
                                .circleCrop()
                                .into(receiverProfileImage);
                        receiverProfileImage.setPadding(0,0,0,0);

                    }
                    receiverUsername.setText(recieverUser.getName());
                    messageSeen(recieverID);
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



    private void messageSeen(final String receiverID){
        rootRef.child("Messages").child(senderID).child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot sp: snapshot.getChildren()){
                        Messages messages=sp.getValue(Messages.class);
                        if(messages!=null) {
                            if (messages.getFrom().equals(senderID) || messages.getFrom().equals(receiverID)) {
                                HashMap<String, Object> hash = new HashMap<>();
                                hash.put("isSeen", "true");
                                sp.getRef().updateChildren(hash);

                            }
                        }
                    }

                    // once the user  sees the messages
                    //update the number of unseen messages in the badge
                MainActivity.setBadgeToNumberOfNotifications(rootRef , mAuth);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void getRecepientCard(String receiverID){
          // get the reciver public key
            OnResultListener<Card> findUsersListener =
                    new OnResultListener<Card>() {
                        @Override
                        public void onSuccess(Card card) {
//                            com.virgilsecurity.common.model.Data data = new com.virgilsecurity.common.model.Data(messageText.getBytes());
//                            // Encrypt data using user public keys
//                            com.virgilsecurity.common.model.Data encryptedData = eThree.authEncrypt(data, findUsersResult);
                            // Encrypt message using user public key
                            recepientVirgilCard = card;
                           //TODO disable send button until cards are recived
                        }
                        @Override
                        public void onError(@NotNull Throwable throwable) {

                        }
                    };
            eThree.findUser(receiverID).addCallback(findUsersListener);

    }
    private void getSenderCard(){

        OnResultListener<Card> findUsersListener =
                new OnResultListener<Card>() {
                    @Override public void onSuccess(Card senderCard) {
                        senderVirgilCard = senderCard;

                        retrieveMessages();
                    }

                    @Override public void onError(@NotNull Throwable throwable) {
                        // Error handling
                    }
                };

// Lookup destination user public keys
        eThree.findUser(mAuth.getCurrentUser().getUid()).addCallback(findUsersListener);


    }


    List<byte[]> encryptImageMessage(byte[] imageByteArray){

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageByteArray);
        int streamSize = imageByteArray.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] streamDataKey = eThree.encryptShared(byteArrayInputStream , streamSize , byteArrayOutputStream);
        byte [] encryptedStreamDataKey =  eThree.authEncrypt(new com.virgilsecurity.common.model.Data(streamDataKey) , recepientVirgilCard).getValue();
        List<byte[]> resultList= new ArrayList<>();
        resultList.add(encryptedStreamDataKey);
        resultList.add(byteArrayOutputStream.toByteArray());
        Toast.makeText(getApplicationContext() , " " + byteArrayOutputStream.toByteArray().length , Toast.LENGTH_SHORT).show();

        return resultList;
    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
