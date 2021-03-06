package com.example.shroomies.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.shroomies.ChattingActivity;
import com.example.shroomies.GroupChattingActivity;
import com.example.shroomies.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging  extends FirebaseMessagingService {

    PendingIntent pendingIntent;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        if (sent.equals(currentUserID)) {
            if (!currentUserID.equals(user)) {
                sendNormalNotification(remoteMessage);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOAndAboveNotification(remoteMessage);
                } else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage msg) {
        String groupID = msg.getData().get("groupID");
        String user = msg.getData().get("user");
        String icon = msg.getData().get("icon");
        String title = msg.getData().get("title");
        String body = msg.getData().get("body");
        RemoteMessage.Notification notification = msg.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        if (groupID != null) {
            Intent intent = new Intent(this, GroupChattingActivity.class);
            intent.putExtra("GROUPID", groupID);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        } else {
            Intent intent = new Intent(this, ChattingActivity.class);
            intent.putExtra("USERID", user);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        }


        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
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

    private void sendOAndAboveNotification(RemoteMessage msg) {
        String groupID = msg.getData().get("groupID");
        String isCardNOtification = msg.getData().get("cardNotification");
        String user = msg.getData().get("user");
        String icon = msg.getData().get("icon");
        String title = msg.getData().get("title");
        String body = msg.getData().get("body");
        String requestNoti=msg.getData().get("requestNoti");
        String acceptReqNoti=msg.getData().get("acceptReqNoti");
        RemoteMessage.Notification notification = msg.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));

        if (groupID != null) {

            Intent intent = new Intent(this, GroupChattingActivity.class);
            intent.putExtra("GROUPID", groupID);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        } else if (isCardNOtification != null || requestNoti!=null || acceptReqNoti!=null ) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        }
        else {
            Intent intent = new Intent(this, ChattingActivity.class);
            intent.putExtra("USERID", user);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoandAboveNotifactaions noti = new OreoandAboveNotifactaions(this);
        NotificationCompat.Builder builder = noti.getONotifications(title, body, pendingIntent, defSoundUri, icon);

        int j = 0;
        if (i > 0) {
            j = i;
        }

        noti.getManager().notify(j, builder.build());
    }


    @Override
    public void onNewToken(@NonNull String s){
        super.onNewToken(s);
        updateToken(s);
    }
    private void updateToken(String tokenReferesh){
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Token");
        Token token= new Token(tokenReferesh);
        ref.child(userID).setValue(token);
    }
}
