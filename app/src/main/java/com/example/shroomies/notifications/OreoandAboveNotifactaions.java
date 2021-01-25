package com.example.shroomies.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class OreoandAboveNotifactaions extends ContextWrapper {

private static final String ID = "SOME_ID";
private static final String NAME = "FirebaseAPP";
private NotificationManager notificationManager;
    public OreoandAboveNotifactaions(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){

            createChannel();
        }
    }

    private void createChannel() {
        NotificationChannel  notificationChannel = new NotificationChannel(ID , NAME , NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }
    public NotificationManager getManager(){
        if(notificationManager == null){
            notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }


    public NotificationCompat.Builder getONotifications(String title , String body , PendingIntent pendingIntent , Uri soundUri, String icon){

        return new NotificationCompat.Builder(getApplicationContext(),ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));

    }


}
