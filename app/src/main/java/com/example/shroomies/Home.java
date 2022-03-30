package com.example.shroomies;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Home extends Application {
    FirebaseAuth mAuth;
    RequestQueue requestQueue;
    private static final Pattern pattern = Pattern.compile(Config.USERNAME_PATTERN);

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
//            if(user.isEmailVerified()){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            // if the username is invalid
            // then the user signed in for the first time
            // using google
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

//            }
        }

    }
}
