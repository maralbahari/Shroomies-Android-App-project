package com.example.shroomies;

import android.app.Application;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Home extends Application {
    SessionManager sessionManager;
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    public void onCreate() {
        super.onCreate();
        mAuth=FirebaseAuth.getInstance();
                FirebaseUser user=mAuth.getCurrentUser();
                if(user!=null && user.isEmailVerified()){
                    mAuth.removeAuthStateListener(authStateListener);
                    Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("ID",user.getUid());
                    intent.putExtra("EMAIL",user.getEmail());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
    }
}
