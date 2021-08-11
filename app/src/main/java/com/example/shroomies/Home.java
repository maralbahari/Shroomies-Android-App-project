package com.example.shroomies;

import android.app.Application;
import android.content.Intent;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Home extends Application {
    FirebaseAuth mAuth;
    @Override
    public void onCreate() {
        super.onCreate();
        mAuth=FirebaseAuth.getInstance();
                FirebaseUser user=mAuth.getCurrentUser();
                if(user!=null && user.isEmailVerified()){
                    Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("ID",user.getUid());
                    intent.putExtra("EMAIL",user.getEmail());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
    }


}
