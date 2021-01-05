package com.example.shroomies.notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();
//        if(user!=null){
            updateToken(tokenRefresh);

    }

    private void updateToken(String tokenRefresh) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        ref.child(userId).setValue(token);
    }
}
