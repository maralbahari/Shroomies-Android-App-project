package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.shroomies.notifications.MyFirebaseMessaging;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingMenuLayout;
import org.jetbrains.annotations.NotNull;
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
    ImageButton inboxButton;

    TextView usernameDrawer;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    SessionManager sessionManager;
    MyFirebaseMessaging myFirebaseMessaging;
    BadgeDrawable inboxNotificationBadge;
    DatabaseReference rootRef;
    ImageView profilePic;
    View headerView;
    DatabaseReference myRef;
    User user;
    Button logoutButton , requestsButton;
    FirebaseUser fUser;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Creating session in main activity
//        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
//        mAuth.useEmulator("10.0.2.2" , 9099);


        FirebaseUser fUser=mAuth.getCurrentUser();

        logoutButton = findViewById(R.id.logout);
        requestsButton = findViewById(R.id.my_requests_menu);
        btm_view = findViewById(R.id.bottomNavigationView);
        drawerLayout =  findViewById(R.id.drawerLayout);
        toolbar =  findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        myShroomies=findViewById(R.id.logo_toolbar);
        inboxButton = findViewById(R.id.inbox_button);

        drawerLayout.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);


        requestsButton.setOnClickListener(v -> {
            getFragment(new RequestFragment());
            drawerLayout.closeMenu(true);

        });

        logoutButton.setOnClickListener(view -> {
            if (fUser!=null) {
                mAuth.signOut();
                Intent intent= new Intent(getApplicationContext(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                EthreeSingleton.getInstance(getApplication() , null , null).clearInstance();
            }
        });
//        mAuth.useEmulator("http://localhost:4000/auth",9099);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String userID = extras.getString("ID");
            String userEmail=extras.getString("EMAIL");
//            sessionManager=new SessionManager(getApplicationContext(),userID);
//            sessionManager.createSession(userID,userEmail);

            loadUserDetails();
        }
       com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
       .addOnCompleteListener(new OnCompleteListener<String>() {
           @Override
           public void onComplete(@NonNull @NotNull Task<String> task) {
               FirebaseDatabase database=FirebaseDatabase.getInstance();
//               database.useEmulator("10.0.2.2",9000);
               FirebaseUser user=mAuth.getCurrentUser();
               String userID= user.getUid();
               DatabaseReference ref= database.getReference("tokens");
//               Token token= new Token(task.getResult().toString());
               ref.child(userID).setValue(task.getResult());
           }
       });
//        getFragment(new FindRoommate());
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
//                    getFragment(new ArchiveFragment());
//                }if(menuItem.getItemId()==R.id.my_favorite_menu){
//                    getFragment(new Favorite());
//
//                }if(menuItem.getItemId()==R.id.my_requests_menu){
//                    getFragment(new RequestFragment());
//                }if(menuItem.getItemId()==R.id.logout){
//                    sessionManager.logout();

//                }
//                return false;
//            }
//        });
        btm_view.setOnItemSelectedListener(item -> {
            if(item.getItemId()==R.id.find_roomie_menu){
                getFragment(new FindRoommate());
            }if(item.getItemId()==R.id.publish_post_menu){
                getFragment(new PublishPost());
            }if(item.getItemId()==R.id.user_profile_menu){
                getFragment(new UserProfile());
            }
            return true;
        });



        myShroomies.setOnClickListener(v -> startActivity(new Intent( getApplicationContext(), MyShroomiesActivity.class)));
//        setBadgeToNumberOfNotifications(rootRef,mAuth);

        inboxButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext() , MessageInbox.class)));

    }


    private void getFragment (Fragment fragment) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

//    static void setBadgeToNumberOfNotifications(final DatabaseReference rootRef , final FirebaseAuth mAuth){
//        // get the number of unseen
//        // private messages
//        final List<Messages> unSeenMessageList = new ArrayList<>();
//        rootRef.child("Messages").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    for (DataSnapshot dataSnapshot
//                            :snapshot.getChildren()){
//                        for (DataSnapshot dataSnapshot1:
//                                dataSnapshot.getChildren()){
//                            Messages message= dataSnapshot1.getValue(Messages.class);
//                            if (!message.getIsSeen()){
//                                unSeenMessageList.add(message);
//                            }
//
//                        }
//                    }
//
//
//                }
////                getUnseenGroupMessages(rootRef, mAuth,unSeenMessageList.size());
//                unSeenMessageList.clear();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//    private static  void getUnseenGroupMessages(final DatabaseReference  rootRef , final FirebaseAuth mAuth, final int unseenPrivetMsgs ){
//        //get the number of unseen group messages
//        final ArrayList<String> unseenGroupMessages  = new ArrayList<>();
//        rootRef.child("GroupChatList").child(mAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
//                    for (final DataSnapshot ds:
//                            snapshot.getChildren()) {
//                        rootRef.child("GroupChats").child(ds.getKey()).child("Messages").addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.exists()){
//                                    unseenGroupMessages.clear();
//                                    for (DataSnapshot dataSnapshot
//                                            :snapshot.getChildren()){
//                                        for (DataSnapshot snapshot1
//                                                :dataSnapshot.child("seenBy").getChildren()){
//                                            if(snapshot1.getKey().equals(mAuth.getInstance().getCurrentUser().getUid())&&snapshot1.getValue().equals("false")){
//                                                unseenGroupMessages.add(snapshot1.getValue().toString());
//                                            }
//                                        }
//                                    }
//
////                                    btm_view.getOrCreateBadge(R.id.message_inbox_menu).setNumber(unseenGroupMessages.size()+unseenPrivetMsgs);
//
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//                    }
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//    }
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
//        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    user=snapshot.getValue(User.class);
////                    updateNavHead();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }




}