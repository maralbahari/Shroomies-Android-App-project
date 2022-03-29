package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingMenuLayout;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    private FlowingDrawer drawerLayout;
    private ConstraintLayout rootLayout;
    private Toolbar toolbar;
    private FlowingMenuLayout navigationView;
    private ImageButton myShroomies, inboxButton;
    private TextView usernameDrawer;
    private Button logoutButton , requestsButton, favoriteButton;
    private ImageView profilePic;
    private BottomNavigationView bottomNavigationview;
    private FrameLayout requestButtonFrame;

    private FragmentTransaction ft;
    private FragmentManager fm;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private User user;



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser fUser=mAuth.getCurrentUser();
        logoutButton = findViewById(R.id.logout);
        rootLayout  = findViewById(R.id.root_layout);
        requestsButton = findViewById(R.id.my_requests_menu);
        drawerLayout =  findViewById(R.id.drawerLayout);
        toolbar =  findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        myShroomies=findViewById(R.id.logo_toolbar);
        inboxButton = findViewById(R.id.inbox_button);
        usernameDrawer = findViewById(R.id.drawer_nav_profile_name);
        profilePic = findViewById(R.id.drawer_nav_profile_pic);
        bottomNavigationview = findViewById(R.id.bottomNavigationView);
        requestButtonFrame=findViewById(R.id.drawer_nav_request_button_frame_layout);
        favoriteButton = findViewById(R.id.my_favorite_menu);
        drawerLayout.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        drawerLayout.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == ElasticDrawer.STATE_OPENING|| newState ==ElasticDrawer.STATE_OPEN) {
                    rootLayout.setForeground(new ColorDrawable(ContextCompat.getColor(getApplication(), R.color.dim_background)));
                }else{
                    rootLayout.setForeground(null);

                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {

            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        loadUserDetails();


        requestsButton.setOnClickListener(v -> {
            startActivity(new Intent(this,RequestActivity.class));
            drawerLayout.closeMenu(true);

        });
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                drawerLayout.closeMenu(true);
            }
        });

        logoutButton.setOnClickListener(view -> {
            if (fUser!=null) {
                mAuth.signOut();
                Intent intent= new Intent(getApplicationContext(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
//                EthreeSingleton.getInstance(getApplication() , null , null).clearInstance();
            }
        });

       com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
       .addOnCompleteListener(task -> {
           FirebaseDatabase database=FirebaseDatabase.getInstance();
           FirebaseUser user=mAuth.getCurrentUser();
           String userID= user.getUid();
           DatabaseReference ref= database.getReference("tokens");
           ref.child(userID).setValue(task.getResult());
       });
        bottomNavigationview.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                if (item.getItemId() == R.id.find_roomie_menu) {
                    getFragment(new FindRoomFragment());
                }
                if (item.getItemId() == R.id.user_profile_menu) {
                    getFragment(new UserProfile());
                }
                if (item.getItemId() == R.id.publish_post_menu) {
                    startActivity(new Intent(MainActivity.this,PublishPostActivity.class));
                }
                return true;
            }
        });


        myShroomies.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MyShroomiesActivity.class)));

        inboxButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MessageInbox.class)));
        getFragment(new FindRoomFragment());

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

    private void loadUserDetails(){
        if (mAuth.getCurrentUser()!=null) {
            rootRef.child(Config.users).child(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    user = task.getResult().getValue(User.class);
                  if (user!=null) {
                      if(user.getUsername()==null){
                          finish();
                          startActivity(new Intent(getApplicationContext() , AddUsername.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));

                      }else{
                          usernameDrawer.setText("@"+user.getUsername());
                          int requestNo=user.getRequestCount();
                          requestsButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                              @SuppressLint("UnsafeExperimentalUsageError")
                              @Override
                              public void onGlobalLayout() {
                                  BadgeDrawable badgeDrawable=BadgeDrawable.create(MainActivity.this);
                                  badgeDrawable.setBackgroundColor(getColor(R.color.lightGrey));
                                  badgeDrawable.setBadgeTextColor(Color.WHITE);
                                  BadgeUtils.attachBadgeDrawable(badgeDrawable, requestsButton, requestButtonFrame);
                                  badgeDrawable.setHorizontalOffset(100);
                                  badgeDrawable.setVerticalOffset(70);
                                  badgeDrawable.setHotspotBounds(100,100,100,100);
                                  badgeDrawable.setBadgeGravity(BadgeDrawable.BOTTOM_END);
                                  requestButtonFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                  if (requestNo==0){
                                      badgeDrawable.setVisible(false);
                                  }else{
                                      badgeDrawable.setVisible(true);
                                      badgeDrawable.setNumber(requestNo);
                                  }
                              }
                          });
                          if(user.getImage()!=null){
                              if(!user.getImage().isEmpty()){
                                  GlideApp.with(getApplicationContext())
                                          .load(user.getImage())
                                          .fitCenter()
                                          .circleCrop()
                                          .transition(DrawableTransitionOptions.withCrossFade())
                                          .into(profilePic);
                              }
                          }
                      }
                  }
                }
            });
        }
    }
}