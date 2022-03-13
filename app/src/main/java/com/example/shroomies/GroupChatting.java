package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimeZone;

public class GroupChatting extends AppCompatActivity {
    private ImageButton addImage;
    private EditText messageBody;
    private RecyclerView groupChattingRecycler;
    private ImageView selectedImageView;
    private TextView imageTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout cardReplyLayout;
    private RelativeLayout noMessageLayout;
    private LottieAnimationView noMessageMushroom;
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    // data structure
    private HashMap<String, User> membersHashmap=new HashMap<>();
    private GroupChatMessageAdapter groupChatMessageAdapter;
    private ArrayList<GroupMessage> groupMessageList=new ArrayList<>();
    private ExpensesCard expensesCard=new ExpensesCard();
    private TasksCard tasksCard=new TasksCard();

    //variables
    private Uri chosenImage;
    private String[] storagePermissions;
    private int adapterPositionOldestUnSeenMessage=0;
    private GroupMessage oldestUnSeenMessage = new GroupMessage();
    private int unSeenCounter=0;
    private int currentPage=1;
    private int messagePos=0;
    private String lastMessageID="";
    private String prevMessageID="";
    private String imageUrl;
    private String apartmentID= "";


    // static variables
    private static final int TOTAL_ITEMS_TO_LAOD=10;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;



    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chatting);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser= mAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();
        Toolbar toolbar=findViewById(R.id.group_chatting_activity_toolbar);
        toolbar.setTitleTextColor(this.getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ImageButton sendMessage = findViewById(R.id.group_chat_send_message_button);
        selectedImageView = findViewById(R.id.group_chat_selected_image_view);
        imageTextView  = findViewById(R.id.group_chat_image_text_view);
        addImage = findViewById(R.id.group_chat_choose_file_button);
        messageBody = findViewById(R.id.group_chat_messeg_body_edit_text);
        groupChattingRecycler = findViewById(R.id.group_chat_recycler_view);
        swipeRefreshLayout=findViewById(R.id.group_message_swipe_layout);
        cardReplyLayout=findViewById(R.id.reply_card_message_layout);
        TextView cardTitleView = findViewById(R.id.reply_message_title);
        View cardImportanceView = findViewById(R.id.reply_message_importance);
        TextView cardTypeView = findViewById(R.id.reply_message_type);
        ImageButton dismissReply = findViewById(R.id.dismiss_reply);
        noMessageLayout=findViewById(R.id.no_message_layout);
//        noMessageMushroom=findViewById(R.id.no_message_anim);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        groupChattingRecycler.setLayoutManager(linearLayoutManager);
        groupChattingRecycler.setHasFixedSize(true);
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //bundle
        Bundle bundle= getIntent().getExtras();
        if(bundle!=null) {
            membersHashmap=(HashMap<String, User>) bundle.getBundle("extras").getSerializable("MEMBERS");
            expensesCard=bundle.getParcelable("ExpenseCARD");
            tasksCard=bundle.getParcelable("TaskCARD");
            if (expensesCard!=null){
                addImage.setVisibility(View.GONE);
                cardReplyLayout.setVisibility(View.VISIBLE);
                cardTitleView.setText(expensesCard.getTitle());
                cardTypeView.setText("Expense Card");
                String importanceColor=expensesCard.getImportance();
                switch (importanceColor) {
                    case  "2":
                        cardImportanceView.setBackgroundColor(this.getColor(R.color.canceRed));
                        break;
                    case  "1":
                        cardImportanceView.setBackgroundColor(this.getColor(R.color.orange));
                        break;
                    default:
                        cardImportanceView.setBackgroundColor(this.getColor(R.color.okGreen));
                }

            } if (tasksCard!=null) {
                addImage.setVisibility(View.GONE);
                cardReplyLayout.setVisibility(View.VISIBLE);
                cardTitleView.setText(tasksCard.getTitle());
                cardTypeView.setText("Task Card");
                String importanceColor=tasksCard.getImportance();
                switch (importanceColor) {
                    case  "2":
                        cardImportanceView.setBackgroundColor(this.getColor(R.color.canceRed));
                        break;
                    case  "1":
                        cardImportanceView.setBackgroundColor(this.getColor(R.color.orange));
                        break;
                    default:
                        cardImportanceView.setBackgroundColor(this.getColor(R.color.okGreen));
                }
            }
            if(membersHashmap.size()==1){
                // NOT ALLOW SENDING MESSAGE WHEN USER IS ALONE
                new CustomToast(this,"There is no member to chat with");
            }else{
                // NEED APARTMENT ID TO SEND THE MESSAGE TO THE MEMBER OF APARTMENT ONLY
                getApartmentID();
            }
        }
        sendMessage.setOnClickListener(view1 -> {
            String txtMessage=messageBody.getText().toString().trim();
            if(validToSendTextMessage(txtMessage)){
                sendOutMessage(currentUser.getUid(),txtMessage,"text");
            } if (validToSendImageMessage()) {
                sendImageMessage(currentUser.getUid(),chosenImage);
            } if (validToSendExpenseCardMessage(txtMessage)){
                sendOutReplyMessage(currentUser.getUid(),txtMessage,"expensesCard");
            }if(validToSendTaskCardMessage(txtMessage)){
                sendOutReplyMessage(currentUser.getUid(),txtMessage,"taskCard");
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if(apartmentID!=null){
                currentPage++;
                messagePos=0;
                loadMoreMessages();
            }
        });
        addImage.setOnClickListener(view14 -> showImagePickDialog());
        dismissReply.setOnClickListener(view12 -> {
            cardReplyLayout.setVisibility(View.GONE);
            addImage.setVisibility(View.VISIBLE);
            expensesCard=null;
            tasksCard=null;

        });
    }


    private boolean validToSendTextMessage(String textMessage){
        return !apartmentID.isEmpty() && currentUser!=null && !textMessage.isEmpty() && expensesCard==null && tasksCard==null;
    }
    private boolean validToSendImageMessage(){
        return  !apartmentID.isEmpty() && currentUser!=null && chosenImage!=null;
    }
    private boolean validToSendExpenseCardMessage(String textMessage){
        return expensesCard!=null && currentUser!=null && !textMessage.isEmpty() && tasksCard==null && !apartmentID.isEmpty();
    }
    private boolean validToSendTaskCardMessage(String textMessage){
        return expensesCard==null && currentUser!=null && !textMessage.isEmpty() && tasksCard!=null && !apartmentID.isEmpty();
    }
    private void getApartmentID(){
        if(currentUser!=null){
            currentUser.getIdToken(true).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    apartmentID=(String) task.getResult().getClaims().get(Config.apartmentID);
                    loadMessages(apartmentID,currentUser.getUid());
                }

            }).addOnFailureListener(e -> new CustomToast(this,e.getMessage()));
        }
    }
    private void sendOutReplyMessage(String currentUserID,String messageContent,String messageType){
        DatabaseReference messageRef=rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages).push();
        String messageID=messageRef.getKey();
        HashMap messageDetails = new HashMap();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                .withZone(TimeZone.getDefault().toZoneId());
        if (messageType.equals("expensesCard")){
            messageDetails.put("cardID",expensesCard.getCardID());
            messageDetails.put("cardTitle",expensesCard.getTitle());
            messageDetails.put("cardImportance",expensesCard.getImportance());
        } if (messageType.equals("taskCard")){
            messageDetails.put("cardID",tasksCard.getCardID());
            messageDetails.put("cardTitle",tasksCard.getTitle());
            messageDetails.put("cardImportance",tasksCard.getImportance());
        }
        messageDetails.put("message", messageContent);
        messageDetails.put("date",dateFormat.format(ZonedDateTime.now()));
        messageDetails.put("type", messageType);
        messageDetails.put("messageID",messageID);
        messageDetails.put("from", currentUserID);
        HashMap<String, Boolean> seenBy = new HashMap<>();
        // add each members id with false next to it
        // this is going to act like the is isseen in the chatting  activity
        for (String id : membersHashmap.keySet()) {
            if(currentUserID.equals(id)){
                seenBy.put(id, true);

            } else{
                seenBy.put(id, false);
            }
        }
        messageDetails.put("seenBy", seenBy);
        messageRef.updateChildren(messageDetails).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                cardReplyLayout.setVisibility(View.GONE);
                expensesCard=null;
                tasksCard=null;
                addImage.setVisibility(View.VISIBLE);
                messageBody.setText("");
            }
        }).addOnFailureListener(e -> new CustomToast(this,e.getMessage()));
    }
    private void loadMoreMessages(){
        DatabaseReference messageRef=rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages);
        Query messageQuery=messageRef.orderByKey().endAt(lastMessageID).limitToLast(currentPage*TOTAL_ITEMS_TO_LAOD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                if(snapshot.exists()){
                    GroupMessage groupMessage=snapshot.getValue(GroupMessage.class);
                    if(groupMessage!=null){
                        if (!prevMessageID.equals(groupMessage.getMessageID())){
                            groupMessageList.add(messagePos++,groupMessage);
                        } else{
                            prevMessageID=lastMessageID;
                        }
                        if(messagePos==1){
                            lastMessageID=groupMessage.getMessageID();
                        }
                    }
                    groupChatMessageAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);

                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    new CustomToast(GroupChatting.this,"No more message exist");
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void sendOutMessage(String currentUserID, String messageContent,String messageType){
        DatabaseReference messageRef=rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages).push();
        String messageID=messageRef.getKey();
        HashMap messageDetails = new HashMap();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                .withZone(TimeZone.getDefault().toZoneId());
        messageDetails.put("message", messageContent);
        messageDetails.put("date",dateFormat.format(ZonedDateTime.now()));
        messageDetails.put("type", messageType);
        messageDetails.put("messageID",messageID);
        messageDetails.put("from", currentUserID);
        HashMap<String, Boolean> seenBy = new HashMap<>();
        // add each members id with false next to it
        // this is going to act like the is isseen in the chatting  activity
        for (String id : membersHashmap.keySet()) {
            if(currentUserID.equals(id)){
                seenBy.put(id, true);

            } else{
                seenBy.put(id, false);
            }
        }
        messageDetails.put("seenBy", seenBy);
        messageRef.updateChildren(messageDetails).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(messageType.equals("image")){
                    selectedImageView.setVisibility(View.GONE);
                    messageBody.setVisibility(View.VISIBLE);
                    addImage.setVisibility(View.VISIBLE);
                    imageTextView.setVisibility(View.GONE);
                }else{
                    messageBody.setText("");
                }
            }
        }).addOnFailureListener(e -> new CustomToast(GroupChatting.this,e.getMessage()));
    }
    private void loadMessages(String apartmentID, String currentUserID){
        groupMessageList=new ArrayList<>();
        groupChatMessageAdapter=new GroupChatMessageAdapter(groupMessageList,this,membersHashmap);
        groupChattingRecycler.setAdapter(groupChatMessageAdapter);
        HashMap <String,Object> seenMessageHashmaps=new HashMap();
        DatabaseReference messageRef=rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages);
        Query messageQuery=messageRef.limitToLast(currentPage*TOTAL_ITEMS_TO_LAOD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                if(snapshot.exists()){
                    groupChattingRecycler.setVisibility(View.VISIBLE);
                    noMessageLayout.setVisibility(View.GONE);
                    noMessageMushroom.pauseAnimation();
                        GroupMessage groupMessage=snapshot.getValue(GroupMessage.class);
                        if(groupMessage!=null){
                            messagePos++;
                            if(messagePos==1){
                                lastMessageID=groupMessage.getMessageID();
                                prevMessageID=groupMessage.getMessageID();
                            }
                            groupMessageList.add(groupMessage);
                            //counting for unSeen messages
                            if(!groupMessage.getSeenBy().get(currentUserID)){
                                seenMessageHashmaps.put(groupMessage.getMessageID()+"/seenBy/"+currentUserID,true);
                                unSeenCounter++;
                                if(unSeenCounter==1){
                                    //getting first or oldest unSeen Message
                                    oldestUnSeenMessage =groupMessage;
                                    Log.d("SHROOMIES MESSAGES",groupMessage.getMessage());
                                }
                            }

                        }
                    groupChatMessageAdapter.notifyDataSetChanged();
                    if(oldestUnSeenMessage !=null){
                        adapterPositionOldestUnSeenMessage=groupMessageList.indexOf(oldestUnSeenMessage);
                        if(adapterPositionOldestUnSeenMessage!=-1){
                            groupChattingRecycler.smoothScrollToPosition(adapterPositionOldestUnSeenMessage);
                        }
                    }else{
                        groupChattingRecycler.smoothScrollToPosition(groupChatMessageAdapter.getItemCount());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    if(seenMessageHashmaps.size()>=1) {
                        setSeenMessage(apartmentID,seenMessageHashmaps);
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {


            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    groupChattingRecycler.setVisibility(View.GONE);
                    noMessageLayout.setVisibility(View.VISIBLE);
                    noMessageMushroom.playAnimation();
                }else{
                    groupChattingRecycler.setVisibility(View.VISIBLE);
                    noMessageLayout.setVisibility(View.GONE);
                    noMessageMushroom.pauseAnimation();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void setSeenMessage(String apartmentID,HashMap <String,Object> seenMessageHashmaps){
        DatabaseReference seenRef=rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages);
        seenRef.updateChildren(seenMessageHashmaps);
    }

    private void showImagePickDialog() {
        String[] options = {"Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkStoragePermisson()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermisson() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    //this method is called when user press allow or deny form permission request dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                        new CustomToast(GroupChatting.this, " storage permission is neccessary....");
                        pickFromGallery();

                    }
                }
            }
            break;
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if ( resultCode== RESULT_OK && requestCode == IMAGE_PICK_GALLERY_CODE) {
            if (data != null) {
                chosenImage = data.getData();
                GlideApp.with(this)
                        .load(chosenImage)
                        .transform(new CenterCrop(),new RoundedCorners(20) )
                        .error(R.drawable.ic_no_file_added)
                        .into(selectedImageView);
                selectedImageView.setVisibility(View.VISIBLE);
                imageTextView.setVisibility(View.VISIBLE);
                addImage.setVisibility(View.GONE);
                messageBody.setVisibility(View.GONE);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(String userID, Uri image) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("sending image...");
        progressDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        //load the image as a bitmap
        Bitmap bitmap = null;

        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), image);
            try {
                bitmap = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();

        StorageReference filePathName = storageReference.child("shroomies apartment images").child(apartmentID).child("messages images").child(image.getLastPathSegment()
                + System.currentTimeMillis());


        filePathName.putBytes(byteArray).addOnCompleteListener(task -> {
            chosenImage=null;
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                task.getResult().getMetadata().getReference().getDownloadUrl().addOnCompleteListener(task1 -> {
                    imageUrl= task1.getResult().toString();
                    sendOutMessage(userID,imageUrl,"image");
                });
            }
        });

    }

}
