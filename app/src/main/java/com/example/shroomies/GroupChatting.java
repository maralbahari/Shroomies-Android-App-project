package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class GroupChatting extends AppCompatActivity {
    //views
    private ImageButton sendMessage, addImage;
    private EditText messageBody;
    private RecyclerView groupChattingRecycler;
    private ImageView selectedImageView;
    private TextView imageTextView;
    private IOverScrollDecor chattingRecyclerViewDecor;
    private IOverScrollStateListener onOverPullListener;
    private LinearLayoutManager linearLayoutManager;
    // firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    // data structure
    private HashMap<String, User> membersHashmap;
    private GroupChatMessageAdapter groupChatMessageAdapter;
    private ArrayList<GroupMessage> groupMessageList;
    private String apartmentID=new String("");
    //variable
    private int adapterPositionOldestUnSeenMessage=0;
    private GroupMessage oldestUnSeenMessage = new GroupMessage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chatting);
        Toolbar chattingToolbar = findViewById(R.id.group_chat_toolbar);
        sendMessage = findViewById(R.id.group_chat_send_message_button);
        selectedImageView = findViewById(R.id.group_chat_selected_image_view);
        imageTextView  = findViewById(R.id.group_chat_image_text_view);
        addImage = findViewById(R.id.group_chat_choose_file_button);
        messageBody = findViewById(R.id.group_chat_messeg_body_edit_text);
        groupChattingRecycler = findViewById(R.id.group_chat_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        groupChattingRecycler.setLayoutManager(linearLayoutManager);
        groupChattingRecycler.setHasFixedSize(true);
        chattingRecyclerViewDecor = OverScrollDecoratorHelper.setUpOverScroll(groupChattingRecycler, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();
        Bundle bundle= getIntent().getExtras();
        if(bundle!=null) {
            membersHashmap=(HashMap<String, User>) bundle.getSerializable("MEMBERS");
            if(membersHashmap.size()==1){
                // NOT ALLOW SENDING MESSAGE WHEN USER IS ALONE
                Toast.makeText(getApplicationContext(),"THERE IS NO MEMBER TO CHAT WITH",Toast.LENGTH_SHORT).show();
            }else{
                // NEED APARTMENT ID TO SEND THE MESSAGE TO THE MEMBER OF APARTMENT ONLY
                getApartmentID();
            }
        }
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtMessage=messageBody.getText().toString().trim();
                if(validToSendMessage(txtMessage)){
                    sendTextMessage(apartmentID,currentUser.getUid(),membersHashmap,txtMessage);
                }
            }
        });
    }
    private boolean validToSendMessage(String textMessage){
        return !apartmentID.isEmpty() && currentUser!=null && !textMessage.isEmpty();
    }
    private void getApartmentID(){
        if(currentUser!=null){
            currentUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
                    if(task.isSuccessful()){
                        apartmentID=(String) task.getResult().getClaims().get(Config.apartmentID);
                        loadMessages(apartmentID,currentUser.getUid());
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void sendTextMessage(String apartmentID,String currentUserID, HashMap<String, User> membersHashmap,String messageText){
        DatabaseReference messageRef=rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages).push();
        String messageID=messageRef.getKey();
        HashMap messageDetails = new HashMap();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                .withZone(TimeZone.getDefault().toZoneId());
        messageDetails.put("message", messageText);
        messageDetails.put("date",dateFormat.format(ZonedDateTime.now()));
        messageDetails.put("type", "text");
        messageDetails.put("messageID",messageID);
        messageDetails.put("from", currentUserID);
        HashMap<String, Boolean> seenBy = new HashMap<>();
        // add each members id with false next to it
        // this is going to act like the is isseen in the chatting  activity
        for (String id
                : membersHashmap.keySet()) {
            // SENDER ALWAYS SEES THE MESSAGE
            if(id.equals(currentUserID)){
                seenBy.put(id,true);
            }else{
                seenBy.put(id, false);
            }
        }

        messageDetails.put("seenBy", seenBy);
        messageRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull @NotNull Task task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMessages(String apartmentID, String currentUserID){
        groupMessageList=new ArrayList<>();
        groupChatMessageAdapter=new GroupChatMessageAdapter(groupMessageList,getApplicationContext(),membersHashmap);
        HashMap <String,Object> seenMessageHashmaps=new HashMap();
        HashMap<String,Boolean> seenMemberHashmap=new HashMap<>();
        seenMemberHashmap.put(currentUserID,true);
        rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int unSeenCounter=0;
                    for (DataSnapshot messageSnap:snapshot.getChildren()){
                        GroupMessage groupMessage=messageSnap.getValue(GroupMessage.class);
                        if(groupMessage!=null){
                            groupMessageList.add(groupMessage);
                            seenMessageHashmaps.put(groupMessage.getMessageID(),seenMemberHashmap);
                            if(!groupMessage.getSeenBy().get(currentUserID)){
                                if(unSeenCounter==1){
                                    oldestUnSeenMessage =groupMessage;
                                }
                                unSeenCounter++;
                            }
                        }
                    }
                    setSeenMessage(apartmentID,seenMessageHashmaps);
                    groupChatMessageAdapter.notifyDataSetChanged();
                    if(oldestUnSeenMessage !=null){
                        adapterPositionOldestUnSeenMessage=groupMessageList.indexOf(oldestUnSeenMessage);
                        if(adapterPositionOldestUnSeenMessage!=-1){
                            groupChattingRecycler.smoothScrollToPosition(adapterPositionOldestUnSeenMessage);
                        }
                    }else{
                        groupChattingRecycler.smoothScrollToPosition(groupChatMessageAdapter.getItemCount());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),error.getCode()+":"+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setSeenMessage(String apartmentID,HashMap <String,Object> seenMessageHashmaps){
        DatabaseReference seenRef=rootRef.child(Config.groupMessages).child(apartmentID).child(Config.messages);
        seenRef.updateChildren(seenMessageHashmaps).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"seen the message",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}