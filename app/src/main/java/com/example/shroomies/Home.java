package com.example.shroomies;

import android.app.Application;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class Home extends Application {
    SessionManager sessionManager;
    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager= new SessionManager();
        String currentUserID=sessionManager.checkUsersLoggedIn(Home.this);
        if(currentUserID!=null){
            sessionManager=new SessionManager(Home.this,currentUserID);
            HashMap userDetails=sessionManager.getUserDetail();
            if(sessionManager.verifiedEmail()){
                Toast.makeText(Home.this,currentUserID,Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("ID",currentUserID);
                intent.putExtra("EMAIL",userDetails.get("EMAIL").toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }


}
