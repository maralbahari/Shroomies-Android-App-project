package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import com.example.shroomies.notifications.Data;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageInbox extends AppCompatActivity {
   private ImageButton addgroupButton;
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
                    groupList = new ArrayList<>();
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
        usersArrayList = new ArrayList<>();
        messageInboxRecycleViewAdapter=new PrivateInboxRecycleViewAdapter(usersArrayList,getApplicationContext());
        linearLayoutManager=new LinearLayoutManager(this);
        inboxListRecyclerView.setHasFixedSize(true);
        inboxListRecyclerView.setLayoutManager(linearLayoutManager);
        inboxListRecyclerView.setAdapter(messageInboxRecycleViewAdapter);
        rootRef.child("PrivateChatList").child(mAuth.getInstance().getCurrentUser().getUid()).orderByChild("receiverID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        HashMap<String,String> recivers= (HashMap) ds.getValue();
                        usersArrayList.add(recivers.get("receiverID"));
                        Toast.makeText(getApplicationContext(), recivers.toString(), Toast.LENGTH_SHORT).show();
                    }

                    messageInboxRecycleViewAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getGroupChatList() {
        groupList = new ArrayList<>();
        groupInboxRecyclerViewAdapter =new GroupInboxRecyclerViewAdapter(groupList,getApplicationContext());
        linearLayoutManager=new LinearLayoutManager(this);
        inboxListRecyclerView.setHasFixedSize(true);
        inboxListRecyclerView.setLayoutManager(linearLayoutManager);
        inboxListRecyclerView.setAdapter(groupInboxRecyclerViewAdapter);

        rootRef.child("GroupChatList").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        rootRef.child("GroupChats").child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Group group = snapshot.getValue(Group.class);
                                groupList.add(group);
                                groupInboxRecyclerViewAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

}