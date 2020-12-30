package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChattingActivity extends AppCompatActivity {
    private Button sendMessage,addImage,backButton;
    private EditText messageBody;
    private Toolbar chattingToolbar;
    private ImageView receiverProfileImage;
    private TextView receiverUsername;
    private RecyclerView chattingRecycler;
    private String receiverID,receiverName;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String senderID;
    private String saveCurrentDate,saveCurrentTime;
    private final List<Messages> messagesArrayList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        receiverUsername=findViewById(R.id.receiver_username);
        mAuth=FirebaseAuth.getInstance();
        senderID=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference("shroomies-e34d3");
        Bundle extras = getIntent().getExtras();
        if(!(extras==null)){
            receiverID=extras.getString("USERID");
            receiverName=extras.getString("USERNAME");
            receiverUsername.setText(receiverName);
        }
        initializeViews();
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToUser();
            }
        });


//        retrieveMessages();

    }
    private void initializeViews(){
        chattingToolbar= findViewById(R.id.chat_toolbar);
        setSupportActionBar(chattingToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView=inflater.inflate(R.layout.toolbar_chatting_layout,null);
        sendMessage=findViewById(R.id.send_message_text_button);
        addImage=findViewById(R.id.add_image_button_chat);
        backButton=findViewById(R.id.back_button_chatting);
        messageBody=findViewById(R.id.messeg_body);
        chattingRecycler=findViewById(R.id.recycler_view_chatting);
        receiverProfileImage=findViewById(R.id.receiver_image_profile);
        receiverUsername.setText(receiverName);
        chattingRecycler=findViewById(R.id.recycler_view_chatting);
        messagesAdapter=new MessagesAdapter(messagesArrayList);
        linearLayoutManager=new LinearLayoutManager(this);
        chattingRecycler.setHasFixedSize(true);
        chattingRecycler.setLayoutManager(linearLayoutManager);
        chattingRecycler.setAdapter(messagesAdapter);

    }
    private void sendMessageToUser(){
        String messageText=messageBody.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(getApplicationContext(),"please enter a message",Toast.LENGTH_LONG).show();
        }else{
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
                       Toast.makeText(getApplicationContext(),"id: "+messagePushId ,Toast.LENGTH_SHORT).show();
                       messageBody.setText("");
                   }else{
                       String message =task.getException().getMessage();
                       Toast.makeText(getApplicationContext(),"Error "+message,Toast.LENGTH_SHORT).show();
                       messageBody.setText("");
                   }

               }
           });
        }
    }
   public void retrieveMessages(){
        rootRef.child("Messages").child(senderID).child(receiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    Messages messages = snapshot.getValue(Messages.class);
                    messagesArrayList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                }else{

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
}