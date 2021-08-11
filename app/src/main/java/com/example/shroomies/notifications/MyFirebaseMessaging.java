package com.example.shroomies.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.shroomies.MyShroomiesActivity;
import com.example.shroomies.R;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    PendingIntent pendingIntent;
    FirebaseAuth mAuth;
    String userID;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOAndAboveNotification(remoteMessage);
                } else {
                    sendNormalNotification(remoteMessage);
                }
    }

    private void sendNormalNotification(RemoteMessage msg) {
//        String groupID = msg.getData().get("groupID");
        if (msg.getNotification()!=null) {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null){
                 userID=firebaseUser.getUid();
            }
            String title = msg.getNotification().getTitle();
            String body = msg.getNotification().getBody();
            String contentType=msg.getData().get("contentType");
            int i = Integer.parseInt(userID.replaceAll("[\\D]", ""));
            if (contentType.equals("task")) {
                String cardID=msg.getData().get("task");
                Intent intent = new Intent(this, MyShroomiesActivity.class);
                intent.putExtra("CARDID",cardID);
                pendingIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT);
            }
            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_shroomies_full_black_30x28)
                    .setContentText(body)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setSound(defSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int j = 0;
            if (i > 0) {
                j = i;
            }
            notificationManager.notify(j, builder.build());
        }


//        if (groupID != null) {
//            Intent intent = new Intent(this, GroupChattingActivity.class);
//            intent.putExtra("GROUPID", groupID);
//            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
//            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
//        } else {
//            Intent intent = new Intent(this, ChattingActivity.class);
//            intent.putExtra("USERID", user);
//            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
//            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
//        }



    }

    private void sendOAndAboveNotification(RemoteMessage msg) {
        if (msg.getNotification()!=null) {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null){
                userID=firebaseUser.getUid();
            }
            String title = msg.getNotification().getTitle();
            String body = msg.getNotification().getBody();
            String contentType=msg.getData().get("contentType");
            int i = Integer.parseInt(userID.replaceAll("[\\D]", ""));
            if (contentType.equals("task")) {
                String cardID=msg.getData().get("task");
                Intent intent = new Intent(this, MyShroomiesActivity.class);
                intent.putExtra("CARDID",cardID);
                pendingIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT);
            }
            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_shroomies_full_black_30x28)
                    .setContentText(body)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setSound(defSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int j = 0;
            if (i > 0) {
                j = i;
            }
            notificationManager.notify(j, builder.build());
        }

//        if (groupID != null) {
//
//            Intent intent = new Intent(this, GroupChattingActivity.class);
//            intent.putExtra("GROUPID", groupID);
//            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
//            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
//        } else if (isCardNOtification != null || requestNoti!=null || acceptReqNoti!=null ) {
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
//            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        }
//        else {
//            Intent intent = new Intent(this, ChattingActivity.class);
//            intent.putExtra("USERID", user);
//            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
//            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
//        }
    }


    @Override
    public void onNewToken(@NonNull String s){
        super.onNewToken(s);
        Log.d("firebase token" , s);
//        updateToken(s);
    }
//    private void updateToken(String tokenReferesh){
//        mAuth=FirebaseAuth.getInstance();
//        mAuth.useEmulator("10.0.2.2",9099);
//        database=FirebaseDatabase.getInstance();
//        database.useEmulator("10.0.2.2",9000);
//        FirebaseUser user=mAuth.getCurrentUser();
//        String userID= user.getUid();
//        DatabaseReference ref= database.getReference("tokens");
//        Token token= new Token(tokenReferesh);
//        ref.child(userID).setValue(token);
//    }
}
