package com.example.shroomies;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null && user.isEmailVerified()){

            startActivity(new Intent(Home.this,MainActivity.class));
            Toast.makeText(getApplicationContext(),user.getEmail(),Toast.LENGTH_SHORT).show();
        }

    }
}
