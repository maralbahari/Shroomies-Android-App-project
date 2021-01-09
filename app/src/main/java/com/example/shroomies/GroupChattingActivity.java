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

public class GroupChattingActivity extends AppCompatActivity {
    private Button sendMessage,addImage,backButton;
    private EditText messageBody;
    private Toolbar chattingToolbar;
    private ImageView groupImage;
    private TextView groupNameTextview;
    private RecyclerView chattingRecycler;
    private String groupID,groupName;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String senderID;
    private String saveCurrentDate,saveCurrentTime;
    private final List<Group> groupMessagesArrayList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessagesAdapter groupMessagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chatting);
        groupNameTextview=findViewById(R.id.receiver_username);
        mAuth=FirebaseAuth.getInstance();
        senderID=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        chattingToolbar= findViewById(R.id.chat_toolbar);
        setSupportActionBar(chattingToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView=inflater.inflate(R.layout.toolbar_chatting_layout,null);
        sendMessage=findViewById(R.id.send_message_button_group_chat);
        addImage=findViewById(R.id.choose_file_group_chat);
        backButton=findViewById(R.id.back_button_chatting);
        messageBody=findViewById(R.id.messeg_body_group_chat);
        chattingRecycler=findViewById(R.id.recycler_view_group_chatting);
        groupImage=findViewById(R.id.receiver_image_profile);
        groupNameTextview.setText(groupName);
        chattingRecycler=findViewById(R.id.recycler_view_group_chatting);
        groupMessagesAdapter=new GroupMessagesAdapter(groupMessagesArrayList);
        linearLayoutManager=new LinearLayoutManager(this);
        chattingRecycler.setHasFixedSize(true);
        chattingRecycler.setLayoutManager(linearLayoutManager);
        chattingRecycler.setAdapter(groupMessagesAdapter);
        Bundle extras = getIntent().getExtras();
        if(!(extras==null)){
            groupID=extras.getString("GROUPID");
            groupName=extras.getString("GROUPNAME");
            groupNameTextview.setText(groupName);
            loadMessages();
        }

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToGroup();
            }
        });

    }
    public void sendMessageToGroup(){
        String messageText=messageBody.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(getApplicationContext(),"please enter a message",Toast.LENGTH_LONG).show();
        }else {
            DatabaseReference reference = rootRef.child("GroupChats").child(groupID).child("Messages").push();
            //now making a unique id for each single message so that they wont be replaced and we save everything
            String messagePushId=reference.getKey();
            Calendar calendarDate=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate=currentDate.format(calendarDate.getTime());
            Calendar calendarTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm aa");
            saveCurrentTime=currentTime.format(calendarTime.getTime());
            //unique name for each message
            final String uniqueName=messagePushId+saveCurrentDate+saveCurrentTime;
            Map messageDetails= new HashMap();
            messageDetails.put("message",messageText);
            messageDetails.put("time",saveCurrentTime);
            messageDetails.put("date",saveCurrentDate);
            messageDetails.put("type","text");
            messageDetails.put("from",senderID);
            reference.child(uniqueName).setValue(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"id: "+uniqueName ,Toast.LENGTH_SHORT).show();
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
        void loadMessages(){
            rootRef.child("GroupChats").child(groupID).child("Messages").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot.exists()){
                        for(DataSnapshot dataSnapshot
                        :snapshot.getChildren()) {
                            Group groupMessages = dataSnapshot.getValue(Group.class);
                            groupMessagesArrayList.add(groupMessages);
                            groupMessagesAdapter.notifyDataSetChanged();
                        }
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
