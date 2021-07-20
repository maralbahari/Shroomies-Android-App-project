package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.shroomies.notifications.FirebaseMessaging;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingMenuLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FlowingDrawer drawerLayout;
//    ActionBarDrawerToggle barDrawerToggle;
    Toolbar toolbar;
    FlowingMenuLayout navigationView;
    static BottomNavigationView btm_view;
    FragmentTransaction ft;
    FragmentManager fm;
    ImageView myShroomies;
    TextView usernameDrawer;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    SessionManager sessionManager;
    FirebaseMessaging firebaseMessaging;
    BadgeDrawable inboxNotificationBadge;
    DatabaseReference rootRef;
    ImageView profilePic;
    View headerView;
    DatabaseReference myRef;
    User user;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Creating session in main activity
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String userID = extras.getString("ID");
            String userEmail=extras.getString("EMAIL");
            sessionManager=new SessionManager(getApplicationContext(),userID);
            sessionManager.createSession(userID,userEmail);
            loadUserDetails();

        }
        firebaseMessaging = new FirebaseMessaging();
        firebaseMessaging.onNewToken(FirebaseInstanceId.getInstance().getToken());

        btm_view = findViewById(R.id.bottomNavigationView);
        drawerLayout =  findViewById(R.id.drawerLayout);
        drawerLayout.setTouchMode(ElasticDrawer.TOUCH_MODE_FULLSCREEN);
        toolbar =  findViewById(R.id.toolbar);

        getFragment(new FindRoommate());
        setSupportActionBar(toolbar);
//
//        barDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
//        barDrawerToggle.syncState();
//        drawerLayout.addDrawerListener(barDrawerToggle);
//        barDrawerToggle.setDrawerIndicatorEnabled(true);

//        navigationView =  findViewById(R.id.navigationView);
////        headerView = navigationView.getHeaderView(0);
        getSupportActionBar().setTitle(null);
//
//        navigationView.ad
//
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//                drawerLayout.closeMenu();
//                if(menuItem.getItemId()==R.id.shroomies_menu){
//                    startActivity(new Intent( getApplicationContext(), MyShroomiesFragment.class));
//                }if(menuItem.getItemId()==R.id.my_archive_menu){
//                    getFragment(new Archive());
//                }if(menuItem.getItemId()==R.id.my_favorite_menu){
//                    getFragment(new Favorite());
//
//                }if(menuItem.getItemId()==R.id.my_requests_menu){
//                    getFragment(new Request());
//                }if(menuItem.getItemId()==R.id.logout){
//                    sessionManager.logout();
//                    Intent intent= new Intent(getApplicationContext(),LoginActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                }
//                return false;
//            }
//        });
        btm_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.find_roomie_menu){
                    getFragment(new FindRoommate());
                }if(menuItem.getItemId()==R.id.publish_post_menu){
                    getFragment(new PublishPost());
                }if(menuItem.getItemId()==R.id.message_inbox_menu){
                    Intent intent= new Intent(getApplicationContext(),MessageInbox.class);
                    startActivity(intent);
                }if(menuItem.getItemId()==R.id.user_profile_menu){
                    getFragment(new UserProfile());
                }
                return true;
            }
        });
        myShroomies=findViewById(R.id.logo_toolbar);
        myShroomies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent( getApplicationContext(), MyShroomiesActivity.class));
            }
        });
        setBadgeToNumberOfNotifications(rootRef,mAuth);



    }


    private void getFragment (Fragment fragment) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    static void setBadgeToNumberOfNotifications(final DatabaseReference rootRef , final FirebaseAuth mAuth){
        // get the number of unseen
        // private messages
        final List<Messages> unSeenMessageList = new ArrayList<>();
        rootRef.child("Messages").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot
                            :snapshot.getChildren()){
                        for (DataSnapshot dataSnapshot1:
                                dataSnapshot.getChildren()){
                            Messages message= dataSnapshot1.getValue(Messages.class);
                            if (!message.getIsSeen().equals("true")){
                                unSeenMessageList.add(message);
                            }

                        }
                    }


                }
                getUnseenGroupMessages(rootRef, mAuth,unSeenMessageList.size());
                unSeenMessageList.clear();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private static  void getUnseenGroupMessages(final DatabaseReference  rootRef , final FirebaseAuth mAuth, final int unseenPrivetMsgs ){
        //get the number of unseen group messages
        final ArrayList<String> unseenGroupMessages  = new ArrayList<>();
        rootRef.child("GroupChatList").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (final DataSnapshot ds:
                            snapshot.getChildren()) {
                        rootRef.child("GroupChats").child(ds.getKey()).child("Messages").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    unseenGroupMessages.clear();
                                    for (DataSnapshot dataSnapshot
                                            :snapshot.getChildren()){
                                        for (DataSnapshot snapshot1
                                                :dataSnapshot.child("seenBy").getChildren()){
                                            if(snapshot1.getKey().equals(mAuth.getInstance().getCurrentUser().getUid())&&snapshot1.getValue().equals("false")){
                                                unseenGroupMessages.add(snapshot1.getValue().toString());
                                            }
                                        }
                                    }

                                    btm_view.getOrCreateBadge(R.id.message_inbox_menu).setNumber(unseenGroupMessages.size()+unseenPrivetMsgs);

                                }
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
//    public void updateNavHead(){
//        usernameDrawer= headerView.findViewById(R.id.drawer_nav_profile_name);
//        profilePic = headerView.findViewById(R.id.drawer_nav_profile_pic);
//                    usernameDrawer.setText(user.getName());
//                //accept request here crashes
//                    if (user.getImage()!=null){
//                        Glide.with(getApplicationContext()).
//                                load(user.getImage())
//                                .fitCenter()
//                                .circleCrop()
//                                .into(profilePic);
//                    }
//
//
//
//    }
    private void loadUserDetails(){
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    user=snapshot.getValue(User.class);
//                    updateNavHead();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




}