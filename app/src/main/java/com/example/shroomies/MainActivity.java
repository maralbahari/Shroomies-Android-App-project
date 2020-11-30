package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle barDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    static BottomNavigationView btm_view;
    FragmentTransaction ft;
    FragmentManager fm;
    ImageView myShroomies;
    TextView usernameDrawer;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String username = extras.getString("USERNAME");
            String email=extras.getString("EMAIL");
            sessionManager=new SessionManager(getApplicationContext(),username);
            sessionManager.createSession(username,email);
            Toast.makeText(this,username,Toast.LENGTH_LONG).show();
        }
        btm_view = findViewById(R.id.bottomNavigationView);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        usernameDrawer=findViewById(R.id.drawer_nav_profile_name);

        setSupportActionBar(toolbar);
        getFragment(new FindRoommate());
        barDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(barDrawerToggle);
        barDrawerToggle.setDrawerIndicatorEnabled(true);
        barDrawerToggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawer(GravityCompat.START);
                if(menuItem.getItemId()==R.id.setting_menu){
                    getFragment(new PrivacySetting());
                }if(menuItem.getItemId()==R.id.my_archive_menu){
                    getFragment(new Archive());
                }if(menuItem.getItemId()==R.id.my_favorite_menu){
                    getFragment(new Favorite());

                }if(menuItem.getItemId()==R.id.my_requests_menu){
                    getFragment(new Request());
                }if(menuItem.getItemId()==R.id.logout){
                    finish();
                    sessionManager.logout();
                    Intent intent = new Intent(MainActivity.this , LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    finish();
                    startActivity(intent);
                    finish();
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
                    getFragment(new MessageInbox());

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