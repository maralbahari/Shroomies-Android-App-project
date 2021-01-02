package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MessageInbox extends AppCompatActivity {
   private Button addgroupButton;
  private Toolbar inboxToolbar;
   private RecyclerView inboxListRecyclerView;
    private FragmentTransaction ft;
    private FragmentManager fm;
    private  List<String> usersArrayList=new ArrayList<>();
    private  List<Group> groupList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private PrivateInboxRecycleViewAdapter messageInboxRecycleViewAdapter;
    private GroupInboxRecyclerViewAdapter groupInboxRecyclerViewAdapter;
    private String receiverID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private TabLayout inboxTabLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_inbox);
        //database
        rootRef= FirebaseDatabase.getInstance().getReference();
        // find views
        inboxToolbar=(Toolbar) findViewById(R.id.message_inbox_toolbar);
        inboxListRecyclerView =findViewById(R.id.inbotx_recycler);
        addgroupButton=findViewById(R.id.add_chat_group_button);
        inboxTabLayout = findViewById(R.id.tab_layout_inbox_message);

        //set Action bar
        setSupportActionBar(inboxToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        LayoutInflater inflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView=inflater.inflate(R.layout.toolbar_inbox_layout,null);
        actionBar.setCustomView(actionbarView);
        getPrivateChatList();

        // add group button
        addgroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               CreateChatGroupFragment createChatGroupFragment=new CreateChatGroupFragment();
               createChatGroupFragment.show(getSupportFragmentManager(),"create group dialog 1");
            }
        });

        // initiate the tabs
        inboxTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    usersArrayList = new ArrayList<>();
                    getPrivateChatList();
                }
                else if(tab.getPosition()==1){

                    getGroupChatList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }
    public void getPrivateChatList(){
        messageInboxRecycleViewAdapter=new PrivateInboxRecycleViewAdapter(usersArrayList,getApplicationContext());
        linearLayoutManager=new LinearLayoutManager(this);
        inboxListRecyclerView.setHasFixedSize(true);
        inboxListRecyclerView.setLayoutManager(linearLayoutManager);
        inboxListRecyclerView.setAdapter(messageInboxRecycleViewAdapter);
        rootRef.child("inboxLists").child(mAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    for( DataSnapshot ds:snapshot.getChildren()){
                         receiverID=ds.getValue().toString();
                        usersArrayList.add(receiverID);
                    }
//                    ReceiverUsers receiverUsers=snapshot.getValue(ReceiverUsers.class);
//                    usersArrayList.add(receiverUsers);
                    messageInboxRecycleViewAdapter.notifyDataSetChanged();
                }else{ }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void getGroupChatList() {
        groupInboxRecyclerViewAdapter =new GroupInboxRecyclerViewAdapter(groupList,getApplicationContext());
        linearLayoutManager=new LinearLayoutManager(this);
        inboxListRecyclerView.setHasFixedSize(true);
        inboxListRecyclerView.setLayoutManager(linearLayoutManager);
        inboxListRecyclerView.setAdapter(groupInboxRecyclerViewAdapter);
        rootRef.child("GroupChats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userId = mAuth.getInstance().getCurrentUser().getUid();
                    for(DataSnapshot dataSnapshot
                            : snapshot.getChildren()){
                        Group group = dataSnapshot.getValue(Group.class);
                        if(group.getGroupMembers().contains(userId)){
                            groupList.add(group);
                            Toast.makeText(getApplicationContext(),dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    groupInboxRecyclerViewAdapter = new GroupInboxRecyclerViewAdapter(groupList,getApplicationContext());
                    groupInboxRecyclerViewAdapter.notifyDataSetChanged();
                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}