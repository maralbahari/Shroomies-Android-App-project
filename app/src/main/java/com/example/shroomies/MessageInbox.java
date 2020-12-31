package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class MessageInbox extends AppCompatActivity {
   private Button addgroupButton;
  private Toolbar inboxToolbar;
   private RecyclerView userList;
    private FragmentTransaction ft;
    private FragmentManager fm;
    private final List<ReceiverUsers> usersArrayList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageInboxRecycleViewAdapter messageInboxRecycleViewAdapter;
    private String receiverID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_inbox);
        rootRef= FirebaseDatabase.getInstance().getReference();
        inboxToolbar=(Toolbar) findViewById(R.id.message_inbox_toolbar);
        setSupportActionBar(inboxToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView=inflater.inflate(R.layout.toolbar_inbox_layout,null);
        actionBar.setCustomView(actionbarView);
        userList=findViewById(R.id.inbotx_recycler);
        addgroupButton=findViewById(R.id.add_chat_group_button);
        addgroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               CreateChatGroupFragment createChatGroupFragment=new CreateChatGroupFragment();
               createChatGroupFragment.show(getSupportFragmentManager(),"create group dialog 1");
            }
        });
        messageInboxRecycleViewAdapter=new MessageInboxRecycleViewAdapter(usersArrayList,getApplicationContext());
        linearLayoutManager=new LinearLayoutManager(this);
        userList.setHasFixedSize(true);
        userList.setLayoutManager(linearLayoutManager);
        userList.setAdapter(messageInboxRecycleViewAdapter);
        rootRef.child("inboxLists").child(mAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                   ReceiverUsers receiverUsers=snapshot.getValue(ReceiverUsers.class);
                   usersArrayList.add(receiverUsers);
                   messageInboxRecycleViewAdapter.notifyDataSetChanged();
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