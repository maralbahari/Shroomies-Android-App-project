package com.example.shroomies.notifications;

import android.app.NotificationChannel;
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

import com.example.shroomies.ChattingActivity;
import com.example.shroomies.Config;
import com.example.shroomies.MyShroomiesActivity;
import com.example.shroomies.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    PendingIntent pendingIntent;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    String userID;
    String CHANNEL_ID="SOME_ID";
    String NAME="SOME_NAME";
    int notiID;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NAME, importance);
            sendOAndAboveNotification(remoteMessage,channel);
        } else {
            sendNormalNotification(remoteMessage);
        }
    }

    private void sendNormalNotification(RemoteMessage msg) {
        if (msg.getNotification()!=null) {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null){
                 userID=firebaseUser.getUid();
                notiID=buildNotificationId(userID);
            }
            String title = msg.getNotification().getTitle();
            String body = msg.getNotification().getBody();
            String contentType=msg.getData().get("contentType");
            int i = Integer.parseInt(userID.replaceAll("[\\D]", ""));
            if (contentType.equals(Config.task)) {
                String cardID=msg.getData().get(Config.cardID);
                Intent intent = new Intent(this, MyShroomiesActivity.class);
                intent.putExtra("CARDID",cardID);
                intent.putExtra("TAB", Config.task);
                pendingIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT);
            }
            if (contentType.equals(Config.expenses)) {
                String cardID=msg.getData().get(Config.cardID);
                Intent intent = new Intent(this, MyShroomiesActivity.class);
                intent.putExtra("CARDID",cardID);
                intent.putExtra("TAB", Config.expenses);
                pendingIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT);
            }
            if (contentType.equals("privateMessage")) {
                String messageID=msg.getData().get("messageID");
                String receiverID=msg.getData().get("receiverID");
                Intent intent = new Intent(this, ChattingActivity.class);
                intent.putExtra("USERID",receiverID);
                intent.putExtra("messageID",messageID);
                pendingIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);
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
            notificationManager.notify(notiID, builder.build());
        }
    }

    private void sendOAndAboveNotification(RemoteMessage msg,NotificationChannel channel) {

        if (msg.getNotification()!=null) {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null){
                userID=firebaseUser.getUid();
                notiID=buildNotificationId(userID);
            }
            String title = msg.getNotification().getTitle();
            String body = msg.getNotification().getBody();
            String contentType=msg.getData().get("contentType");
            int i;
            String idWithoutLetters = userID.replaceAll("[\\D]", "");
            if(idWithoutLetters.length()<10){
                i = Integer.parseInt(idWithoutLetters);
            }else{
                i = Integer.parseInt(idWithoutLetters.substring(0,9));
            }

            if (contentType.equals("task")) {
                String cardID=msg.getData().get("task");
                Intent intent = new Intent(this, MyShroomiesActivity.class);
                intent.putExtra("CARDID",cardID);
                pendingIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT);
            }
            if (contentType.equals(Config.expenses)) {
                String cardID=msg.getData().get(Config.cardID);
                Intent intent = new Intent(this, MyShroomiesActivity.class);
                intent.putExtra("CARDID",cardID);
                intent.putExtra("TAB", Config.expenses);
                pendingIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT);
            }
            if (contentType.equals("privateMessage")) {
                String messageID=msg.getData().get("messageID");
                String receiverID=msg.getData().get("receiverID");
                Intent intent = new Intent(this, ChattingActivity.class);
                intent.putExtra("USERID",receiverID);
                intent.putExtra("messageID",messageID);
                pendingIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);
            }
            Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_shroomies_full_black_30x28)
                    .setContentText(body)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setSound(defSoundUri)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(notiID, builder.build());
        }
    }


    @Override
    public void onNewToken(@NonNull String s){
        super.onNewToken(s);
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null) {
            updateToken(s,firebaseUser);
        }
    }
    private void updateToken(String tokenReferesh,FirebaseUser firebaseUser){
        rootRef.child("tokens").child(firebaseUser.getUid()).setValue(tokenReferesh).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("FCM", "token refreshed successfully success");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("FCM", "token didnt refresh");
            }
        });
    }
    private int buildNotificationId(String id){
        Log.d("FCM", "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Log.d("FCM", "buildNotificationId: id: " + id);
        Log.d("FCM", "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }

}
