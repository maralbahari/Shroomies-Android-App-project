package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    public static String updatedAdresses;
    public static LatLng updatedLatLng;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle barDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    static BottomNavigationView btm_view;
    FragmentTransaction ft;
    FragmentManager fm;
    ImageView myShroomies;
    TextView usernameDrawer;
    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Creating session in main activity
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String userID = extras.getString("ID");
            String userEmail=extras.getString("EMAIL");
            sessionManager=new SessionManager(getApplicationContext(),userID);
            sessionManager.createSession(userID,userEmail);

        }
        btm_view = findViewById(R.id.bottomNavigationView);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        usernameDrawer=findViewById(R.id.drawer_nav_profile_name);
        setSupportActionBar(toolbar);
        getFragment(new FindRoommate());
        barDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        barDrawerToggle.syncState();
        drawerLayout.addDrawerListener(barDrawerToggle);
        barDrawerToggle.setDrawerIndicatorEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawer(GravityCompat.START);
                if(menuItem.getItemId()==R.id.setting_menu){
                    getFragment(new ShroomiesManager());
                }if(menuItem.getItemId()==R.id.my_archive_menu){
                    getFragment(new Archive());
                }if(menuItem.getItemId()==R.id.my_favorite_menu){
                    getFragment(new Favorite());

                }if(menuItem.getItemId()==R.id.my_requests_menu){
                    getFragment(new Request());
                }if(menuItem.getItemId()==R.id.logout){
                    sessionManager.logout();
                    Intent intent= new Intent(getApplicationContext(),LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                return false;
            }
        });
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
                getFragment(new MyShroomies());
            }
        });

    }
    private void getFragment (Fragment fragment) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

}